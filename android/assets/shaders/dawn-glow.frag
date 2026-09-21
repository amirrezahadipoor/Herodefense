#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_progress;
uniform float u_aspect;

// The dawn after the last night: the run's colour arc ends in the HOLLOW's darkest stop, and
// victory is the arc closing -- when the run is complete, the sky behind the premium summary
// breaks from night into dawn gold. Procedural, so it samples nothing and cannot moire against
// the reviewed backdrop; the look is the constants, and every one of them is a look, not a
// measurement.
//
// Restraint is the contract: the light lives at the horizon and dies before the top of the
// frame, where the epilogue lines and the title panel sit, so the words keep their contrast
// while the night behind them turns to morning.
void main() {
    // v_texCoords.y: 0 at the bottom of the frame, 1 at the top.
    float y = v_texCoords.y;
    vec2 p = vec2(v_texCoords.x, v_texCoords.y * u_aspect);

    // The first light at the horizon: full at the floor, gone by mid-frame.
    float glow = pow(clamp(1.0 - y, 0.0, 1.0), 2.2);

    // Two slower bands rising out of it, like the first thin light of a real sunrise. The second
    // only exists near full dawn, so the morning arrives in stages instead of all at once.
    float rise1 = exp(-5.0 * pow(y - (0.10 + 0.06 * u_progress), 2.0) / 0.012);
    float rise2 = exp(-4.0 * pow(y - (0.24 + 0.10 * u_progress), 2.0) / 0.020)
        * smoothstep(0.5, 1.0, u_progress);

    // The light itself breathes; a reduced-motion caller freezes u_time and holds it still.
    float breathe = 0.93 + 0.07 * sin(u_time * 0.11 + p.x * 0.35);

    // Warm at the horizon, paler gold where it climbs.
    vec3 dawn = mix(vec3(1.00, 0.84, 0.58), vec3(1.00, 0.56, 0.30), clamp(1.0 - y * 1.6, 0.0, 1.0));

    // Die before the top of the frame: the epilogue and the title keep their contrast.
    float topGuard = 1.0 - smoothstep(0.5, 0.78, y);

    float light = (glow * 0.30 + rise1 * 0.24 + rise2 * 0.13) * breathe;
    float alpha = clamp(light * topGuard * u_progress, 0.0, 0.45);

    // The quad is a white pixel, so the batch sampler multiplies by one -- but SpriteBatch sets
    // u_texture on every flush and throws when a bound shader does not declare it, and a declared
    // sampler nothing reads is stripped by the GLSL optimizer. Declaring AND reading it is the
    // only shape that survives both. (Same contract as the arena veil.)
    gl_FragColor = vec4(dawn, alpha) * v_color * texture2D(u_texture, v_texCoords);
}
