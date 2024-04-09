#version 120

varying vec4 Color;
varying vec2 UV;

uniform float texOffset;
uniform sampler2D samplerTex, samplerMask;

void main() {
    // 从纹理中获取颜色
    vec4 colorTex = Color * texture2D(samplerTex, UV.st + vec2(texOffset, 0));
    // 从遮罩中获取颜色
    float colorMask = texture2D(samplerMask, UV.st).a;
    // 将颜色和遮罩混合
    gl_FragColor = vec4(colorTex.rgb, colorMask * colorTex.a);
}

//#version 120
//
//varying vec4 Color;
//varying vec2 UV;
//
//uniform float texOffset;
//uniform sampler2D samplerTex, samplerMask;
//
//void main() {
//    vec4 colorTex = Color * texture2D(samplerTex, UV.st + vec2(texOffset, 0));
//    float colorMask = texture2D(samplerMask, UV.st).a;
//    gl_FragColor = vec4(colorTex.rgb, colorMask * colorTex.a);
//}
