package vn.edu.webpro.robotlab.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** JDK-provided PBKDF2 password storage for the Tomcat runtime. */
public final class PasswordService {
    private static final int ITERATIONS = 210000;
    private static final int BITS = 256;
    public String hash(String password) { byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);return encode(salt, derive(password,salt)); }
    public boolean verify(String password, String encoded) {
        try { String[] p=encoded.split("\\$"); if(p.length!=4||!"pbkdf2-sha256".equals(p[0])||Integer.parseInt(p[1])!=ITERATIONS)return false; byte[] salt=Base64.getDecoder().decode(p[2]), expected=Base64.getDecoder().decode(p[3]), actual=derive(password,salt); return java.security.MessageDigest.isEqual(expected,actual); } catch(Exception e){ return false; }
    }
    private byte[] derive(String password,byte[] salt){try{KeySpec spec=new PBEKeySpec(password.toCharArray(),salt,ITERATIONS,BITS);return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(Exception e){throw new IllegalStateException("Không thể băm mật khẩu.",e);}}
    private String encode(byte[] salt,byte[] hash){return "pbkdf2-sha256$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(hash);}
}
