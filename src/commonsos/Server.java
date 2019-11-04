package commonsos;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import java.util.List;

import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import commonsos.controller.JsonTransformer;
import commonsos.controller.PreflightController;
import commonsos.controller.admin.admin.CreateAdminCompleteController;
import commonsos.controller.admin.admin.CreateAdminTemporaryController;
import commonsos.controller.admin.admin.DeleteAdminController;
import commonsos.controller.admin.admin.GetAdminController;
import commonsos.controller.admin.admin.SearchAdminsController;
import commonsos.controller.admin.admin.UpdateAdminController;
import commonsos.controller.admin.admin.UpdateAdminEmailCompleteController;
import commonsos.controller.admin.admin.UpdateAdminEmailTemporaryController;
import commonsos.controller.admin.admin.UpdateAdminPhotoController;
import commonsos.controller.admin.ads.GetAdByAdminController;
import commonsos.controller.admin.ads.SearchAdsByAdminController;
import commonsos.controller.admin.auth.AdminLoginController;
import commonsos.controller.admin.auth.AdminLogoutController;
import commonsos.controller.admin.community.CreateCommunityController;
import commonsos.controller.admin.community.DeleteCommunityController;
import commonsos.controller.admin.community.GetCommunityController;
import commonsos.controller.admin.community.SearchCommunityByAdminController;
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
import commonsos.controller.admin.system.GetMaintenanceModeController;
import commonsos.controller.admin.system.UpdateMaintenanceModeController;
import commonsos.controller.admin.transaction.CreateEthTransactionController;
import commonsos.controller.admin.transaction.CreateTokenTransactionFromAdminController;
import commonsos.controller.admin.transaction.GetEthBalanceController;
import commonsos.controller.admin.transaction.GetTokenBalanceController;
import commonsos.controller.admin.transaction.SearchCommunityTokenTransactionsController;
import commonsos.controller.admin.transaction.SearchEthBalanceHistoriesController;
import commonsos.controller.admin.user.DeleteUserByAdminController;
import commonsos.controller.admin.user.GetUserByAdminController;
import commonsos.controller.admin.user.SearchUserTransactionsByAdminController;
import commonsos.controller.admin.user.SearchUsersByAdminController;
import commonsos.controller.admin.user.UpdateUserByAdminController;
import commonsos.controller.admin.user.UpdateUserCommunitiesByAdminController;
import commonsos.controller.admin.user.UpdateUserEmailTemporaryByAdminController;
import commonsos.controller.admin.user.UpdateUserNameByAdminController;
import commonsos.controller.app.GetAppApiVersionController;
import commonsos.controller.app.NoVersionRequestController;
import commonsos.controller.app.ad.CreateAdController;
import commonsos.controller.app.ad.DeleteAdByAdminUserController;
import commonsos.controller.app.ad.DeleteAdController;
import commonsos.controller.app.ad.GetAdController;
import commonsos.controller.app.ad.SearchAdController;
import commonsos.controller.app.ad.SearchMyAdsController;
import commonsos.controller.app.ad.UpdateAdController;
import commonsos.controller.app.ad.UpdateAdPhotoController;
import commonsos.controller.app.auth.AppLoginController;
import commonsos.controller.app.auth.AppLogoutController;
import commonsos.controller.app.auth.PasswordResetController;
import commonsos.controller.app.auth.PasswordResetRequestCheckController;
import commonsos.controller.app.auth.PasswordResetRequestController;
import commonsos.controller.app.community.SearchCommunityByUserController;
import commonsos.controller.app.community.SearchCommunityNotificationController;
import commonsos.controller.app.community.UpdateCommunityCoverPhotoByAdminUserController;
import commonsos.controller.app.community.UpdateCommunityPhotoByAdminUserController;
import commonsos.controller.app.message.CreateAdMessageThreadIfNotExistsController;
import commonsos.controller.app.message.CreateDirectMessageThreadIfNotExistsController;
import commonsos.controller.app.message.CreateGroupMessageThreadIfNotExistsController;
import commonsos.controller.app.message.CreateMessageController;
import commonsos.controller.app.message.GetMessageThreadController;
import commonsos.controller.app.message.GetMessageThreadUnreadCountController;
import commonsos.controller.app.message.SearchMessageController;
import commonsos.controller.app.message.SearchMessageThreadController;
import commonsos.controller.app.message.UnsubscribeMessageThreadController;
import commonsos.controller.app.message.UpdateGroupMessageThreadController;
import commonsos.controller.app.message.UpdateMessageThreadPersonalTitleController;
import commonsos.controller.app.message.UpdateMessageThreadPhotoController;
import commonsos.controller.app.transaction.AdminTransactionListController;
import commonsos.controller.app.transaction.CreateTokenTransactionFromUserController;
import commonsos.controller.app.transaction.GetUserTokenBalanceController;
import commonsos.controller.app.transaction.SearchTokenTransactionController;
import commonsos.controller.app.user.CreateUserCompleteController;
import commonsos.controller.app.user.CreateUserTemporaryController;
import commonsos.controller.app.user.DeleteUserController;
import commonsos.controller.app.user.GetTransactionQrCodeController;
import commonsos.controller.app.user.GetUserController;
import commonsos.controller.app.user.SearchUserController;
import commonsos.controller.app.user.SearchUsersCommunityController;
import commonsos.controller.app.user.UpdateAdLastViewTimeController;
import commonsos.controller.app.user.UpdateEmailTemporaryController;
import commonsos.controller.app.user.UpdateNotificationLastViewTimeController;
import commonsos.controller.app.user.UpdateUserAvatarController;
import commonsos.controller.app.user.UpdateUserCommunitiesController;
import commonsos.controller.app.user.UpdateUserController;
import commonsos.controller.app.user.UpdateUserEmailCompleteController;
import commonsos.controller.app.user.UpdateUserMobileDeviceController;
import commonsos.controller.app.user.UpdateUserNameController;
import commonsos.controller.app.user.UpdateUserStatusController;
import commonsos.controller.app.user.UpdateWalletLastViewTimeController;
import commonsos.controller.app.user.UserPasswordResetRequestController;
import commonsos.controller.batch.redistribution.RedistributionBatchController;
import commonsos.controller.wordpress.community.SearchCommunityFromWPController;
import commonsos.controller.wordpress.community.SearchCommunityNotificationFromWPController;
import commonsos.controller.wordpress.community.UpdateCommunityNotificationFromWPController;
import commonsos.di.GsonProvider;
import commonsos.di.Web3jProvider;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.exception.ServiceUnavailableException;
import commonsos.exception.UrlNotFoundException;
import commonsos.filter.AddHeaderFilter;
import commonsos.filter.LogFilter;
import commonsos.interceptor.ControllerInterceptor;
import commonsos.interceptor.SyncServiceInterceptor;
import commonsos.repository.CommunityRepository;
import commonsos.repository.DatabaseMigrator;
import commonsos.repository.entity.Community;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.multithread.InitCommunityCacheTask;
import commonsos.service.multithread.TaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;

