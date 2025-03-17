#version 120

// 顶点着色器
varying vec2 v_uv; // 传递给片元着色器的纹理坐标

void main() {
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; // 计算顶点位置
	v_uv = gl_MultiTexCoord0.xy; // 获取纹理坐标
}


//#version 120
//
//varying vec2 v_uv;
//
//void main() {
//	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
//	v_uv = gl_MultiTexCoord0.xy;
//}
