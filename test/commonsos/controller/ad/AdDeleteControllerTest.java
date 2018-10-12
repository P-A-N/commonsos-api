package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.domain.ad.AdService;
import commonsos.domain.auth.User;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class AdDeleteControllerTest {

  @Mock Request request;
  @Mock AdService service;
  @InjectMocks AdDeleteController controller;

  @Test
  public void handle() {
    when(request.params("id")).thenReturn("123");
    User user = new User();

    Object result = controller.handle(user, request, null);

    assertThat(result).isEqualTo("");
    verify(service).deleteAdLogically(123L, user);
  }

  @Test(expected = NumberFormatException.class)
  public void handle_parseError() {
    when(request.params("id")).thenReturn("string");
    User user = new User();

    controller.handle(user, request, null);
  }
}