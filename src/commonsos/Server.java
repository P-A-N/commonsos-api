package commonsos;

import static java.util.Arrays.asList;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

import java.util.stream.Stream;

import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import commonsos.controller.ad.AdController;
import commonsos.controller.ad.AdCreateController;
import commonsos.controller.ad.AdDeleteController;
import commonsos.controller.ad.AdListController;
import commonsos.controller.ad.AdPhotoUpdateController;
import commonsos.controller.ad.AdUpdateController;
import commonsos.controller.ad.MyAdsController;
import commonsos.controller.admin.UserSearchController;
import commonsos.controller.auth.AccountCreateController;
import commonsos.controller.auth.LoginController;
import commonsos.controller.auth.LogoutController;
import commonsos.controller.community.CommunityListController;
import commonsos.controller.message.GroupMessageThreadController;
import commonsos.controller.message.GroupMessageThreadUpdateController;
import commonsos.controller.message.MessageListController;
import commonsos.controller.message.MessagePostController;
import commonsos.controller.message.MessageThreadController;
import commonsos.controller.message.MessageThreadForAdController;
import commonsos.controller.message.MessageThreadListController;
import commonsos.controller.message.MessageThreadUnreadCountController;
import commonsos.controller.message.MessageThreadWithUserController;
import commonsos.controller.transaction.BalanceController;
import commonsos.controller.transaction.TransactionCreateController;
import commonsos.controller.transaction.TransactionListController;
import commonsos.controller.user.UserAvatarUpdateController;
import commonsos.controller.user.UserController;
import commonsos.controller.user.UserDeleteController;
import commonsos.controller.user.UserMobileDeviceUpdateController;
import commonsos.controller.user.UserUpdateController;
import commonsos.service.blockchain.BlockchainEventService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;

@Slf4j
public class Server {

  @Inject private JsonTransformer toJson;
  @Inject private DatabaseMigrator databaseMigrator;
  @Inject private DemoData demoData;
  @Inject private BlockchainEventService blockchainEventService;
  private Injector injector;

  public void start(String[] args) {
    injector = initDependencies();
    databaseMigrator.execute();
    CookieSecuringEmbeddedJettyFactory.register();
    initRoutes();
    blockchainEventService.listenEvents();
    if (demoDataEnabled(args)) demoData.install();
  }

  private boolean demoDataEnabled(String[] args) {
    return Stream.of(args).map(String::toLowerCase).anyMatch(s -> s.equals("--demodata"));
  }

  protected Injector initDependencies() {
    Module module = new AbstractModule() {
      @Override protected void configure() {
        bind(Gson.class).toProvider(GsonProvider.class);
        bind(Web3j.class).toProvider(Web3jProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
      }
    };

    Injector injector = Guice.createInjector(module, new TransactionInterceptor());
    injector.injectMembers(this);
    return injector;
  }

  private void initRoutes() {

    before((request, response) -> response.type("application/json"));
    before(new LogFilter());
    before((request, response) -> log.info(requestInfo(request)));
//    before(new CSRFFilter(asList("/login", "/logout", "/create-account")));
    before(new AuthenticationFilter(asList("/login", "/logout", "/create-account", "/communities")));

    post("/login", injector.getInstance(LoginController.class), toJson);
    post("/create-account", injector.getInstance(AccountCreateController.class), toJson);
    post("/logout", injector.getInstance(LogoutController.class), toJson);
    get("/user", injector.getInstance(UserController.class), toJson);
    get("/users/:id", injector.getInstance(UserController.class), toJson);
    post("/users/:id", injector.getInstance(UserUpdateController.class), toJson);
    post("/users/:id/delete", injector.getInstance(UserDeleteController.class), toJson);
    post("/users/:id/avatar", injector.getInstance(UserAvatarUpdateController.class), toJson);
    post("/users/:id/mobile-device", injector.getInstance(UserMobileDeviceUpdateController.class), toJson);
    get("/users", injector.getInstance(UserSearchController.class), toJson);

    post("/ads", injector.getInstance(AdCreateController.class), toJson);
    get("/ads", injector.getInstance(AdListController.class), toJson);
    get("/ads/:id", injector.getInstance(AdController.class), toJson);
    post("/ads/:id", injector.getInstance(AdUpdateController.class), toJson);
    post("/ads/:id/delete", injector.getInstance(AdDeleteController.class), toJson);
    post("/ads/:id/photo", injector.getInstance(AdPhotoUpdateController.class), toJson);
    get("/my-ads", injector.getInstance(MyAdsController.class), toJson);

    get("/balance", injector.getInstance(BalanceController.class), toJson);
    get("/transactions", injector.getInstance(TransactionListController.class), toJson);
    post("/transactions", injector.getInstance(TransactionCreateController.class), toJson);

    post("/message-threads/for-ad/:adId", injector.getInstance(MessageThreadForAdController.class), toJson);
    post("/message-threads/user/:userId", injector.getInstance(MessageThreadWithUserController.class), toJson);

    post("/message-threads/group", injector.getInstance(GroupMessageThreadController.class), toJson);
    post("/message-threads/:id/group", injector.getInstance(GroupMessageThreadUpdateController.class), toJson);

    get("/message-threads/unread-count", injector.getInstance(MessageThreadUnreadCountController.class), toJson);
    get("/message-threads/:id", injector.getInstance(MessageThreadController.class), toJson);
    get("/message-threads", injector.getInstance(MessageThreadListController.class), toJson);
    post("/message-threads/:id/messages", injector.getInstance(MessagePostController.class), toJson);
    get("/message-threads/:id/messages", injector.getInstance(MessageListController.class), toJson);

    get("/communities", injector.getInstance(CommunityListController.class), toJson);

    exception(BadRequestException.class, (exception, request, response) -> {
      log.error("Bad request", exception);
      response.status(400);
      response.body("");
    });
    exception(AuthenticationException.class, (exception, request, response) -> {
      log.error("Not authenticated");
      response.status(401);
      response.body("");
    });
    exception(ForbiddenException.class, (exception, request, response) -> {
      log.error("Access denied", exception);
      response.status(403);
      response.body("");
    });
    exception(DisplayableException.class, (exception, request, response) -> {
      log.error("Displayable error", exception);
      response.status(468);
      response.body(toJson.render(ImmutableMap.of("key", exception.getMessage())));
    });
    exception(Exception.class, (exception, request, response) -> {
      log.error("Processing failed", exception);
      response.status(500);
      response.body("");
    });
  }

  private String requestInfo(Request request) {
    String info = request.requestMethod() + " " + request.pathInfo();
    if (request.queryString() != null) info += "?" + request.queryString();
    return info;
  }

  public static void main(String[] args) {
    try {
      new Server().start(args);
    }
    catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
