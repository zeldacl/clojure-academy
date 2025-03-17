#version 120

varying vec3 camspace;

// pd函数：将inp的前三个分量除以第四个分量，返回结果
vec3 pd(vec4 inp) {
	return inp.xyz / inp.w;
}

void main() {
	// 计算顶点在裁剪坐标系下的位置
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	// 计算顶点在相机坐标系下的位置
	camspace = pd(gl_ModelViewMatrix * gl_Vertex);
}
