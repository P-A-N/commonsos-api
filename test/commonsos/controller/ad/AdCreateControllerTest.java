package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;

import commonsos.repository.ad.AdType;
import commonsos.repository.user.User;
import commonsos.service.ad.AdCreateCommand;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdView;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class AdCreateControllerTest {

  @Mock Request request;
  @Mock AdService service;
  @InjectMocks AdCreateController controller;

  @Before
  public void setUp() throws Exception {
    controller.gson = new Gson();
  }

  @Test
  public void handle() throws Exception {
    when(request.body()).thenReturn("{\"title\": \"title\", \"description\": \"description\", \"points\": \"123.456\", \"location\": \"location\", \"type\": \"GIVE\"}");
    User user = new User();
    ArgumentCaptor<AdCreateCommand> captor = ArgumentCaptor.forClass(AdCreateCommand.class);
    AdView adView = new AdView();
    when(service.create(eq(user), captor.capture())).thenReturn(adView);

    AdView result = controller.handle(user, request, null);

    assertThat(result).isEqualTo(adView);
    AdCreateCommand ad = captor.getValue();
    assertEquals("title", ad.getTitle());
    assertEquals("description", ad.getDescription());
    assertEquals(new BigDecimal("123.456"), ad.getPoints());
    assertEquals("location", ad.getLocation());
    assertEquals(AdType.GIVE, ad.getType());
  }
}