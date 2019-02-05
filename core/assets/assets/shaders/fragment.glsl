#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float time;

void main()
{
    vec4 color = vec4(1, 1, 1, 1);
    if (v_texCoords.x < .3333333 || v_texCoords.x > .6666666) {
        color.a *= 1.0f - (abs(0.5f - v_texCoords.x) - 0.1666667f) / 0.3333333f;
    }
    if (v_texCoords.y < .3333333 || v_texCoords.y > .6666666) {
        color.a *= 1.0f - (abs(0.5f - v_texCoords.y) - 0.1666667f) / 0.3333333f;
    }
    vec2 l_texCoords = v_texCoords;
    if (time > 0.f)
        l_texCoords = vec2(v_texCoords.x + sin((v_texCoords.x + time * 0.4f) * 10.f) * .01f,
                           v_texCoords.y + sin((v_texCoords.y + time * 0.2f) * 10.f) * .01f);
    gl_FragColor = v_color * texture2D(u_texture, l_texCoords) * color;
}