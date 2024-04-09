#version 120 core

// uniform变量，表示纹理采样器
uniform sampler2D tex;

// varying变量，表示从顶点着色器传递过来的透明度和纹理坐标
varying float v_alpha;
varying vec2 v_uv;

void main() {
	// 通过纹理采样器和纹理坐标获取纹理颜色，并乘以透明度
	gl_FragColor = texture2D(tex, v_uv) * v_alpha;
}


//#version 330 core
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
