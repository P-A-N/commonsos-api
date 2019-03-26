package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class MyAdsControllerTest {

  @Mock Request request;
  @Mock AdService service;
  @InjectMocks MyAdsController controller;

  @Test
  public void handle() {
    User user = new User();
    List<AdView> adViewList = new ArrayList<>();
    when(service.myAdsView(user)).thenReturn(adViewList);

    List<AdView> result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(adViewList);
  }
}