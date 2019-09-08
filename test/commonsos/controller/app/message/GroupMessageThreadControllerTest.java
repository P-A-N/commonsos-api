package commonsos.controller.app.message;

import static com.google.common.primitives.Longs.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.message.GroupMessageThreadController;
import commonsos.controller.command.app.CreateGroupCommand;
import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.app.MessageThreadView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class GroupMessageThreadControllerTest {

  @InjectMocks GroupMessageThreadController controller;
  @Mock Request request;
  @Mock MessageService service;

  @BeforeEach
  public void setGson() {
    controller.gson = new GsonProvider().get();
  }

  @Test
  public void handle() {
    when(request.body()).thenReturn("{\"title\": \"hello\", \"memberIds\": [\"11\", \"33\"]}");
    User user = new User();
    MessageThreadView view = new MessageThreadView();
    when(service.group(user, new CreateGroupCommand().setMemberIds(asList(11, 33)).setTitle("hello"))).thenReturn(view);

    MessageThreadView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isSameAs(view);
  }
}