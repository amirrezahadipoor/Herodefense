#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_texelStep;

// Roadmap E1, pass 2: the separable Gaussian, run once across and once down at half
// resolution. Five bilinear taps per direction with the standard linear-sampling offsets,
// which is nine texels' worth of weight for five fetches.
void main() {
    vec3 sum = texture2D(u_texture, v_texCoords).rgb * 0.227027;
    sum += (texture2D(u_texture, v_texCoords + u_texelStep * 1.3846).rgb
          + texture2D(u_texture, v_texCoords - u_texelStep * 1.3846).rgb) * 0.3162162;
    sum += (texture2D(u_texture, v_texCoords + u_texelStep * 3.2308).rgb
          + texture2D(u_texture, v_texCoords - u_texelStep * 3.2308).rgb) * 0.0702703;
    gl_FragColor = vec4(sum, 1.0);
}
