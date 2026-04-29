package com.appd.instll;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypter {

    private static final String PASSWORD = "4814780584699673";
    private static final String SALT = "2894356330652558";
    private static final String IV = "2230209522049090";

    private static final int KEY_SIZE = 128;
    private static final int ITERATIONS = 65536;

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";

    private static Crypter instance;

    public static synchronized Crypter getInstance() {
        if (instance == null) {
            instance = new Crypter();
        }
        return instance;
    }

    /* =========================================================
       🔐 MÉTODOS EM BYTES (FORMA CORRETA)
       ========================================================= */

    public byte[] decryptBytes(byte[] encryptedBytes) throws Exception {
        IvParameterSpec ivSpec =
                new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = generateKey();

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(encryptedBytes);
    }

    public byte[] encryptBytes(byte[] rawBytes) throws Exception {
        IvParameterSpec ivSpec =
                new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = generateKey();

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(rawBytes);
    }

    /* =========================================================
       🔁 MÉTODOS DE CONVENIÊNCIA (BASE64)
       ========================================================= */

    public byte[] decryptFromBase64(String encryptedBase64) throws Exception {
        byte[] encrypted =
                Base64.decode(encryptedBase64, Base64.NO_WRAP);
        return decryptBytes(encrypted);
    }

    public String encryptToBase64(byte[] rawBytes) throws Exception {
        byte[] encrypted = encryptBytes(rawBytes);
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    /* ========================================================= */

    private SecretKeySpec generateKey() throws Exception {
        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(KEY_ALGORITHM);

        KeySpec spec = new PBEKeySpec(
                PASSWORD.toCharArray(),
                SALT.getBytes(StandardCharsets.UTF_8),
                ITERATIONS,
                KEY_SIZE
        );

        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
