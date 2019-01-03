package commonsos.controller.message;

import static commonsos.annotation.SyncObject.MESSAGE_THRED_FOR_AD;
import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.annotation.Synchronized;
import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;

@Synchronized(MESSAGE_THRED_FOR_AD)
public class MessageThreadForAdController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    return service.threadForAd(user, parseLong(request.params("adId")));
  }
}
