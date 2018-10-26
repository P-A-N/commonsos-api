package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class AdListControllerTest {

  @Mock AdService service;
  @Mock Request request;
  @InjectMocks AdListController controller;

  @Test
  public void handle() throws Exception {
    when(request.queryParams("communityId")).thenReturn("123");
    ArrayList<AdView> ads = new ArrayList<>();
    User user = new User();
    when(service.listFor(user, 123L, "filter text")).thenReturn(ads);
    when(request.queryParams("filter")).thenReturn("filter text");

    assertThat(controller.handle(user, request, null)).isSameAs(ads);
  }

  @Test(expected = BadRequestException.class)
  public void handle_noCommunityId() {
    controller.handle(new User(), request, null);
  }
}