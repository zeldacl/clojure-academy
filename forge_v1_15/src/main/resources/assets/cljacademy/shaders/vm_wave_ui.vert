#version 120

uniform vec2 screenSize;

// per-vertex
attribute vec2 vertexPos; // 顶点位置
attribute vec2 uv; // 纹理坐标

// per-instance
attribute vec2 offset; // 偏移量
attribute float size; // 大小
attribute float alpha; // 透明度

varying float v_alpha; // 传递给片段着色器的透明度
varying vec2 v_uv; // 传递给片段着色器的纹理坐标

void main() {
    vec2 pos = (vertexPos * size) + offset; // 计算顶点位置

    gl_Position = vec4(pos / screenSize - vec2(0.5), 0, 1); // 计算顶点在屏幕上的位置

    v_alpha = alpha; // 传递透明度
    v_uv = uv; // 传递纹理坐标
}



//#version 330
//
//uniform vec2 screenSize;
//
//// per-vertex
//layout (location=0) in vec2 vertexPos;
//layout (location=1) in vec2 uv;
//
//// per-instance
//layout (location=2) in vec2 offset;
//layout (location=3) in float size;
//layout (location=4) in float alpha;
//
//out float v_alpha;
//out vec2 v_uv;
//
//void main() {
//    vec2 pos = (vertexPos * size) + offset;
//
//    gl_Position = vec4(pos / screenSize - vec2(0.5), 0, 1);
//
//    v_alpha = alpha;
//    v_uv = uv;
//}
