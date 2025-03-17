#version 120

varying vec4 Color;
varying vec2 UV, MaskUV;

uniform sampler2D texture, mask;

void main() {
    vec4 texcrl = texture2D(texture, UV.st);
    // 使用颜色和纹理颜色相乘，得到最终颜色
    gl_FragColor = Color * vec4(texcrl.rgb, texcrl.a * texture2D(mask, MaskUV.st).a);
    // 使用颜色和纹理颜色的alpha值相乘，得到最终的alpha值
}


//#version 120
//
//varying vec4 Color;
//varying vec2 UV, MaskUV;
//
//uniform sampler2D texture, mask;
//
//void main() {
//    vec4 texcrl = texture2D(texture, UV.st);
//    //gl_FragColor = Color * texcrl;
//    gl_FragColor = Color * vec4(texcrl.rgb, texcrl.a * texture2D(mask, MaskUV.st).a);
//}
