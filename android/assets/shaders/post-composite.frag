#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_bloom;
uniform float u_bloomIntensity;
uniform float u_vignette;
uniform float u_pulse;
uniform float u_dread;
uniform float u_exposure;

// Roadmap E1, pass 3: the frame comes back. The blurred light is added where it was born,
// the night is lifted, and a radial vignette darkens the corners so the lane reads as lit
// from inside the fight. The HUD never passes through here -- it is drawn after this,
// straight to the screen.
//
// The lift: the arena art is painted as a night, and measured as one -- a mean of about
// 37 of 255 on the backdrop alone -- which read as too dark on a phone. Each channel is
// multiplied by u_exposure at black, easing to exactly one at white, so the dark ground
// gains what it lacked while light that was already bright neither clips nor blooms
// harder (the bright pass reads the scene before this). Black stays black: an unlit
// pixel is still zero, which is what keeps a black frame measurable as one.
void main() {
    vec4 sceneColor = texture2D(u_texture, v_texCoords);
    vec3 bloom = texture2D(u_bloom, v_texCoords).rgb;
    vec2 offset = v_texCoords - vec2(0.5);
    float dist = length(offset);
    float falloff = smoothstep(0.48, 0.75, dist);
    vec3 lit = sceneColor.rgb + bloom * u_bloomIntensity;
    lit = lit * (u_exposure - (u_exposure - 1.0) * lit);
    vec3 rgb = lit * (1.0 - u_vignette * falloff);
    // A critical hit re-tints the vignette edge red while its pulse decays: the frame itself
    // answers the hit without a HUD element or a screen shake of its own.
    float pulse = u_pulse * falloff;
    rgb += vec3(0.85, 0.10, 0.08) * pulse * 0.45;
    rgb *= 1.0 - pulse * 0.22;
    // Dread: while a boss or an elite stands, the vignette edge breathes red -- a slower,
    // deeper stain than a crit's flash, eased in Java so it never pops.
    float dread = u_dread * falloff;
    rgb += vec3(0.45, 0.05, 0.05) * dread * 0.5;
    rgb *= 1.0 - dread * 0.18;
    gl_FragColor = vec4(rgb, sceneColor.a);
}
