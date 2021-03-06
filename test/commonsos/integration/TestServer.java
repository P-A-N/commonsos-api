package commonsos.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
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
import commonsos.di.Web3jProvider;
import commonsos.interceptor.TransactionInterceptor;
import commonsos.repository.EntityManagerService;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageUploadService;
import commonsos.service.notification.PushNotificationService;
import spark.Spark;

public class TestServer extends Server {

  public static final int TEST_SERVER_PORT = 4568;
  
  private BlockchainService blockchainService;
  
  private boolean blockchainEnable = false;
  private boolean imageuploadEnable = false;
  
  @Override
  protected void setupServer() {
    super.setupServer();
    Spark.port(TEST_SERVER_PORT);
  }
  
  @Override
  protected Injector initDependencies() {
    // ethereum
    Web3j web3j = mock(Web3j.class);
    BlockchainEventService blockchainEventService = mock(BlockchainEventService.class);
    BlockchainService blockchainService = mock(BlockchainService.class);
    when(blockchainService.tokenBalance(any(), any())).thenReturn(BigDecimal.TEN);
    when(blockchainService.transferTokens(any(), any(), any(), any())).thenReturn("0x1");
    when(blockchainService.isConnected()).thenReturn(true);
    when(blockchainService.createWallet(any())).thenReturn("wallet");
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials(any(), any())).thenReturn(credentials);
    
    // s3
    ImageUploadService imageService = mock(ImageUploadService.class);
    when(imageService.create(any(InputStream.class))).thenReturn("http://test.com/ad/photo");
    
    // firebase
    PushNotificationService pushNotificationService = mock(PushNotificationService.class);
    
    Module module = new AbstractModule() {
      @Override protected void configure() {
        bind(Gson.class).toProvider(GsonProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        
        if (blockchainEnable) {
          bind(Web3j.class).toProvider(Web3jProvider.class);
        } else {
          bind(Web3j.class).toInstance(web3j);
          bind(BlockchainEventService.class).toInstance(blockchainEventService);
          bind(BlockchainService.class).toInstance(blockchainService);
        }

        if (!imageuploadEnable) {
          bind(ImageUploadService.class).toInstance(imageService);
        }
        
        bind(PushNotificationService.class).toInstance(pushNotificationService);
        bind(EntityManagerService.class).to(TestEntityManagerService.class);
        bind(EmailService.class).to(TestEmailService.class);
      }
    };

    Injector injector = Guice.createInjector(module, new TransactionInterceptor());
    injector.injectMembers(this);
    
    this.blockchainService = injector.getInstance(BlockchainService.class);
    checkBlockchainIsConnected();
    
    return injector;
  }

  public BlockchainService getBlockchainService() {
    return blockchainService;
  }
  
  public void setBlockchainEnable(boolean blockchainEnable) {
    this.blockchainEnable = blockchainEnable;
  }

  public void setImageuploadEnable(boolean imageuploadEnable) {
    this.imageuploadEnable = imageuploadEnable;
  }
  
  private void checkBlockchainIsConnected() {
    if (blockchainEnable && !blockchainService.isConnected()) throw new RuntimeException("blockchain is not connected.");
  }

  public static void main(String[] args) {
    new TestServer().start(args);
  }
}
