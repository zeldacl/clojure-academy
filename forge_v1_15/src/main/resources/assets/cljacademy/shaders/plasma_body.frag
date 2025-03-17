#version 120

uniform int ballCount;
uniform vec4 balls[16]; // xyz: position(in camera space) w: size
uniform float alpha;

varying vec3 camspace;

// 计算密度的函数
// 该函数接受一个vec3类型的position参数，返回一个float类型的值，表示该点的密度
float f(vec3 position) {
    float ret = 0.0;
    for (int i = 0; i < ballCount; ++i) {
        // 计算当前点到球心的距离
        float distance = max(0.1, length(position - balls[i].xyz));

        // 根据球的大小和alpha值计算密度
        ret += alpha * balls[i].w / (distance * distance);
    }
    // 将密度限制在[0, 2]之间
    return clamp(ret, 0, 2);
}

// 光线追踪函数
// 该函数接受一个起点和一个方向，返回一个vec4，表示该方向上的颜色和alpha值
vec4 rayMarch(vec3 begin, vec3 dir) {
    dir *= 0.15;

    vec3 pos = begin;

    vec4 accum = vec4(0, 0, 0, 0);
    for (int i = 0; i < 20 && accum.a < 1; ++i) {
        // 计算当前点的密度
        float density = f(pos);

        // 根据密度计算alpha值
        float alpha = 0.075 * density;

        // 根据密度计算颜色
        vec3 crl = mix(vec3(0.43, 0.74, 1), vec3(0.98, 0.51, 0.92), 1-density/2);

        // 混合颜色和alpha值
        accum.rgb = mix(accum.rgb, crl, alpha / (accum.a + alpha));
        accum.a += alpha;

        // 沿着光线移动相机
        pos += dir;
    }

    // 如果alpha值小于0.2，则将其映射到[0.2, 0]，以使混合在边缘区域看起来正常
    if (accum.a < 0.2) {
        accum.a = 2 * accum.a - 0.2;
    }
    // accum.a = clamp(accum.a, 0, 1); //* 0.8;
    return accum;
}

// 主函数
void main() {
    // 获取相机位置
    vec3 cam = camspace;
    // 将相机位置转换为相机空间
    cam.z = -cam.z;

    // 计算相机方向
    vec3 dir = normalize(cam);

    // 光线追踪
    vec4 rc = rayMarch(cam - dir * 3, dir);
    // 限制alpha值在[0, 1]之间
    rc.a = clamp(rc.a, 0, 1) * (0.5 + alpha * 0.5);

    // 输出颜色
    gl_FragColor = rc;
}
// 该代码是一个片段着色器，使用光线追踪算法渲染一个3D场景。它接受一组球体，表示为vec4，使用函数f()计算每个点的密度。然后使用光线追踪算法渲染场景，rayMarch()函数中的每次循环迭代沿着光线移动相机并累积场景的颜色和alpha值。最终颜色输出为gl_FragColor。

//#version 120
//
//uniform int ballCount;
//uniform vec4 balls[16]; // xyz: position(in camera space) w: size
//uniform float alpha;
//
//varying vec3 camspace;
//
//float f(vec3 position) {
//    float ret = 0.0;
//    for (int i = 0; i < ballCount; ++i) {
//        float distance = max(0.1, length(position - balls[i].xyz));
//
//        ret += alpha * balls[i].w / (distance * distance);
//    }
//    return clamp(ret, 0, 2);
//}
//
//vec4 rayMarch(vec3 begin, vec3 dir) {
//    dir *= 0.15;
//
//    vec3 pos = begin;
//
//    vec4 accum = vec4(0, 0, 0, 0);
//    for (int i = 0; i < 20 && accum.a < 1; ++i) {
//        float density = f(pos);
//
//        float alpha = 0.075 * density;
//        vec3 crl = mix(vec3(0.43, 0.74, 1), vec3(0.98, 0.51, 0.92), 1-density/2);
//
//        accum.rgb = mix(accum.rgb, crl, alpha / (accum.a + alpha));
//        accum.a += alpha;
//
//        pos += dir;
//    }
//
//    if (accum.a < 0.2) { // Make alpha in [0.2, 0.1] map to [0.2, 0], to make blending look normal in edge area
//        accum.a = 2 * accum.a - 0.2;
//    }
//    // accum.a = clamp(accum.a, 0, 1); //* 0.8;
//    return accum;
//}
//
//void main() {
//    vec3 cam = camspace;
//    cam.z = -cam.z;
//
//    vec3 dir = normalize(cam);
//
//    vec4 rc = rayMarch(cam - dir * 3, dir);
//    rc.a = clamp(rc.a, 0, 1) * (0.5 + alpha * 0.5);
//
//	gl_FragColor = rc;
//}
