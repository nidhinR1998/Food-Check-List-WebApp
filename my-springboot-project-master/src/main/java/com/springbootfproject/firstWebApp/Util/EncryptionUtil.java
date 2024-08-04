package com.springbootfproject.firstWebApp.Util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.springbootfproject.firstWebApp.config.EncryptionConfig;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = ConstantsUtil.VALUE_CODE;
    
    private final EncryptionConfig encryptionConfig;
    @Autowired
    public EncryptionUtil(EncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }
    
  
    public String encrypt(String plainText) throws Exception {
    	String key = encryptionConfig.getEncryptionKey();
        SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getUrlEncoder().encodeToString(encryptedBytes); 
    }

    
    public String decrypt(String encryptedText) throws Exception {
    	String key = encryptionConfig.getEncryptionKey();
        SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

}
