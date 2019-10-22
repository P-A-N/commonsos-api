package commonsos.controller.app.ad;

import javax.inject.Inject;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.util.RequestUtil;
import commonsos.view.AdView;
import spark.Request;
import spark.Response;

public class GetAdController extends AfterAppLoginController {
  
  @Inject AdService service;

  @Override public AdView handleAfterLogin(User user, Request request, Response response) {
    Long id = RequestUtil.getPathParamLong(request, "id");
    
    Ad ad = service.getAd(id);
    if (!user.getId().equals(ad.getCreatedUserId())) {
      ad = service.getPublicAd(id);
    }
    return service.view(ad, user);
  }
}
