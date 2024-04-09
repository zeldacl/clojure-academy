
// 定义渲染属性
Properties {
    Uniform {
    screenSize = vec2(1, 1); //屏幕大小
    tex = sampler2D; //纹理
}
VertexLayout {
vertexPos = POSITION; //顶点位置
uv = UV1; //纹理坐标
}
Instance {
    offset = vec2(0, 0); //偏移量
    size = 1.0; //大小
    alpha = 1.0; //透明度
}
}

// 定义渲染设置
Settings {
    DepthTest Always; //深度测试
    DepthMask Off; //深度掩码
    Blend On; //混合
    BlendFunc SrcAlpha OneMinusSrcAlpha; //混合函数
}

// 定义顶点着色器
Vertex {
#version 120

uniform vec2 screenSize; //屏幕大小

// per-vertex
attribute vec2 vertexPos; //顶点位置
attribute vec2 uv; //纹理坐标

// per-instance
attribute vec2 offset; //偏移量
attribute float size; //大小
attribute float alpha; //透明度

varying float v_alpha; //输出透明度
varying vec2 v_uv; //输出纹理坐标

void main() {
    vec2 pos = (vertexPos * size) + offset; //计算顶点位置

    gl_Position = vec4(pos / screenSize - vec2(0.5), 0, 1); //计算顶点位置

    v_alpha = alpha; //输出透明度
    v_uv = uv; //输出纹理坐标
}
}

// 定义片段着色器
Fragment {
#version 120

    uniform sampler2D tex; //纹理

    varying float v_alpha; //输入透明度
    varying vec2 v_uv; //输入纹理坐标

    void main() {
    gl_FragColor = texture2D(tex, v_uv) * v_alpha; //计算颜色
}
}



//Properties {
//    Uniform {
//        screenSize = vec2(1, 1);
//        tex = sampler2D;
//    }
//    VertexLayout {
//        vertexPos = POSITION;
//        uv = UV1;
//    }
//    Instance {
//        offset = vec2(0, 0);
//        size = 1.0;
//        alpha = 1.0;
//    }
//}
//
//Settings {
//    DepthTest Always;
//    DepthMask Off;
//    Blend On;
//    BlendFunc SrcAlpha OneMinusSrcAlpha;
//}
//
//Vertex {
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
//}
//
//Fragment {
//#version 330
//
//uniform sampler2D tex;
//
//in float v_alpha;
//in vec2 v_uv;
//
//out vec4 fragColor;
//
//void main() {
//	fragColor = texture(tex, v_uv) * v_alpha;
//}
//}
