package commonsos.controller.app.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.ad.AdController;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class AdControllerTest {

  @Mock Request request;
  @Mock AdService service;
  @InjectMocks AdController controller;

  @Test
  public void handle() {
    when(request.params("id")).thenReturn("123");
    User user = new User();
    AdView adView = new AdView();
    when(service.view(user, 123L)).thenReturn(adView);

    AdView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(adView);
  }
}