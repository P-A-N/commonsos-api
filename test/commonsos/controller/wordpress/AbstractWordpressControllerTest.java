package commonsos.controller.wordpress;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.Configuration;
import commonsos.exception.AuthenticationException;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

@ExtendWith(MockitoExtension.class)
public class AbstractWordpressControllerTest {

  @Mock Request request;
  @Mock Configuration config;
  @InjectMocks TestWordpressController controller;

  @Test
  public void handle1() {
    when(request.ip()).thenReturn("127.0.0.1");
    when(config.allowedWordpressRequestIpList()).thenReturn("127.0.0.1");
    controller.handle(request, null);
  }
  
  @Test
  public void handle2() {
    when(request.ip()).thenReturn("127.0.0.1");
    when(config.allowedWordpressRequestIpList()).thenReturn("127.0.0.3,127.0.0.2,127.0.0.1");
    controller.handle(request, null);
  }
  
  @Test
  public void handle3() {
    when(request.ip()).thenReturn("127.0.0.1");
    when(config.allowedWordpressRequestIpList()).thenReturn("127.0.0.2");
    assertThrows(AuthenticationException.class, () -> controller.handle(request, null));
  }
  
  private static class TestWordpressController extends AbstractWordpressController {
    @Override
    protected CommonView handleWordpress(Request request, Response response) {
      return null;
    }
  }
}