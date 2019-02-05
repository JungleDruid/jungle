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
    float lowBound = 1.0 / 3.0;
    float highBound = 2.0 / 3.0;
    if (v_texCoords.x < lowBound || v_texCoords.x > highBound) {
        color.a *= 1.0 - (abs(0.5 - v_texCoords.x) - 0.5 + lowBound) / lowBound;
    }
    if (v_texCoords.y < lowBound || v_texCoords.y > highBound) {
        color.a *= 1.0 - (abs(0.5 - v_texCoords.y) - 0.5 + lowBound) / lowBound;
    }
    vec2 l_texCoords = v_texCoords;
    if (time > 0.0)
        l_texCoords = vec2(v_texCoords.x + sin((v_texCoords.x + time * 0.4) * 10.0) * 0.01,
                           v_texCoords.y + sin((v_texCoords.y + time * 0.2) * 10.0) * 0.01);
    gl_FragColor = v_color * texture2D(u_texture, l_texCoords) * color;
}