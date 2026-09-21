package com.amirrezahadipoor.herodefense.save;

/**
 * Writes encoded save payloads off the frame thread, last payload wins (roadmap B4).
 *
 * <p>The frame that saves is the frame that should not stall: {@code LocalSaveRepository.save} used to encode the
 * state into JSON and write it to preferences inline, which on the frame thread meant a measured 165+ ms of work at
 * the end of a two-hundred-wave run, twice per save (primary and backup keys), at every level-up and at every
 * pause. Encoding stays on the caller's thread -- the {@code GameState} is the game's, and handing a live object to
 * another thread is how a save becomes a race -- but the two {@code putString} calls and the {@code flush} that
 * follow it are the disk, and the disk moves here.
 *
 * <p><b>Last payload wins on purpose.</b> A queue would write states nobody will ever load, and the cost of a
 * skipped intermediate state is one generation of the backup copy, which is a fallback for a corrupt primary and
 * not a version history. So a save that arrives while another is in flight replaces it: the primary ends up holding
 * the newest state the game had and the backup the one before that.
 *
 * <p><b>A read is a flush.</b> {@link #flush()} blocks until nothing is pending and nothing is in flight, and
 * {@code LocalSaveRepository} calls it before every load, existence check and clear. That is what keeps the class
 * honest for the caller that saved and immediately asked what it had saved -- including every test.
 *
 * <p>The worker is a daemon, is created on the first save rather than in the constructor, and is not recreated once
 * {@link #close()} has run: a save that arrives after the close is written inline, because by then the process is
 * on its way out and the write is the last thing that matters.
 */
final class BackgroundSaveWriter implements AutoCloseable {

    /** Where a payload goes once the worker has it; the repository's own preference writes. */
    interface Sink {
        void write(String payload);
    }

    private final Sink sink;
    private final Object lock = new Object();

    private String pending;
    private boolean writing;
    private boolean workerAlive;
    private boolean closed;

    BackgroundSaveWriter(Sink sink) {
        if (sink == null) {
            throw new IllegalArgumentException("A sink is required");
        }
        this.sink = sink;
    }

    /** Queues a payload. Never blocks on the disk; starts the worker the first time it is called. */
    void submit(String payload) {
        if (payload == null) {
            return;
        }
        synchronized (lock) {
            if (closed) {
                sink.write(payload);
                return;
            }
            pending = payload;
            if (workerAlive) {
                lock.notifyAll();
                return;
            }
            workerAlive = true;
        }
        Thread worker = new Thread(this::drain, "hero-defense-save-writer");
        worker.setDaemon(true);
        worker.start();
    }

    /** Waits until nothing is pending and nothing is being written. A no-op when called from the worker itself. */
    void flush() {
        if ("hero-defense-save-writer".equals(Thread.currentThread().getName())) {
            return;
        }
        synchronized (lock) {
            while (pending != null || writing) {
                try {
                    lock.wait(1000L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /** Flushes, then lets the worker exit on its next tick. Idempotent. */
    @Override
    public void close() {
        flush();
        synchronized (lock) {
            closed = true;
            lock.notifyAll();
        }
    }

    /** True while a payload is queued or being written; the gate's way of asking whether a save is in flight. */
    boolean busy() {
        synchronized (lock) {
            return pending != null || writing;
        }
    }

    private void drain() {
        while (true) {
            String payload;
            synchronized (lock) {
                while (pending == null && !closed) {
                    try {
                        lock.wait(1000L);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        workerAlive = false;
                        return;
                    }
                }
                if (pending == null) {
                    workerAlive = false;
                    lock.notifyAll();
                    return;
                }
                payload = pending;
                pending = null;
                writing = true;
            }
            try {
                sink.write(payload);
            } catch (RuntimeException failure) {
                // A save that fails must not kill the worker, the game or the next save: the payload that could
                // not be written is dropped and the next one is attempted. The failure is the platform's to
                // report -- core has no logger by rule -- and the visible effect is one lost save generation,
                // which is exactly what the backup key exists to absorb.
                writing = false;
            }
            synchronized (lock) {
                writing = false;
                lock.notifyAll();
            }
        }
    }
}
