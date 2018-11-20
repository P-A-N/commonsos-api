package commonsos.controller.user;

import commonsos.controller.user.UserMobileDeviceUpdateController;
import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.MobileDeviceUpdateCommand;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserMobileDeviceUpdateControllerTest {

  @InjectMocks UserMobileDeviceUpdateController controller;
  @Mock Request request;
  @Mock UserService userService;
  @Mock User user;

  @Before
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