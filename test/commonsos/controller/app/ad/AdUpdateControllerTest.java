package commonsos.controller.app.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;

import commonsos.command.app.UpdateAdCommand;
import commonsos.controller.app.ad.UpdateAdController;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class AdUpdateControllerTest {

  @Mock Request request;
  @Mock AdService adService;
  @Captor ArgumentCaptor<UpdateAdCommand> commandCaptor;
  @InjectMocks UpdateAdController controller;

  @BeforeEach
  public void setUp() throws Exception {
    controller.gson = new Gson();
  }

  @Test
  public void handle() throws Exception {
    // prepare
    when(request.body()).thenReturn("{"
        + " \"title\": \"title\","
        + " \"description\": \"description\","
        + " \"points\": \"123.456\","
        + " \"location\": \"location\","
        + " \"type\": \"GIVE\""
        + "}");
    when(request.params("id")).thenReturn("123");
    User user = new User();
    
    Ad ad = new Ad();
    when(adService.updateAd(any(), any())).thenReturn(ad);
    AdView adView = new AdView();
    when(adService.view(ad, user)).thenReturn(adView);
    
    // execute
    AdView result = controller.handleAfterLogin(user, request, null);
    
    // verify
    assertThat(result).isEqualTo(adView);
    
    verify(adService).updateAd(any(), commandCaptor.capture());
    UpdateAdCommand actualCommand = commandCaptor.getValue();
    assertThat(actualCommand.getId()).isEqualTo(123L);
    assertThat(actualCommand.getTitle()).isEqualTo("title");
    assertThat(actualCommand.getDescription()).isEqualTo("description");
    assertThat(actualCommand.getPoints()).isEqualTo(new BigDecimal("123.456"));
    assertThat(actualCommand.getLocation()).isEqualTo("location");
    assertThat(actualCommand.getType()).isEqualTo(AdType.GIVE);
  }

  @Test
  public void handle_parseError() throws Exception {
    // prepare
    when(request.params("id")).thenReturn("string");
    
    // execute
    assertThrows(NumberFormatException.class, () -> controller.handleAfterLogin(null, request, null));
  }
}