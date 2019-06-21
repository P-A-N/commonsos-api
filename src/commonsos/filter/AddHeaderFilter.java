package commonsos.filter;

import spark.Filter;
import spark.Request;
import spark.Response;

public class AddHeaderFilter implements Filter {

  @Override
  public void handle(Request request, Response response) throws Exception {
    response.type("application/json");
    response.header("Access-Control-Allow-Origin", "http://commonspeople.localhost:8888");
  }
}
