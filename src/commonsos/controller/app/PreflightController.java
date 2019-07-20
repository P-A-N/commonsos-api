package commonsos.controller.app;

import javax.inject.Inject;

import commonsos.Configuration;
import commonsos.annotation.ReadOnly;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class PreflightController implements Route {

  @Inject Configuration config;

  @Override
  public Object handle(Request request, Response response) {
    response.status(204);
    return "";
  }
}