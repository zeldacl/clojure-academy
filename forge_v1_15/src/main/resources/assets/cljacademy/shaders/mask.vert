#version 120

// 顶点着色器
varying vec4 Color; // 颜色
varying vec2 UV, MaskUV; // 纹理坐标

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; // 计算顶点位置
    Color = gl_Color; // 获取颜色
    UV = gl_MultiTexCoord0.xy; // 获取纹理坐标
    MaskUV = gl_MultiTexCoord4.xy; // 获取遮罩纹理坐标
}


//#version 120
//
//varying vec4 Color;
//varying vec2 UV, MaskUV;
//
//void main() {
//    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
//    Color = gl_Color;
//    UV = gl_MultiTexCoord0.xy;
//    MaskUV = gl_MultiTexCoord4.xy;
//}
