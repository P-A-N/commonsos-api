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

import commonsos.EntityManagerService;
import commonsos.GsonProvider;
import commonsos.Server;
import commonsos.TransactionInterceptor;
import commonsos.service.ImageService;
import commonsos.service.PushNotificationService;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;

public class TestServer extends Server {
  
  @Override
  protected Injector initDependencies() {
    Web3j web3j = mock(Web3j.class);
    PushNotificationService pushNotificationService = mock(PushNotificationService.class);
    ImageService imageService = mock(ImageService.class);
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
