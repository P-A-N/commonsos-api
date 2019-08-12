package commonsos;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import commonsos.controller.admin.admin.CreateAdminCompleteController;
import commonsos.controller.admin.admin.CreateAdminTemporaryController;
import commonsos.controller.admin.admin.DeleteAdminController;
import commonsos.controller.admin.admin.GetAdminController;
import commonsos.controller.admin.admin.SearchAdminsController;
import commonsos.controller.admin.admin.UpdateAdminController;
import commonsos.controller.admin.admin.UpdateAdminEmailCompleteController;
import commonsos.controller.admin.admin.UpdateAdminEmailTemporaryController;
import commonsos.controller.admin.admin.UpdateAdminPhotoController;
import commonsos.controller.admin.auth.AdminLoginController;
import commonsos.controller.admin.auth.AdminLogoutController;
import commonsos.controller.admin.community.CreateCommunityController;
import commonsos.controller.admin.community.DeleteCommunityController;
import commonsos.controller.admin.community.GetCommunityController;
import commonsos.controller.admin.community.SearchCommunityController;
import commonsos.controller.admin.community.UpdateCommunityController;
import commonsos.controller.admin.community.UpdateCommunityCoverPhotoController;
import commonsos.controller.admin.community.UpdateCommunityPhotoController;
import commonsos.controller.admin.community.UpdateCommunityTokenNameController;
import commonsos.controller.admin.community.UpdateCommunityTotalSupplyController;
import commonsos.controller.admin.community.redistribution.CreateRedistributionController;
import commonsos.controller.admin.community.redistribution.DeleteRedistributionController;
import commonsos.controller.admin.community.redistribution.GetRedistributionController;
import commonsos.controller.admin.community.redistribution.SearchRedistributionController;
import commonsos.controller.admin.community.redistribution.UpdateRedistributionController;
import commonsos.controller.admin.transaction.CreateEthTransactionController;
import commonsos.controller.admin.transaction.CreateTokenTransactionController;
import commonsos.controller.admin.transaction.GetEthBalanceController;
import commonsos.controller.admin.transaction.GetTokenBalanceController;
import commonsos.controller.admin.transaction.SearchEthBalanceHistoriesController;
import commonsos.controller.admin.transaction.SearchTokenTransactionsController;
import commonsos.controller.admin.user.DeleteUserController;
import commonsos.controller.admin.user.GetUserController;
import commonsos.controller.admin.user.SearchUserTransactionsController;
import commonsos.controller.admin.user.SearchUsersController;
import commonsos.controller.admin.user.UpdateUserCommunitiesController;
import commonsos.controller.admin.user.UpdateUserController;
import commonsos.controller.admin.user.UpdateUserEmailTemporaryController;
import commonsos.controller.admin.user.UpdateUserNameController;
import commonsos.controller.app.JsonTransformer;
import commonsos.controller.app.PreflightController;
import commonsos.controller.app.ad.AdController;
import commonsos.controller.app.ad.AdCreateController;
import commonsos.controller.app.ad.AdDeleteController;
import commonsos.controller.app.ad.AdListController;
import commonsos.controller.app.ad.AdPhotoUpdateController;
import commonsos.controller.app.ad.AdUpdateController;
import commonsos.controller.app.ad.AdminAdDeleteController;
import commonsos.controller.app.ad.MyAdsController;
import commonsos.controller.app.auth.AppLoginController;
import commonsos.controller.app.auth.AppLogoutController;
import commonsos.controller.app.auth.PasswordResetController;
import commonsos.controller.app.auth.PasswordResetRequestCheckController;
import commonsos.controller.app.auth.PasswordResetRequestController;
import commonsos.controller.app.community.CommunityCoverPhotoUpdateController;
import commonsos.controller.app.community.CommunityListController;
import commonsos.controller.app.community.CommunityNotificationController;
import commonsos.controller.app.community.CommunityNotificationListController;
import commonsos.controller.app.community.CommunityPhotoUpdateController;
import commonsos.controller.app.message.GroupMessageThreadController;
import commonsos.controller.app.message.GroupMessageThreadUpdateController;
import commonsos.controller.app.message.MessageListController;
import commonsos.controller.app.message.MessagePostController;
import commonsos.controller.app.message.MessageThreadController;
import commonsos.controller.app.message.MessageThreadForAdController;
import commonsos.controller.app.message.MessageThreadListController;
import commonsos.controller.app.message.MessageThreadPhotoUpdateController;
import commonsos.controller.app.message.MessageThreadUnreadCountController;
import commonsos.controller.app.message.MessageThreadUnsubscribeController;
import commonsos.controller.app.message.MessageThreadWithUserController;
import commonsos.controller.app.message.UpdateMessageThreadPersonalTitleController;
import commonsos.controller.app.transaction.AdminTransactionListController;
import commonsos.controller.app.transaction.BalanceController;
import commonsos.controller.app.transaction.TransactionCreateController;
import commonsos.controller.app.transaction.TransactionListController;
import commonsos.controller.app.user.CreateUserCompleteController;
import commonsos.controller.app.user.CreateUserTemporaryController;
import commonsos.controller.app.user.GetTransactionQrCodeController;
import commonsos.controller.app.user.SearchUsersCommunityController;
import commonsos.controller.app.user.UpdateAdLastViewTimeController;
import commonsos.controller.app.user.UpdateEmailCompleteController;
import commonsos.controller.app.user.UpdateEmailTemporaryController;
import commonsos.controller.app.user.UpdateNotificationLastViewTimeController;
import commonsos.controller.app.user.UpdateWalletLastViewTimeController;
import commonsos.controller.app.user.UserAvatarUpdateController;
import commonsos.controller.app.user.UserController;
import commonsos.controller.app.user.UserDeleteController;
import commonsos.controller.app.user.UserMobileDeviceUpdateController;
import commonsos.controller.app.user.UserNameUpdateController;
import commonsos.controller.app.user.UserPasswordResetRequestController;
import commonsos.controller.app.user.UserSearchController;
import commonsos.controller.app.user.UserStatusUpdateController;
import commonsos.controller.app.user.UserUpdateCommunitiesController;
import commonsos.controller.app.user.UserUpdateController;
import commonsos.di.GsonProvider;
import commonsos.di.Web3jProvider;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.filter.AddHeaderFilter;
import commonsos.filter.LogFilter;
import commonsos.interceptor.TransactionInterceptor;
import commonsos.repository.DatabaseMigrator;
import commonsos.service.blockchain.BlockchainEventService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;

