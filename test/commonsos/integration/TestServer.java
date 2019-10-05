package commonsos.integration;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import commonsos.Server;
import commonsos.command.app.UploadPhotoCommand;
import commonsos.di.GsonProvider;
import commonsos.di.Web3jProvider;
import commonsos.interceptor.ControllerInterceptor;
import commonsos.interceptor.SyncServiceInterceptor;
import commonsos.repository.EntityManagerService;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.blockchain.EthBalance;
import commonsos.service.blockchain.TokenBalance;
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
    TokenBalance tokenBalance = new TokenBalance().setBalance(TEN).setToken(new CommunityToken().setTokenName("name").setTokenSymbol("symbol").setTotalSupply(ONE));
    when(blockchainService.getTokenBalance(any(User.class), any(Long.class))).thenAnswer(new Answer<TokenBalance>() {
      @Override public TokenBalance answer(InvocationOnMock invocation) throws Throwable {
        return tokenBalance.setCommunityId((Long) invocation.getArgument(1));
      }
    });
    when(blockchainService.getTokenBalance(any(Community.class), any(WalletType.class))).thenAnswer(new Answer<TokenBalance>() {
      @Override public TokenBalance answer(InvocationOnMock invocation) throws Throwable {
        return tokenBalance.setCommunityId(((Community) invocation.getArgument(0)).getId());
      }
    });
    when(blockchainService.getTokenBalance(any(Long.class), any(WalletType.class))).thenAnswer(new Answer<TokenBalance>() {
      @Override public TokenBalance answer(InvocationOnMock invocation) throws Throwable {
        return tokenBalance.setCommunityId(invocation.getArgument(0));
      }
    });
    EthBalance ethBalance = new EthBalance().setBalance(TEN);
    when(blockchainService.getEthBalance(any(Community.class))).thenAnswer(new Answer<EthBalance>() {
      @Override public EthBalance answer(InvocationOnMock invocation) throws Throwable {
        return ethBalance.setCommunityId(((Community) invocation.getArgument(0)).getId());
      }
    });
    when(blockchainService.getBalance(any())).thenReturn(TEN.pow(18+6));
    when(blockchainService.getSystemEthBalance()).thenReturn(TEN);
    when(blockchainService.transferTokens(any(), any(), any(), any())).thenReturn("0x1");
    when(blockchainService.transferEther(any(Credentials.class), any(String.class), any(BigInteger.class), any(boolean.class))).thenReturn("0x1");
    when(blockchainService.transferEther(any(Credentials.class), any(String.class), any(BigDecimal.class), any(boolean.class))).thenReturn("0x1");
    when(blockchainService.createToken(any(Credentials.class), any(), any())).thenReturn("0x1");
    when(blockchainService.isConnected()).thenReturn(true);
    when(blockchainService.createWallet(any())).thenReturn("wallet");
    when(blockchainService.getCommunityToken(any())).thenReturn(tokenBalance.getToken());
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials(any(), any())).thenReturn(credentials);
    when(blockchainService.systemCredentials()).thenReturn(credentials);
    
    
    // s3
    ImageUploadService imageUploadService = mock(ImageUploadService.class);
    when(imageUploadService.create(any(InputStream.class), any(String.class))).thenReturn("http://test.com/ad/photo");
    when(imageUploadService.create(any(File.class), any(String.class))).thenReturn("http://test.com/ad/photo");
    when(imageUploadService.create(any(UploadPhotoCommand.class), any(String.class))).thenReturn("http://test.com/ad/photo");
    
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
          bind(ImageUploadService.class).toInstance(imageUploadService);
        }
        
        bind(PushNotificationService.class).toInstance(pushNotificationService);
        bind(EntityManagerService.class).to(TestEntityManagerService.class);
        bind(EmailService.class).to(TestEmailService.class);
      }
    };

    Injector injector = Guice.createInjector(module,
        new ControllerInterceptor(),
        new SyncServiceInterceptor());
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
