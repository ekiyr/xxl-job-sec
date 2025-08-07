package com.xxl.job.core.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Security;

public class Sm4Util {

    /** 初始化BC */
    static {
        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static final byte[] DEFAULT_IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};


    public static final String ALGORITHM_NAME = "SM4";
    /**
     * 加密算法/分组加密模式/分组填充方式
     * PKCS5Padding-以8个字节为一组进行分组加密
     * 定义分组加密模式使用：PKCS5Padding
     */
    public static final String ECB_PADDING = "SM4/ECB/PKCS5Padding";
    public static final String CBC_PADDING = "SM4/CBC/PKCS7Padding";


    public static String encrypt4Base64WithCBC(String data, String aeskey) {
        return encrypt4Base64WithCBC(data, StandardCharsets.UTF_8, aeskey);
    }

    public static String encrypt4Base64WithCBC(String data, Charset charset, String aeskey) throws CryptException {
        byte[] plainData = data.getBytes(charset);
        byte[] key = Base64Util.decode(aeskey);
        return Base64Util.encodeUrlSafe(encryptWithCBC(plainData, key, DEFAULT_IV));
    }

    public static byte[] encryptWithCBC(byte[] data, byte[] aeskey, byte[] iv) throws CryptException {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(aeskey, ALGORITHM_NAME);
            // 实例化
            Cipher cipher = Cipher.getInstance(CBC_PADDING);
            // 初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (CryptException cryptex) {
            throw cryptex;
        } catch (Exception e) {
            throw new CryptException("AES加密失败", e);
        }
    }


    /**
     * 对加密字符串进行解密
     *
     * @param cryptString 字符串
     * @param secKey      秘钥
     */

    public static String decrypt4Base64WithCBC(String cryptString, String secKey) {
        byte[] cryptBytes = Base64Util.decode(cryptString);
        byte[] key = Base64Util.decode(secKey);
        byte[] plainBytes = decryptWithCBC(cryptBytes, key, DEFAULT_IV);
        return new String(plainBytes, StandardCharsets.UTF_8 );
    }

    /**
     * 对加密字符串进行解密
     *
     * @param cryptString 字符串
     * @param secKey      秘钥
     */
    public static String decrypt4Base64WithCBC(String cryptString, byte[] secKey, byte[] ivbytes) {
        //若目标文件夹不存在,则创建文件夹
        if (!StringUtils.hasText(cryptString)) {
            return cryptString;
        }
        try {
            byte[] cryptBytes = Base64Util.decode(cryptString);
            byte[] plainBytes = decryptWithCBC(cryptBytes, secKey, ivbytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new CryptException("SM4解密错误!", ex);
        }
    }

    /**
     * 对加密字符串进行解密
     *
     * @param crypted 字节数组
     * @param secKey  秘钥
     */
    public static byte[] decryptWithCBC(byte[] crypted, byte[] secKey, byte[] ivbytes) {
        //若目标文件夹不存在,则创建文件夹
        if (null == crypted || crypted.length == 0) {
            return crypted;
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(secKey, ALGORITHM_NAME);
            Cipher cipher = Cipher.getInstance(CBC_PADDING);
            if (null != ivbytes && ivbytes.length > 0) {
                IvParameterSpec iv = new IvParameterSpec(ivbytes);
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            }
            return cipher.doFinal(crypted);
        } catch (Exception e) {
            throw new CryptException("SM4解密错误!", e);
        }
    }

    public static String genAesKey4Base64() {
        return Base64Util.encodeUrlSafe(genAesKey(128));
    }
    public static String genAesKey4Base64(int keysSize) {
        return Base64Util.encodeUrlSafe(genAesKey(keysSize));
    }


    public static byte[] genAesKey(int keysSize) {
        try {
            // 实例化密钥生成器
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME);
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

        Long start = System.currentTimeMillis()-5*60*1000L;

        System.out.println(genAesKey4Base64(128));

        String str ="{123123:yiopooooo}";
        String TOKEN_AES_KEY = "svz4mfW2ynl49Q08sD5lOA";

        String plainStr = String.format("%016d",start)+str;

        String tokenOnAes = encrypt4Base64WithCBC(plainStr,TOKEN_AES_KEY);
        System.out.println(">>"+tokenOnAes);
        String tokenHex = new BigInteger(tokenOnAes.getBytes()).toString(16);
        System.out.println(tokenHex);
        String tokenAes = new String(new BigInteger(tokenHex, 16).toByteArray());
        System.out.println(tokenAes);
        String pStr = decrypt4Base64WithCBC(tokenAes,TOKEN_AES_KEY);
        System.out.println(pStr);
        Long triggerTime = Long.valueOf(pStr.substring(0,16));
        System.out.println("diffTime:"+(System.currentTimeMillis()-triggerTime));
        String orgStr = pStr.substring(16);
        System.out.println(triggerTime+"-;-"+orgStr);
        System.out.println("Cost:"+(System.currentTimeMillis()-start));


    }
}