@Slf4j
public class Server {

  @Inject private JsonTransformer toJson;
  @Inject private DatabaseMigrator databaseMigrator;
  @Inject private BlockchainEventService blockchainEventService;
  private Injector injector;

  public void start(String[] args) {
    injector = initDependencies();
    databaseMigrator.execute();
    CookieSecuringEmbeddedJettyFactory.register();
    setupServer();
    initRoutes();
    blockchainEventService.listenEvents();
  }
  
  protected void setupServer() {
    // nothing to setup in production server.
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

    before(injector.getInstance(AddHeaderFilter.class));
    before(injector.getInstance(LogFilter.class));
    before((request, response) -> log.info(requestInfo(request)));

    initAppUserRoutes();
    initAdminPageRoutes();
    
    exception(BadRequestException.class, (exception, request, response) -> {
      log.error("Bad request", exception);
      response.status(400);
      response.body("");
    });
    exception(AuthenticationException.class, (exception, request, response) -> {
      log.error("Not authenticated", exception);
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

  private void initAppUserRoutes() {
    post("/login", injector.getInstance(AppLoginController.class), toJson);
    post("/logout", injector.getInstance(AppLogoutController.class), toJson);
    post("/create-account", injector.getInstance(CreateUserTemporaryController.class), toJson);
    post("/create-account/:accessId", injector.getInstance(CreateUserCompleteController.class), toJson);
    get("/user", injector.getInstance(UserController.class), toJson);
    get("/users/:id", injector.getInstance(UserController.class), toJson);
    post("/users/:id", injector.getInstance(UserUpdateController.class), toJson);
    post("/users/:id/username", injector.getInstance(UserNameUpdateController.class), toJson);
    post("/users/:id/status", injector.getInstance(UserStatusUpdateController.class), toJson);
    post("/users/:id/passwordreset", injector.getInstance(UserPasswordResetRequestController.class), toJson);
    post("/users/:id/delete", injector.getInstance(UserDeleteController.class), toJson);
    post("/users/:id/avatar", injector.getInstance(UserAvatarUpdateController.class), toJson);
    get("/users/:id/qr", injector.getInstance(GetTransactionQrCodeController.class), toJson);
    post("/users/:id/mobile-device", injector.getInstance(UserMobileDeviceUpdateController.class), toJson);
    get("/users", injector.getInstance(UserSearchController.class), toJson);
    get("/users/:id/communities", injector.getInstance(SearchUsersCommunityController.class), toJson);
    post("/users/:id/communities", injector.getInstance(UserUpdateCommunitiesController.class), toJson);
    post("/users/:id/emailaddress", injector.getInstance(UpdateEmailTemporaryController.class), toJson);
    post("/users/:id/emailaddress/:accessId", injector.getInstance(UpdateEmailCompleteController.class), toJson);
    post("/users/:id/wallet/lastViewTime", injector.getInstance(UpdateWalletLastViewTimeController.class), toJson);
    post("/users/:id/ad/lastViewTime", injector.getInstance(UpdateAdLastViewTimeController.class), toJson);
    post("/users/:id/notification/lastViewTime", injector.getInstance(UpdateNotificationLastViewTimeController.class), toJson);
    post("/passwordreset", injector.getInstance(PasswordResetRequestController.class), toJson);
    get("/passwordreset/:accessId", injector.getInstance(PasswordResetRequestCheckController.class), toJson);
    post("/passwordreset/:accessId", injector.getInstance(PasswordResetController.class), toJson);

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
    post("/message-threads/:id/title", injector.getInstance(UpdateMessageThreadPersonalTitleController.class), toJson);
    post("/message-threads/:id/photo", injector.getInstance(MessageThreadPhotoUpdateController.class), toJson);
    post("/message-threads/:id/unsubscribe", injector.getInstance(MessageThreadUnsubscribeController.class), toJson);

    get("/message-threads/unread-count", injector.getInstance(MessageThreadUnreadCountController.class), toJson);
    get("/message-threads/:id", injector.getInstance(MessageThreadController.class), toJson);
    get("/message-threads", injector.getInstance(MessageThreadListController.class), toJson);
    post("/message-threads/:id/messages", injector.getInstance(MessagePostController.class), toJson);
    get("/message-threads/:id/messages", injector.getInstance(MessageListController.class), toJson);

    get("/communities", injector.getInstance(CommunityListController.class), toJson);
    post("/communities/:id/photo", injector.getInstance(CommunityPhotoUpdateController.class), toJson);
    post("/communities/:id/coverPhoto", injector.getInstance(CommunityCoverPhotoUpdateController.class), toJson);
    post("/communities/:id/notification/:wordpressId", injector.getInstance(CommunityNotificationController.class), toJson);
    get("/communities/:id/notification", injector.getInstance(CommunityNotificationListController.class), toJson);

    // TODO change path
    post("/admin/ads/:id/delete", injector.getInstance(AdminAdDeleteController.class), toJson);
    // TODO delete
    get("/admin/transactions", injector.getInstance(AdminTransactionListController.class), toJson);
  }
  
  private void initAdminPageRoutes() {
    // TODO change to /admin/*
    options("/*", injector.getInstance(PreflightController.class));

    // common
    post("/admin/login", injector.getInstance(AdminLoginController.class), toJson);
    post("/admin/logout", injector.getInstance(AdminLogoutController.class), toJson);
    
    // admins
    post("/admin/create-admin", injector.getInstance(CreateAdminTemporaryController.class), toJson);
    post("/admin/create-admin/:accessId", injector.getInstance(CreateAdminCompleteController.class), toJson);
    get("/admin/admins", injector.getInstance(SearchAdminsController.class), toJson);
    get("/admin/admins/:id", injector.getInstance(GetAdminController.class), toJson);
    post("/admin/admins/:id", injector.getInstance(UpdateAdminController.class), toJson);
    post("/admin/admins/:id/photo", injector.getInstance(UpdateAdminPhotoController.class), toJson);
    post("/admin/admins/:id/emailaddress", injector.getInstance(UpdateAdminEmailTemporaryController.class), toJson);
    get("/admin/admins/:id/emailaddress/:accessId", injector.getInstance(UpdateAdminEmailCompleteController.class), toJson);
    post("/admin/admins/:id/delete", injector.getInstance(DeleteAdminController.class), toJson);

    // app users
    get("/admin/users", injector.getInstance(SearchUsersController.class), toJson);
    get("/admin/users/:id", injector.getInstance(GetUserController.class), toJson);
    post("/admin/users/:id", injector.getInstance(UpdateUserController.class), toJson);
    post("/admin/users/:id/username", injector.getInstance(UpdateUserNameController.class), toJson);
    post("/admin/users/:id/communities", injector.getInstance(UpdateUserCommunitiesController.class), toJson);
    post("/admin/users/:id/emailaddress", injector.getInstance(UpdateUserEmailTemporaryController.class), toJson);
    post("/admin/users/:id/delete", injector.getInstance(DeleteUserController.class), toJson);
    get("/admin/users/:id/transactions", injector.getInstance(SearchUserTransactionsController.class), toJson);

    // transactions
    post("/admin/transactions/coin", injector.getInstance(CreateTokenTransactionController.class), toJson);
    get("/admin/transactions/coin", injector.getInstance(GetTokenBalanceController.class), toJson);
    get("/admin/transactions/coin/balance", injector.getInstance(SearchTokenTransactionsController.class), toJson);
    post("/admin/transactions/eth", injector.getInstance(CreateEthTransactionController.class), toJson);
    get("/admin/transactions/eth/balance", injector.getInstance(GetEthBalanceController.class), toJson);
    get("/admin/transactions/eth/balance/histories", injector.getInstance(SearchEthBalanceHistoriesController.class), toJson);
    
    // communities
    get("/admin/communities", injector.getInstance(SearchCommunityController.class), toJson);
    post("/admin/communities", injector.getInstance(CreateCommunityController.class), toJson);
    get("/admin/communities/:id", injector.getInstance(GetCommunityController.class), toJson);
    post("/admin/communities/:id", injector.getInstance(UpdateCommunityController.class), toJson);
    post("/admin/communities/:id/photo", injector.getInstance(UpdateCommunityPhotoController.class), toJson);
    post("/admin/communities/:id/coverPhoto", injector.getInstance(UpdateCommunityCoverPhotoController.class), toJson);
    post("/admin/communities/:id/totalSupply", injector.getInstance(UpdateCommunityTotalSupplyController.class), toJson);
    post("/admin/communities/:id/tokenName", injector.getInstance(UpdateCommunityTokenNameController.class), toJson);
    post("/admin/communities/:id/delete", injector.getInstance(DeleteCommunityController.class), toJson);
    
    // community redistributions
    get("/admin/communities/:id/redistributions", injector.getInstance(SearchRedistributionController.class), toJson);
    post("/admin/communities/:id/redistributions", injector.getInstance(CreateRedistributionController.class), toJson);
    get("/admin/communities/:id/redistributions/:redistributionId", injector.getInstance(GetRedistributionController.class), toJson);
    post("/admin/communities/:id/redistributions/:redistributionId", injector.getInstance(UpdateRedistributionController.class), toJson);
    post("/admin/communities/:id/redistributions/:redistributionId/delete", injector.getInstance(DeleteRedistributionController.class), toJson);
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
