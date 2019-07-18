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
  }
}
