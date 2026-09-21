#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_eyeL;
uniform vec2 u_eyeR;
uniform float u_sx;
uniform float u_sy;
uniform float u_strength;

// The Hollow's gaze: two cold lights in the dark upper field of the HOLLOW arena, deep in the
// backdrop. The night in the story is watching ("I felt it happen", "I never get tired of
// watching"), and the reviewed art leaves this field empty -- so the eyes live there. Procedural:
// no texture sampled, nothing that can moire. The look is the constants, and every one of them is
// a look, not a measurement.
void main() {
    vec2 p = v_texCoords;

    float dxL = (p.x - u_eyeL.x) / u_sx;
    float dyL = (p.y - u_eyeL.y) / u_sy;
    float dxR = (p.x - u_eyeR.x) / u_sx;
    float dyR = (p.y - u_eyeR.y) / u_sy;

    // A soft cold glow, wider than tall, with a slightly cooler core.
    float glowL = exp(-0.6 * (dxL * dxL + dyL * dyL));
    float glowR = exp(-0.6 * (dxR * dxR + dyR * dyR));
    float glow = clamp(glowL + glowR, 0.0, 1.0);

    vec3 eye = mix(vec3(0.55, 0.62, 0.78), vec3(0.80, 0.86, 1.00), glow);
    float alpha = glow * u_strength;

    // The quad is a white pixel, so the batch sampler multiplies by one -- but SpriteBatch sets
    // u_texture on every flush and throws when a bound shader does not declare it, and a declared
    // sampler nothing reads is stripped by the GLSL optimizer. Declaring AND reading it is the
    // only shape that survives both. (Same contract as the arena veil.)
    gl_FragColor = vec4(eye, alpha) * v_color * texture2D(u_texture, v_texCoords);
}
