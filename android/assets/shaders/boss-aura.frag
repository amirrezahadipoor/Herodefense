#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;
uniform vec3 u_color;
uniform float u_pulse;

// Roadmap D4: the ground a boss stands on is not the ground an enemy stands on. A soft torus
// of the boss's own telegraph colour breathes under its feet -- the same identity colour the
// special-attack warnings use, so the aura and the telegraphs read as one voice. u_pulse is
// zero under reduced motion, which freezes the breath at its calm value instead of skipping it.
void main() {
    vec2 d = v_texCoords - vec2(0.5);
    float r = length(d) * 2.0;
    float ring = exp(-7.0 * abs(r - 0.62));
    float core = exp(-5.5 * r * r) * 0.4;
    float breath = 0.78 + 0.22 * sin(u_time * 1.9) * u_pulse;
    float alpha = clamp((ring * 0.5 + core) * breath, 0.0, 0.5) * v_color.a;
    gl_FragColor = vec4(u_color, alpha);
}
