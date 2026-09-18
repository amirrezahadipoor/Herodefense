#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_bloom;
uniform float u_bloomIntensity;
uniform float u_vignette;

// Roadmap E1, pass 3: the frame comes back. The blurred light is added where it was born,
// and a radial vignette darkens the corners so the lane reads as lit from inside the fight.
// The HUD never passes through here -- it is drawn after this, straight to the screen.
void main() {
    vec4 sceneColor = texture2D(u_texture, v_texCoords);
    vec3 bloom = texture2D(u_bloom, v_texCoords).rgb;
    vec2 offset = v_texCoords - vec2(0.5);
    float falloff = clamp(dot(offset, offset) * 1.6, 0.0, 1.0);
    vec3 rgb = (sceneColor.rgb + bloom * u_bloomIntensity) * (1.0 - u_vignette * falloff);
    gl_FragColor = vec4(rgb, sceneColor.a);
}