@Slf4j
public class Server {

  @Inject private JsonTransformer toJson;
  @Inject private DatabaseMigrator databaseMigrator;
  @Inject private BlockchainEventService blockchainEventService;
  @Inject private CommunityRepository communityRepository;
  @Inject private TaskExecutorService taskExecutorService;
  @Inject private Configuration config;
  @Inject private Cache cache;
  private Injector injector;

  public void start(String[] args) {
    injector = initDependencies();
    databaseMigrator.execute();
    CookieSecuringEmbeddedJettyFactory.register();
    setupServer();
    initCache();
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

    Injector injector = Guice.createInjector(module,
        new ControllerInterceptor(),
        new SyncServiceInterceptor());
    injector.injectMembers(this);
    return injector;
  }
  
  private void initCache() {
    List<Community> communityList = communityRepository.list(null).getList();
    communityList.forEach(c -> {
      InitCommunityCacheTask task = new InitCommunityCacheTask(c);
      taskExecutorService.execute(task);
    });

    cache.setSystemConfig(Cache.SYS_CONFIG_KEY_MAINTENANCE_MODE, config.maintenanceMode());
  }

  private void initRoutes() {
    before(injector.getInstance(AddHeaderFilter.class));
    before(injector.getInstance(LogFilter.class));
    before((request, response) -> log.info(requestInfo(request)));

    initAppRoutes();
    initWordPressRoutes();
    initAdminPageRoutes();
    initBatchRoutes();
    
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
    exception(UrlNotFoundException.class, (exception, request, response) -> {
      log.error("Url not found", exception);
      response.status(404);
      response.body("");
    });
    exception(DisplayableException.class, (exception, request, response) -> {
      log.error("Displayable error", exception);
      response.status(468);
      response.body(toJson.render(ImmutableMap.of("key", exception.getMessage())));
    });
    exception(ServiceUnavailableException.class, (exception, request, response) -> {
      log.error("Service unavailable", exception);
      response.status(503);
      response.body("");
    });
    exception(Exception.class, (exception, request, response) -> {
      log.error("Processing failed", exception);
      response.status(500);
      response.body("");
    });
  }

