package commonsos.controller.app.user;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.user.UserMobileDeviceUpdateController;
import commonsos.controller.command.app.MobileDeviceUpdateCommand;
import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class UserMobileDeviceUpdateControllerTest {

  @InjectMocks UserMobileDeviceUpdateController controller;
  @Mock Request request;
  @Mock UserService userService;
  @Mock User user;

  @BeforeEach
  public void setGson() {
    controller.gson = new GsonProvider().get();
  }

  @Test
  public void handle() {
    when(request.body()).thenReturn("{\"pushNotificationToken\":\"12345\"}");

    controller.handleAfterLogin(user, request, null);

    MobileDeviceUpdateCommand expectedCommand = new MobileDeviceUpdateCommand().setPushNotificationToken("12345");
    verify(userService).updateMobileDevice(user, expectedCommand);
  }
}