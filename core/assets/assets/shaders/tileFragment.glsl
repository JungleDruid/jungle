#version 130
#ifdef GL_ES
precision mediump float;
#endif

const int right = 1;
const int up    = 2;
const int left  = 4;
const int down  = 8;

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float time;
uniform float bound;
uniform int test;
uniform int ignore;

void main() {
    vec4 color = vec4(1, 1, 1, 1);
    float lowBound = bound;
    if (bound == 0.0) {
        lowBound = 1.0 / 3.0;
    }
    float highBound = 1.0 - lowBound;
    int pos = 0;
    if (v_texCoords.x < lowBound) {
        pos |= left ^ (ignore & left);
    } else if (v_texCoords.x > highBound) {
        pos |= right ^ (ignore & right);
    }
    if (v_texCoords.y < lowBound) {
        pos |= up ^ (ignore & up);
    } else if (v_texCoords.y > highBound) {
        pos |= down ^ (ignore & down);
    }
    if ((pos & (left | right)) != 0) {
        color.a *= 1.0 - (abs(0.5 - v_texCoords.x) - 0.5 + lowBound) / lowBound;
    }
    if ((pos & (up | down)) != 0) {
        color.a *= 1.0 - (abs(0.5 - v_texCoords.y) - 0.5 + lowBound) / lowBound;
    }
    vec2 l_texCoords = v_texCoords;
    if (time > 0.0)
        l_texCoords = vec2(v_texCoords.x + sin((v_texCoords.x + time * 0.4) * 10.0) * 0.01,
                           v_texCoords.y + sin((v_texCoords.y + time * 0.2) * 10.0) * 0.01);
    gl_FragColor = v_color * texture2D(u_texture, l_texCoords) * color;
}
