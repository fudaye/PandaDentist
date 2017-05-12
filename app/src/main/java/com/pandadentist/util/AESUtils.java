package com.pandadentist.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class AESUtils {

    private static final String TAG = AESUtils.class.getSimpleName();


    /**  AES 密钥 */
    public static final String SECRETKEY = "binkeabc123!@#bk";

    /**
     *
     * @Description: 加密
     * @param @param content
     * @param @param password
     * @param @return
     * @return byte[]
     */
    private static byte[] encrypt(String content, String password) {
        try {
            byte[] raw = password.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(content.getBytes("utf-8"));
            return encrypted;
        } catch (Exception e) {
        }
        return null;
    }

    private static byte[] decrypt(byte[] content, String password) {
        try {
            byte[] raw = password.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
        }
        return null;
    }

    private static String parseByte2HexStr(byte buf[]) {
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

    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1),
                        16);
                int low = Integer.parseInt(
                        hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }
    }

    public static String encAESCode(String content) {
        Log.d(TAG,"加密前数据-->"+content);
        byte[] encryptResult = encrypt(content, SECRETKEY);
        String encryptResultStr = parseByte2HexStr(encryptResult);
        return encryptResultStr;
    }

    public static String desAESCode(String content) {
        if(TextUtils.isEmpty(content)){
            Log.d(TAG,"解密字符为空");
            return "";
        }
        byte[] decryptFrom = parseHexStr2Byte(content);
        byte[] decryptResult = decrypt(decryptFrom, SECRETKEY);
        String decryptString = null;
        try {
            decryptString = new String(decryptResult,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "解密后json数据--->" + decryptString);
        return decryptString;
    }

}
