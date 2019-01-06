package commonsos.controller.community;

import java.util.List;

import javax.inject.Inject;

import commonsos.annotation.ReadOnly;
import commonsos.service.CommunityService;
import commonsos.view.CommunityView;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class CommunityListController implements Route {

  @Inject CommunityService service;

  @Override
  public List<CommunityView> handle(Request request, Response response) {
    String filter = request.queryParams("filter");
    return service.list(filter);
  }
}
