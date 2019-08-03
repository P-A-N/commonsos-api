package commonsos.filter;

import static commonsos.controller.admin.auth.AdminLoginController.ADMIN_SESSION_ATTRIBUTE_NAME;
import static commonsos.controller.app.auth.AppLoginController.USER_SESSION_ATTRIBUTE_NAME;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.MDC;

import commonsos.session.AdminSession;
import commonsos.session.UserSession;
import spark.Filter;
import spark.Request;
import spark.Response;

public class LogFilter implements Filter {
  public static final String USER_MDC_KEY = "username";
  public static final String ADMIN_MDC_KEY = "adminEmailAddress";
  static final String X_REQUEST_ID = "X-Request-Id";

  AtomicLong requestId = new AtomicLong(0);

  @Override
  public void handle(Request request, Response response) throws Exception {
    MDC.put("requestId", requestId(request));
    MDC.put("sessionId", request.session().id().substring(0, 10));
    UserSession userSession = request.session().attribute(USER_SESSION_ATTRIBUTE_NAME);
    MDC.put(USER_MDC_KEY, userSession == null ? "" : userSession.getUsername());
    AdminSession adminSession = request.session().attribute(ADMIN_SESSION_ATTRIBUTE_NAME);
    MDC.put(ADMIN_MDC_KEY, adminSession == null ? "" : adminSession.getAdminEmailAddress());

    MDC.put("ip", request.ip());
  }

  private String requestId(Request request) {
    return request.headers(X_REQUEST_ID) != null ? request.headers(X_REQUEST_ID) : createRequestId();
  }

  private String createRequestId() {
    return Long.toString(requestId.addAndGet(1));
  }
}
