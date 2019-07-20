package commonsos.filter;

import javax.inject.Inject;

import commonsos.Configuration;
import spark.Filter;
import spark.Request;
import spark.Response;

public class AddHeaderFilter implements Filter {

  @Inject Configuration config;

  @Override
  public void handle(Request request, Response response) throws Exception {
    response.type("application/json");
    response.header("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
    response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    response.header("Access-Control-Allow-Headers", "Origin, Content-Type");
    response.header("Access-Control-Max-Age", "3600");
    response.header("Access-Control-Allow-Credentials", "true");
  }
}
