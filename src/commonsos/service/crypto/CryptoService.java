package commonsos.service.crypto;

import static java.lang.Integer.parseInt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.Configuration;
import commonsos.exception.ServerErrorException;
import commonsos.service.AbstractService;

@Singleton
public class CryptoService extends AbstractService {

  private static final int KEY_LENGTH = 256;
  private static final String HASH_ALGORITHM = "SHA-256";
  
  @Inject private Configuration config;

  Base64.Encoder encoder = Base64.getEncoder();
  Base64.Decoder decoder = Base64.getDecoder();

  public String hash(String plainText) {
    try {
      MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
      byte[] hash = md.digest(plainText.getBytes());
      return encoder.encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new ServerErrorException(e);
    }
  }
  
  public String encryptoPassword(String password) {
    byte[] salt = generateSalt();
    int iterations = 10;
    byte[] cryptoText = pbe(password.toCharArray(), salt, iterations, KEY_LENGTH);
    return encoder.encodeToString(salt) + "|" + encoder.encodeToString(cryptoText) + "|" + String.valueOf(iterations);
  }

  public boolean checkPassword(String plainPassword, String encryptPassword) {
    String[] split = encryptPassword.split("\\|");
    if (split.length != 3) return false;

    byte[] salt = decoder.decode(split[0]);
    byte[] expectedHash = decoder.decode(split[1]);
    int iterations = parseInt(split[2]);

    return Arrays.equals(expectedHash, pbe(plainPassword.toCharArray(), salt, iterations, KEY_LENGTH));
  }
  
  public String encryptoWithAES(String plainText) {
    String aesKey = config.aesKey();
    SecretKeySpec kSpec = new SecretKeySpec(aesKey.getBytes(), "AES");
    String aesIv = config.aesIv();
    IvParameterSpec ivSpec = new IvParameterSpec(aesIv.getBytes());
    
    try {
      Cipher encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
      encrypter.init(Cipher.ENCRYPT_MODE, kSpec, ivSpec);
      byte[] b = encrypter.doFinal(plainText.getBytes());
      return encoder.encodeToString(b);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }
  
  public String decryptoWithAES(String encryptoText) {
    String aesKey = config.aesKey();
    SecretKeySpec kSpec = new SecretKeySpec(aesKey.getBytes(), "AES");
    String aesIv = config.aesIv();
    IvParameterSpec ivSpec = new IvParameterSpec(aesIv.getBytes());
    
    try {
      Cipher decrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
      decrypter.init(Cipher.DECRYPT_MODE, kSpec, ivSpec);
      byte[] b = decrypter.doFinal(decoder.decode(encryptoText));
      return new String(b);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }

  byte[] pbe(final char[] text, final byte[] salt, final int iterations, final int keyLength) {
    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
      PBEKeySpec spec = new PBEKeySpec( text, salt, iterations, keyLength );
      SecretKey key = skf.generateSecret( spec );
      return key.getEncoded();

    } catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
      throw new ServerErrorException(e);
    }
  }

  byte[] generateSalt() {
    byte bytes[] = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return bytes;
  }
}
