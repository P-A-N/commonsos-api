package commonsos.service.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;

public class CryptoServiceTest {

  CryptoService service = spy(new CryptoService());

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
}