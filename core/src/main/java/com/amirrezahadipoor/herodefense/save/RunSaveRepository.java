package com.amirrezahadipoor.herodefense.save;

import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.Optional;

/** The run save as the session sees it: clear, save, load (roadmap R2.3). */
public interface RunSaveRepository {

    void save(GameState state);

    Optional<GameState> load();

    boolean hasSave();

    void clear();
}
