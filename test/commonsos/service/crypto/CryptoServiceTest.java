package commonsos.service.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.Configuration;

@ExtendWith(MockitoExtension.class)
public class CryptoServiceTest {

  @InjectMocks @Spy CryptoService service;
  @Spy Configuration config;
  
  @Before
  public void setup() {
  }

  @Test
  public void encryptoPassword() {
    doReturn(new byte[]{0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,12}).when(service).generateSalt();

    String hash = service.encryptoPassword("secret");

    assertThat(hash).isEqualTo("AAECAwQFBgcICQABAgMEBQYHCAkAAQIDBAUGBwgJDA==|3wRb8pstRs3VEQ02hedNcVJJ6m2jfdcQ9aCAV+xKWyA=|10");
  }

  @Test
  public void checkPassword() {
    String hash = "AAECAwQFBgcICQABAgMEBQYHCAkAAQIDBAUGBwgJDA==|3wRb8pstRs3VEQ02hedNcVJJ6m2jfdcQ9aCAV+xKWyA=|10";

    assertThat(service.checkPassword("secret", hash)).isTrue();
  }

  @Test
  public void checkPassword_wrongPassword() {
    String hash = "AAECAwQFBgcICQABAgMEBQYHCAkAAQIDBAUGBwgJDA==|3wRb8pstRs3VEQ02hedNcVJJ6m2jfdcQ9aCAV+xKWyA=|10";

    assertThat(service.checkPassword("wrong", hash)).isFalse();
  }
  
  @Test
  public void encrypto_decrypto_withAES() {
    String plainText = "hogehoge";
    String encryptedText = service.encryptoWithAES(plainText);
    String decryptedText = service.decryptoWithAES(encryptedText);

    assertThat(plainText).isNotEqualTo(encryptedText);
    assertThat(plainText).isEqualTo(decryptedText);
  }
}