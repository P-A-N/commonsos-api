package commonsos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;


public class TimezoneTest {

  @Test
  public void timezone_must_be_asia_tokyo() {
    assertThat(ZoneId.systemDefault()).isEqualTo(ZoneId.of("Asia/Tokyo"));
  }
}