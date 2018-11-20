package commonsos.controller.message;

import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.GroupMessageThreadUpdateCommand;
import commonsos.view.MessageThreadView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import static com.google.common.primitives.Longs.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupMessageThreadUpdateControllerTest {

  @InjectMocks GroupMessageThreadUpdateController controller;
  @Mock Request request;
  @Mock MessageService service;

  @Before
  public void setGson() {
    controller.gson = new GsonProvider().get();
  }

  @Test
  public void handle() {
    when(request.params("id")).thenReturn("99");
    when(request.body()).thenReturn("{\"title\": \"Hola!\", \"memberIds\": [11, 33]}");
    User user = new User();
    MessageThreadView view = new MessageThreadView();
    when(service.updateGroup(user, new GroupMessageThreadUpdateCommand().setThreadId(99L).setTitle("Hola!").setMemberIds(asList(11, 33)))).thenReturn(view);

    MessageThreadView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isSameAs(view);
  }
}