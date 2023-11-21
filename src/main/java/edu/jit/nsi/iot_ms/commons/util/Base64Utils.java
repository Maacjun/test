package edu.jit.nsi.iot_ms.commons.util;

import java.util.Base64;

/**
 * @packageName: xxz.vegetables.utils
 * @className: Base64Utils
 * @Description:
 * @author: xxz
 * @date: 2019/12/20 13:36
 */

public class Base64Utils {
    /**
     * string 转成 base64字节数组
     **/
    public static byte[] baseConvertStr(String str) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(str.getBytes());
    }

    /**
     * 字节数组 转成 int
     * 小端, data[0]低位, data[mark-1]高位
     **/
    public static int byteArrayToInt(byte[] bytes, int mark) {
        //data[0] 高字节, data[3]低字节
        int value = 0;
        boolean ispostive=true;
        ///反码，例如温度
        if((bytes[0]&0x80)!=0){
            ispostive=false;
            value=-1;
        }
        for (int i = 0; i < mark; i++) {
            if(ispostive)
                value += (bytes[i] & 0xFF) << ((mark-1-i) * 8);
            else
                value -= (~bytes[i] & 0xFF) << ((mark-1-i) * 8);
        }

        return value;
    }

    /**
     * int转字节数组
     * 低位[0]
     *
     */
    public static byte[] intToBytes(int data){
        byte[] b = new byte[4];
        b[0] = (byte)(data & 0xff);
        b[1] = (byte)(data>>8 & 0xff);
        b[2] = (byte)(data>>16 & 0xff);
        b[3] = (byte)(data>>24 & 0xff);
        return b;
    }

}
