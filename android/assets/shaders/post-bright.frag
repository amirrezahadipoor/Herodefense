#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_threshold;

// Roadmap E1, pass 1: keep only the light. A soft knee above the threshold, squared so the
// falloff is gentle -- hard cuts here become hard edges in the blur that follows.
void main() {
    vec4 source = texture2D(u_texture, v_texCoords);
    float luma = dot(source.rgb, vec3(0.2126, 0.7152, 0.0722));
    float soft = clamp((luma - u_threshold) / (1.0 - u_threshold), 0.0, 1.0);
    gl_FragColor = vec4(source.rgb * soft * soft, 1.0);
}
