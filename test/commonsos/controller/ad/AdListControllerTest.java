package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.repository.user.User;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdView;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class AdListControllerTest {

  @Mock AdService service;
  @Mock Request request;
  @InjectMocks AdListController controller;

  @Test
  public void handle() throws Exception {
    ArrayList<AdView> ads = new ArrayList<>();
    User user = new User();
    when(service.listFor(user, "filter text")).thenReturn(ads);
    when(request.queryParams("filter")).thenReturn("filter text");

    assertThat(controller.handle(user, request, null)).isSameAs(ads);
  }
}