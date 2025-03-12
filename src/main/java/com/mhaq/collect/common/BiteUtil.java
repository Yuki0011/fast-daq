package com.mhaq.collect.common;


public class BiteUtil {


    public static String format = "yyyy-MM-dd HH:mm:ss";

    public static String tx = "EB90A5C1";
    public static String length = "1179";

    //16进制转byte数组
    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String[] ByteArrayToStringArray(byte[] by) {
        // 命令长度
        int orderLength = hexStringToInt(returnHex(by[6]));
        // 响应数据长度
        int size = by.length - orderLength;
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = returnHex(by[i + orderLength]);
        }
        return result;
    }


    public static String[] ByteToStringArray(byte[] by) {
        // 响应数据长度
        String s = byteArrayToString(by);
        char[] chars = s.toCharArray();
        String[] strArr = new String[chars.length];
        for (int i =0;i<chars.length;i++) {
            strArr[i] = chars[i]+"";
        }
        return strArr;
    }


    public static String byteArrayToString(byte[] by) {
        StringBuilder str = new StringBuilder();
        // 响应数据长度
        int size = by.length;
        for (int i = 0; i < size; i++) {
            str.append(returnHex(by[i]));
        }
        return str.toString();
    }


    public static String returnHex(byte bit) {
        String hex = Integer.toHexString(bit & 0xFF);
        if (hex.length() == 1) {
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }


    /**
     * 16进制字符串转十进制int
     *
     * @param HexString
     * @return
     */
    public static int hexStringToInt(String HexString) {

        int inJTFingerLockAddress = Integer.valueOf(HexString, 16);

        return inJTFingerLockAddress;
    }


    /**
     * 16进制 获取指定位置的2进制字符串
     *
     * @return
     */
    public static String getStrByHex(String hex, int start, int end) {
        int h = Integer.parseInt(hex, 16);
        String bin;
        if (0 == h) {
            bin = "00000000";
        } else {
            bin = bqZero(Integer.toBinaryString(h));
        }
        return bin.substring(start, end);

    }


    public static String bqZero(String bin) {
        int length = bin.length();
        if (length < 8) {
            int size = 8 - length;
            String b = "";
            for (int i = 1; i <= size; i++) {
                b += "0";
            }
            return b + bin;
        }
        return bin;
    }


    public static String byteArrToString(byte[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = values.length; i < size; i++) {
            builder.append(values[i]);
        }
        return builder.toString();
    }


    /**
     * 16进制 获取指定位置 返回十进制数字
     * loactioN
     *
     * @return
     */
    public static String getStrByHex(String hex, int location) {
        int h = Integer.parseInt(hex, 16);
        String bin;
        if (0 == h) {
            bin = "00000000";
        } else {
            bin = bqZero(Integer.toBinaryString(h));
        }
        return bin.substring(location, location + 1);
    }


    // 累加和校验
    public static String makeChecksum(String data) {
        if (data == null || data.equals("")) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        /**
         * 用256求余最大是255，即16进制的FF
         */
        int mod = total % 256;
        String hex = Integer.toHexString(mod);
        len = hex.length();
        // 如果不够校验位的长度，补0,这里用的是两位校验
        if (len < 2) {
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }


    // 累加和校验(最后一位)
    public static String makeChecksumByArr(String[] data, int last) {
        if (null == data || data.length == 0) {
            return "";
        }
        int total = 0;
        for (int i = 0, size = data.length - 1; i < size; i++) {
            total += Integer.parseInt(data[i], 16);
        }
        /**
         * 用256求余最大是255，即16进制的FF
         */
        int mod = total % 256;
        String hex = Integer.toHexString(mod);
        int len = hex.length();
        // 如果不够校验位的长度，补0,这里用的是两位校验
        if (len < 2) {
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }


    // 16进制按位取反
    public static String invertByHex(String str) {
        byte[] buf = hexStrToByteArray(str);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * 计算CRC16校验码
     *
     * @param bytes
     * @return
     */
    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        // 低字节在前高字节在后
        byte[] nbyte = reverse(hexStrToByteArray(Integer.toHexString(CRC)));
        return byteArrayToString(nbyte);
    }


    //实现数组元素的翻转
    public static byte[] reverse(byte[] arr) {
        //遍历数组
        for (int i = 0; i < arr.length / 2; i++) {
            //交换元素 因为i从0开始所以这里一定要再减去1
            byte temp = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = arr[i];
            arr[i] = temp;
        }
        //返回反转后的结果
        return arr;
    }


    public static String parseByteToHexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    // 8位 十六进制 反转 （转换成2进程取反）
    public static String reverseHex(String hex) {
        int h = Integer.parseInt(hex, 16);
        String tb = Integer.toBinaryString(h);
        if (tb.length() < 8) {
            for (int i = 1, size = 8 - tb.length(); i <= size; i++) {
                tb = "0" + tb;
            }
        }
        String bin = reverseBin(tb);
        int b = Integer.parseInt(bin, 2);
        String th = Integer.toHexString(b).toUpperCase();
        return th.length() == 1 ? "0" + th : th;
    }


    // 二进制字符串取反码
    public static String reverseBin(String bin) {
        char[] chars = bin.toCharArray();
        String reverse = "";
        for (int i = 0, size = chars.length; i < size; i++) {
            String a = chars[i] == '0' ? "1" : "0";
            reverse += a;
        }
        return reverse;
    }


    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }


    // 2进制字符串转16进制字符串
    public static String bin2hex(String input) {
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        System.out.println("原数据长度：" + (len / 8) + "字节");

        for (int i = 0; i < len / 4; i++) {
            //每4个二进制位转换为1个十六进制位
            String temp = input.substring(i * 4, (i + 1) * 4);
            int tempInt = Integer.parseInt(temp, 2);
            String tempHex = Integer.toHexString(tempInt).toUpperCase();
            sb.append(tempHex);
        }

        return sb.toString();
    }

    public static String bin2int(String bin) {
        return Integer.parseInt(bin, 2) + "";
    }


}
