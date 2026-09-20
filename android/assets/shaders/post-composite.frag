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

// Roadmap E1, pass 3: the frame comes back. The blurred light is added where it was born,
// and a radial vignette darkens the corners so the lane reads as lit from inside the fight.
// The HUD never passes through here -- it is drawn after this, straight to the screen.
void main() {
    vec4 sceneColor = texture2D(u_texture, v_texCoords);
    vec3 bloom = texture2D(u_bloom, v_texCoords).rgb;
    vec2 offset = v_texCoords - vec2(0.5);
    float dist = length(offset);
    float falloff = smoothstep(0.48, 0.75, dist);
    vec3 rgb = (sceneColor.rgb + bloom * u_bloomIntensity) * (1.0 - u_vignette * falloff);
    // A critical hit re-tints the vignette edge red while its pulse decays: the frame itself
    // answers the hit without a HUD element or a screen shake of its own.
    float pulse = u_pulse * falloff;
    rgb += vec3(0.85, 0.10, 0.08) * pulse * 0.45;
    rgb *= 1.0 - pulse * 0.22;
    gl_FragColor = vec4(rgb, sceneColor.a);
}
