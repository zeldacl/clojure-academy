#version 120

// 传入的参数
varying vec2 uv;

void main() {
    // 将纹理坐标赋值给uv
    uv = gl_MultiTexCoord0.xy;
    // 计算顶点位置
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}

