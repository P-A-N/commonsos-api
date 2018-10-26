package commonsos.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import commonsos.Server;
import commonsos.di.GsonProvider;
import commonsos.interceptor.TransactionInterceptor;
import commonsos.repository.EntityManagerService;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.image.ImageService;
import commonsos.service.notification.PushNotificationService;
import spark.Spark;

public class TestServer extends Server {

  public static int TEST_SERVER_PORT = 4568;
  
  @Override
  protected void setupServer() {
    super.setupServer();
    Spark.port(TEST_SERVER_PORT);
  }
  
  @Override
  protected Injector initDependencies() {
    Web3j web3j = mock(Web3j.class);
    PushNotificationService pushNotificationService = mock(PushNotificationService.class);
    ImageService imageService = mock(ImageService.class);
    when(imageService.create(any())).thenReturn("http://test.com/ad/photo");
    BlockchainEventService blockchainEventService = mock(BlockchainEventService.class);
    
    BlockchainService blockchainService = mock(BlockchainService.class);
    when(blockchainService.tokenBalance(any(), any())).thenReturn(BigDecimal.TEN);
    when(blockchainService.transferTokens(any(), any(), any(), any())).thenReturn("0x1");
    when(blockchainService.isConnected()).thenReturn(true);
    when(blockchainService.createWallet(any())).thenReturn("wallet");
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials(any(), any())).thenReturn(credentials);
    
    Module module = new AbstractModule() {
      @Override protected void configure() {
        bind(Gson.class).toProvider(GsonProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());

        bind(Web3j.class).toInstance(web3j);
        bind(PushNotificationService.class).toInstance(pushNotificationService);
        bind(ImageService.class).toInstance(imageService);
        bind(BlockchainEventService.class).toInstance(blockchainEventService);
        bind(BlockchainService.class).toInstance(blockchainService);
        bind(EntityManagerService.class).to(TestEntityManagerService.class);
      }
    };

    Injector injector = Guice.createInjector(module, new TransactionInterceptor());
    injector.injectMembers(this);
    return injector;
  }
  
  public static void main(String[] args) {
    new TestServer().start(args);
  }
}
