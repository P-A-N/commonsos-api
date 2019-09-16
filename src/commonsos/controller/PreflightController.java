package commonsos.controller;

import javax.inject.Inject;

import commonsos.Configuration;
import spark.Request;
import spark.Response;
import spark.Route;

public class PreflightController implements Route {

  @Inject Configuration config;

  @Override
  public Object handle(Request request, Response response) {
    response.status(204);
    return "";
  }
}
