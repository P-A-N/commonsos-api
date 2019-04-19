package commonsos.controller;

import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class PreflightController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    response.header("Access-Control-Allow-Origin", "http://commonspeople.localhost:8888");
    response.header("Access-Control-Allow-Methods", "GET, POST");
    response.header("Access-Control-Allow-Headers", "Origin, Content-Type");
    response.header("Access-Control-Max-Age", "3600");
    response.header("Access-Control-Allow-Credentials", "true");
    response.status(204);
    return "";
  }
}
