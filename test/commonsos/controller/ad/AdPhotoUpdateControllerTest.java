package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.service.command.AdPhotoUpdateCommand;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class AdPhotoUpdateControllerTest {

  @Mock Request request;
  @Mock AdService service;
  @InjectMocks @Spy AdPhotoUpdateController controller;

  @Test
  public void handle() {
    User user = new User();
    ArgumentCaptor<AdPhotoUpdateCommand> commandArgument = ArgumentCaptor.forClass(AdPhotoUpdateCommand.class);
    when(service.updatePhoto(eq(user), commandArgument.capture())).thenReturn("/url");
    when(request.params("id")).thenReturn("123");
    InputStream image = mock(InputStream.class);
    doReturn(image).when(controller).image(request);

    String result = controller.handleAfterLogin(user, request, null);

    assertThat(commandArgument.getValue().getAdId()).isEqualTo(123);
    assertThat(commandArgument.getValue().getPhoto()).isEqualTo(image);
    assertThat(result).isEqualTo("/url");
  }
}