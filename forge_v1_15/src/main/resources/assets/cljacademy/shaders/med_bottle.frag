#version 120

uniform sampler2D u_texture;
uniform vec4 u_color;

varying vec2 v_uv;

// 定义一个函数，用于将uv坐标转换为纹理坐标
vec2 trans_uv(vec2 uv, int i) {
    const float step = 11.0 / 64.0; // 每个字符的宽度
    vec2 ret = uv;
    ret.x = (i + uv.x) * step; // 计算纹理坐标
    return ret;
}

void main() {
    vec2 used_uv = v_uv;
    used_uv.x = used_uv.x * 32 / 11 - (21.0 / 22.0); // 将uv坐标转换为字符坐标
    if (used_uv.x < 0 || used_uv.x > 1) { // 如果字符坐标不在范围内，则不渲染
        gl_FragColor =  vec4(0, 0, 0, 0);
    } else {
        vec4 color_back = texture2D(u_texture, trans_uv(used_uv, 0)); // 获取背景颜色
        vec4 color_content = texture2D(u_texture, trans_uv(used_uv, 1)) * u_color; // 获取前景颜色，并乘以颜色变量
        vec4 color_front = texture2D(u_texture, trans_uv(used_uv, 2)); // 获取前景颜色

        // 混合三个颜色
        gl_FragColor = mix(mix(color_back, color_content, color_content.a),
        color_front, color_front.a);
    }
}


//#version 120
//
//uniform sampler2D u_texture;
//uniform vec4 u_color;
//
//varying vec2 v_uv;
//
//vec2 trans_uv(vec2 uv, int i) {
//    const float step = 11.0 / 64.0;
//    vec2 ret = uv;
//    ret.x = (i + uv.x) * step;
//    return ret;
//}
//
//void main() {
//    vec2 used_uv = v_uv;
//    used_uv.x = used_uv.x * 32 / 11 - (21.0 / 22.0);
//    if (used_uv.x < 0 || used_uv.x > 1) {
//        gl_FragColor =  vec4(0, 0, 0, 0);
//    } else {
//        vec4 color_back = texture2D(u_texture, trans_uv(used_uv, 0));
//        vec4 color_content = texture2D(u_texture, trans_uv(used_uv, 1)) * u_color;
//        vec4 color_front = texture2D(u_texture, trans_uv(used_uv, 2));
//
//        gl_FragColor = mix(mix(color_back, color_content, color_content.a),
//                           color_front, color_front.a);
//    }
//}
