package commonsos;

import static commonsos.controller.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import spark.Filter;
import spark.Request;
import spark.Response;

@Slf4j
public class AuthenticationFilter implements Filter {

  private List<String> exclusionPaths;

  public AuthenticationFilter(List<String> exclusionPaths) {
    this.exclusionPaths = exclusionPaths;
  }

  @Override public void handle(Request request, Response response) {
    if (exclusionPaths.contains(request.pathInfo())) return;

    if (request.session().attribute(USER_SESSION_ATTRIBUTE_NAME) == null) throw new AuthenticationException();
  }
}
