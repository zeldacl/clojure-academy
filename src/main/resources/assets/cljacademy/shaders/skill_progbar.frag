#version 120

// 从纹理中获取阈值
uniform sampler2D texGradient;
// 纹理中的圆形
uniform sampler2D texCircle;
// 进度
uniform float progress;

// 纹理坐标
varying vec2 uv;

void main() {
    // 获取阈值
    float threshold = texture2D(texGradient, uv).r;

    // 如果进度大于阈值，就显示圆形，否则不显示
    gl_FragColor = progress > threshold ? texture2D(texCircle, uv) : vec4(0, 0, 0, 0);
}