  private void initAppRoutes() {
    get("/app/version", injector.getInstance(GetAppApiVersionController.class), toJson);
    post("/app/:version/login", injector.getInstance(AppLoginController.class), toJson);
    post("/app/:version/logout", injector.getInstance(AppLogoutController.class), toJson);
    post("/app/:version/create-account", injector.getInstance(CreateUserTemporaryController.class), toJson);
    post("/app/:version/create-account/:accessId", injector.getInstance(CreateUserCompleteController.class), toJson);
    get("/app/:version/user", injector.getInstance(GetUserController.class), toJson);
    get("/app/:version/users/:id", injector.getInstance(GetUserController.class), toJson);
    post("/app/:version/users/:id", injector.getInstance(UpdateUserController.class), toJson);
    post("/app/:version/users/:id/username", injector.getInstance(UpdateUserNameController.class), toJson);
    post("/app/:version/users/:id/status", injector.getInstance(UpdateUserStatusController.class), toJson);
    post("/app/:version/users/:id/passwordreset", injector.getInstance(UserPasswordResetRequestController.class), toJson);
    post("/app/:version/users/:id/delete", injector.getInstance(DeleteUserController.class), toJson);
    post("/app/:version/users/:id/avatar", injector.getInstance(UpdateUserAvatarController.class), toJson);
    get("/app/:version/users/:id/qr", injector.getInstance(GetTransactionQrCodeController.class), toJson);
    post("/app/:version/users/:id/mobile-device", injector.getInstance(UpdateUserMobileDeviceController.class), toJson);
    get("/app/:version/users", injector.getInstance(SearchUserController.class), toJson);
    get("/app/:version/users/:id/communities", injector.getInstance(SearchUsersCommunityController.class), toJson);
    post("/app/:version/users/:id/communities", injector.getInstance(UpdateUserCommunitiesController.class), toJson);
    post("/app/:version/users/:id/emailaddress", injector.getInstance(UpdateEmailTemporaryController.class), toJson);
    post("/app/:version/users/:id/emailaddress/:accessId", injector.getInstance(UpdateUserEmailCompleteController.class), toJson);
    post("/app/:version/users/:id/wallet/lastViewTime", injector.getInstance(UpdateWalletLastViewTimeController.class), toJson);
    post("/app/:version/users/:id/ad/lastViewTime", injector.getInstance(UpdateAdLastViewTimeController.class), toJson);
    post("/app/:version/users/:id/notification/lastViewTime", injector.getInstance(UpdateNotificationLastViewTimeController.class), toJson);
    post("/app/:version/passwordreset", injector.getInstance(PasswordResetRequestController.class), toJson);
    get("/app/:version/passwordreset/:accessId", injector.getInstance(PasswordResetRequestCheckController.class), toJson);
    post("/app/:version/passwordreset/:accessId", injector.getInstance(PasswordResetController.class), toJson);

    post("/app/:version/ads", injector.getInstance(CreateAdController.class), toJson);
    get("/app/:version/ads", injector.getInstance(SearchAdController.class), toJson);
    get("/app/:version/ads/:id", injector.getInstance(GetAdController.class), toJson);
    post("/app/:version/ads/:id", injector.getInstance(UpdateAdController.class), toJson);
    post("/app/:version/ads/:id/delete", injector.getInstance(DeleteAdController.class), toJson);
    post("/app/:version/ads/:id/photo", injector.getInstance(UpdateAdPhotoController.class), toJson);
    get("/app/:version/my-ads", injector.getInstance(SearchMyAdsController.class), toJson);

    get("/app/:version/balance", injector.getInstance(GetUserTokenBalanceController.class), toJson);
    get("/app/:version/transactions", injector.getInstance(SearchTokenTransactionController.class), toJson);
    post("/app/:version/transactions", injector.getInstance(CreateTokenTransactionFromUserController.class), toJson);

    post("/app/:version/message-threads/for-ad/:adId", injector.getInstance(CreateAdMessageThreadIfNotExistsController.class), toJson);
    post("/app/:version/message-threads/user/:userId", injector.getInstance(CreateDirectMessageThreadIfNotExistsController.class), toJson);

    post("/app/:version/message-threads/group", injector.getInstance(CreateGroupMessageThreadIfNotExistsController.class), toJson);
    post("/app/:version/message-threads/:id/group", injector.getInstance(UpdateGroupMessageThreadController.class), toJson);
    post("/app/:version/message-threads/:id/title", injector.getInstance(UpdateMessageThreadPersonalTitleController.class), toJson);
    post("/app/:version/message-threads/:id/photo", injector.getInstance(UpdateMessageThreadPhotoController.class), toJson);
    post("/app/:version/message-threads/:id/unsubscribe", injector.getInstance(UnsubscribeMessageThreadController.class), toJson);

    get("/app/:version/message-threads/unread-count", injector.getInstance(GetMessageThreadUnreadCountController.class), toJson);
    get("/app/:version/message-threads/:id", injector.getInstance(GetMessageThreadController.class), toJson);
    get("/app/:version/message-threads", injector.getInstance(SearchMessageThreadController.class), toJson);
    post("/app/:version/message-threads/:id/messages", injector.getInstance(CreateMessageController.class), toJson);
    get("/app/:version/message-threads/:id/messages", injector.getInstance(SearchMessageController.class), toJson);

    get("/app/:version/communities", injector.getInstance(SearchCommunityByUserController.class), toJson);
    post("/app/:version/communities/:id/photo", injector.getInstance(UpdateCommunityPhotoByAdminUserController.class), toJson);
    post("/app/:version/communities/:id/coverPhoto", injector.getInstance(UpdateCommunityCoverPhotoByAdminUserController.class), toJson);
    get("/app/:version/communities/:id/notification", injector.getInstance(SearchCommunityNotificationController.class), toJson);

    // TODO change path
    post("/app/:version/admin/ads/:id/delete", injector.getInstance(DeleteAdByAdminUserController.class), toJson);
    // TODO delete
    get("/app/:version/admin/transactions", injector.getInstance(AdminTransactionListController.class), toJson);

    // TODO for no version api. delete it in the future
    post("/login", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/logout", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/create-account", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/create-account/*", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/user", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/users", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/users/*", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/users/*", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/password*", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/password*", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/ads", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/ads", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/ads/*", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/ads/*", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/my-ads", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/balance", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/transactions", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/transactions", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/message-threads", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/message-threads/*", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/message-threads/*", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/communities", injector.getInstance(NoVersionRequestController.class), toJson);
    get("/communities/*", injector.getInstance(NoVersionRequestController.class), toJson);
    post("/communities/*", injector.getInstance(NoVersionRequestController.class), toJson);
  }

  private void initWordPressRoutes() {
    get("/wordpress/communities", injector.getInstance(SearchCommunityFromWPController.class), toJson);
    post("/wordpress/communities/:id/notification/:wordpressId", injector.getInstance(UpdateCommunityNotificationFromWPController.class), toJson);
    get("/wordpress/communities/:id/notification", injector.getInstance(SearchCommunityNotificationFromWPController.class), toJson);
  }
  
  private void initAdminPageRoutes() {
    options("/admin/*", injector.getInstance(PreflightController.class));

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
    post("/admin/admins/:id/emailaddress/:accessId", injector.getInstance(UpdateAdminEmailCompleteController.class), toJson);
    post("/admin/admins/:id/delete", injector.getInstance(DeleteAdminController.class), toJson);

    // app users
    get("/admin/users", injector.getInstance(SearchUsersByAdminController.class), toJson);
    get("/admin/users/:id", injector.getInstance(GetUserByAdminController.class), toJson);
    post("/admin/users/:id", injector.getInstance(UpdateUserByAdminController.class), toJson);
    post("/admin/users/:id/username", injector.getInstance(UpdateUserNameByAdminController.class), toJson);
    post("/admin/users/:id/communities", injector.getInstance(UpdateUserCommunitiesByAdminController.class), toJson);
    post("/admin/users/:id/emailaddress", injector.getInstance(UpdateUserEmailTemporaryByAdminController.class), toJson);
    post("/admin/users/:id/delete", injector.getInstance(DeleteUserByAdminController.class), toJson);
    get("/admin/users/:id/transactions", injector.getInstance(SearchUserTransactionsByAdminController.class), toJson);

    // ads
    get("/admin/ads/:id", injector.getInstance(GetAdByAdminController.class), toJson);
    get("/admin/ads", injector.getInstance(SearchAdsByAdminController.class), toJson);

    // transactions
    post("/admin/transactions/coin", injector.getInstance(CreateTokenTransactionFromAdminController.class), toJson);
    get("/admin/transactions/coin", injector.getInstance(SearchCommunityTokenTransactionsController.class), toJson);
    get("/admin/transactions/coin/balance", injector.getInstance(GetTokenBalanceController.class), toJson);
    post("/admin/transactions/eth", injector.getInstance(CreateEthTransactionController.class), toJson);
    get("/admin/transactions/eth/balance", injector.getInstance(GetEthBalanceController.class), toJson);
    get("/admin/transactions/eth/balance/histories", injector.getInstance(SearchEthBalanceHistoriesController.class), toJson);
    
    // communities
    get("/admin/communities", injector.getInstance(SearchCommunityByAdminController.class), toJson);
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
    
    // system
    get("/admin/system/maintenance-mode", injector.getInstance(GetMaintenanceModeController.class), toJson);
    post("/admin/system/maintenance-mode", injector.getInstance(UpdateMaintenanceModeController.class), toJson);
  }

  private void initBatchRoutes() {
    post("/batch/redistribution", injector.getInstance(RedistributionBatchController.class), toJson);
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
