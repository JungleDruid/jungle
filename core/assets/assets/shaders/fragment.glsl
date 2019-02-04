#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main()
{
    vec4 color = vec4(1, 1, 1, 1);
    if (v_texCoords.x < .3333333 || v_texCoords.x > .6666666) {
        color.a *= 1.0f - (abs(0.5f - v_texCoords.x) - 0.1666667f) / 0.3333333f;
    }
    if (v_texCoords.y < .3333333 || v_texCoords.y > .6666666) {
        color.a *= 1.0f - (abs(0.5f - v_texCoords.y) - 0.1666667f) / 0.3333333f;
    }
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * color;
}