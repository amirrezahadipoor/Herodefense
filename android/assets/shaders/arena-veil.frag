#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform vec3 u_tint;
uniform float u_aspect;
uniform float u_strength;

// Roadmap D4: the arena air. Two slow mist bands drifting against each other, one broad
// light shaft breathing from the upper right, and a fade toward the bottom of the frame so
// the fight itself stays crisp. Procedural -- it samples nothing, so it cannot moire against
// the reviewed backdrop art, and every constant here is a look, not a measurement.
void main() {
    vec2 p = vec2(v_texCoords.x, v_texCoords.y * u_aspect);

    float band1 = sin(p.x * 3.1 + u_time * 0.11) * cos(p.y * 2.3 - u_time * 0.07);
    float band2 = sin(p.x * 1.7 - u_time * 0.05 + 2.1) * cos(p.y * 3.7 + u_time * 0.09);
    float mist = 0.5 + 0.28 * band1 + 0.22 * band2;

    float shaftAxis = p.x * 0.8 + p.y * 0.6;
    float shaft = exp(-4.0 * abs(fract(shaftAxis * 0.45 - u_time * 0.013) - 0.5));
    shaft *= 0.35 + 0.15 * sin(u_time * 0.23);

    float verticalFade = smoothstep(0.0, 0.55, v_texCoords.y);

    float alpha = clamp((mist * 0.5 + shaft * 0.4) * u_strength * verticalFade, 0.0, 0.14);
    // The quad is a white pixel, so the batch sampler multiplies by one -- but SpriteBatch sets
    // u_texture on every flush and throws when a bound shader does not declare it, and a declared
    // sampler nothing reads is stripped by the GLSL optimizer. Declaring AND reading it is the
    // only shape that survives both. The emulator run of D4's first push is the evidence.
    gl_FragColor = vec4(u_tint, alpha) * v_color * texture2D(u_texture, v_texCoords);
}
