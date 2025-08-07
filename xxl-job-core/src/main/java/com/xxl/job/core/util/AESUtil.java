package com.xxl.job.core.util;



import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Security;

/**
 * 使用JAVA API 实现 AES的加密及解密
 * @Author: copyed
 * @date: 2019-12-16 16:17
 */
public class AESUtil {

    public static final String AES_ALGORITHM = "AES";
    public static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String DEFAULT_CHARSET = "GBK";
    public static final int KEY_SIZE_256 = 256;
    public static final int KEY_SIZE_192 = 192;
    public static final int KEY_SIZE_128 = 128;

    public static String encrypt4Base64(String data, String aeskey) {
        return encrypt4Base64(data, DEFAULT_CHARSET, aeskey);
    }

    public static String encrypt4Base64(String data, String charset, String aeskey) throws CryptException {
        byte[] plainData;
        try {
            plainData = data.getBytes(charset);
            byte[] key = Base64Util.decode(aeskey);
            return Base64Util.encodeUrlSafe(encrypt(plainData, key));
        } catch (UnsupportedEncodingException e) {
            throw new CryptException("不支持的编码格式：" + charset, e);
        }
    }

    public static byte[] encrypt(byte[] data, byte[] aeskey) throws CryptException {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(aeskey, AES_ALGORITHM);
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            // 实例化
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            // 初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec,new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (CryptException cryptex) {
            throw cryptex;
        } catch (Exception e) {
            throw new CryptException("AES加密失败", e);
        }
    }

    public static String decrypt4Base64(String data, String aeskey) {
        return decrypt4Base64(data, DEFAULT_CHARSET, aeskey);

    }

    public static String decrypt4Base64(String data, String charset, String aeskey) throws CryptException {
        try {
            byte[] cipherData = Base64Util.decode(data);
            byte[] key = Base64Util.decode(aeskey);
            return new String(decrypt(cipherData, key), charset);
        } catch (UnsupportedEncodingException e) {
            throw new CryptException("不支持的编码格式：" + charset, e);
        }
    }

    public static byte[] decrypt(byte[] data, byte[] aeskey) throws CryptException {
        try {

            SecretKeySpec skeySpec = new SecretKeySpec(aeskey, AES_ALGORITHM);
            // 实例化
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            // 初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, skeySpec,new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptException("解密码失败", e);
        }
    }

    public static String genAesKey4Base64() {
        return Base64Util.encodeUrlSafe(genAesKey());
    }
    
    public static String genAesKey4Base64(int keysSize) {
        return Base64Util.encodeUrlSafe(genAesKey(keysSize));
    }

    public static byte[] genAesKey() {
        return genAesKey(KEY_SIZE_128);
    }
    
    public static byte[] genAesKey(int keysSize) {
        try {
            // 实例化密钥生成器
            KeyGenerator kg = KeyGenerator.getInstance(AES_ALGORITHM);
            // 初始化密钥生成器
            kg.init(keysSize);
            // 生成密钥
            SecretKey secretKey = kg.generateKey();
            // 获取二进制密钥编码形式
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new CryptException("生成AES秘钥失败", e);
        }
    }

    public static void main(String[] args){

        System.out.println(genAesKey4Base64(KEY_SIZE_128));

        String str ="{123123:yiopooooo}";
        String TOKEN_AES_KEY = "svz4mfW2ynl49Q08sD5lOA";

        String tokenOnAes = AESUtil.encrypt4Base64(str,TOKEN_AES_KEY);
        System.out.println(tokenOnAes);
        String tokenHex = new BigInteger(tokenOnAes.getBytes()).toString(16);
        System.out.println(tokenHex);
        String tokenAes = new String(new BigInteger(tokenHex, 16).toByteArray());
        System.out.println(tokenAes);
        String plainStr = AESUtil.decrypt4Base64(tokenAes,TOKEN_AES_KEY);
        System.out.println(plainStr);

    }
    
    
}
