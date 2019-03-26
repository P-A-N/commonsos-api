package commonsos.service.blockchain;

import static commonsos.TestId.id;
import static commonsos.service.UserService.WALLET_PASSWORD;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import com.fasterxml.jackson.databind.ObjectMapper;

import commonsos.exception.DisplayableException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

@ExtendWith(MockitoExtension.class)
public class BlockchainServiceTest {

  @Mock CommunityRepository communityRepository;
  @Mock(answer = RETURNS_DEEP_STUBS) Web3j web3j;
  @InjectMocks @Spy BlockchainService service;

  @Test
  public void createWallet() throws IOException, CipherException {
    String wallet = service.createWallet("secret");

    assertThat(credentials(wallet)).isNotNull();
  }

  private Credentials credentials(String wallet) throws CipherException, IOException {
    return Credentials.create(Wallet.decrypt("secret", new ObjectMapper().readValue(wallet, WalletFile.class)));
  }

  @Test
  public void credentials() {
    service.objectMapper = new ObjectMapper();
    String wallet = "{\"address\":\"116ca1e1cc960a033a613f442e3c1bfc91841521\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"a5433cfd68ab9c72aa0e6174a03b4c8093e71db9b6e6ac941d681a34119e2eb9\",\"cipherparams\":{\"iv\":\"ffc2a2393dfb60914ef723a4bb441297\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"e4a8e82817fddd7dcfa8fa87db947aedd6ab5d13a6d5522cf725c9dfbff855a9\"},\"mac\":\"ab674a8909c6e6a26ff7176f7f8a6708a2a621645341a7de7691ce9672893f8a\"},\"id\":\"98f240c5-aa47-42fd-9fab-6ba29722cb15\",\"version\":3}";

    Credentials credentials = service.credentials(wallet, "test");

    assertThat(credentials.getAddress()).isEqualTo("0x116ca1e1cc960a033a613f442e3c1bfc91841521");
  }

  @Test
  public void transferTokens_asAdmin() throws Exception {
    User remitter = new User().setId(id("remitter")).setWallet("remitter wallet");
    User beneficiary = new User().setWalletAddress("beneficiary address");
    when(communityRepository.isAdmin(remitter.getId(), id("community"))).thenReturn(true);

    Community community = new Community().setTokenContractAddress("contract address");
    when(communityRepository.findById(id("community"))).thenReturn(Optional.of(community));

    EthSendTransaction response = mock(EthSendTransaction.class);
    when(response.getTransactionHash()).thenReturn("transaction hash");

    Credentials credentials = mock(Credentials.class);
    doReturn(credentials).when(service).credentials("remitter wallet", WALLET_PASSWORD);
    doReturn(response).when(service).contractTransfer("contract address", credentials, "beneficiary address", new BigInteger("10000000000000000000"));


    String result = service.transferTokens(remitter, beneficiary, id("community"), TEN);


    assertThat(result).isEqualTo("transaction hash");
  }

  @Test
  public void signAndSend() throws IOException {
    Credentials credentials = mock(Credentials.class);
    RawTransaction rawTransaction = mock(RawTransaction.class);
    doReturn("signed message").when(service).signMessage(credentials, rawTransaction);
    EthSendTransaction response = mock(EthSendTransaction.class);
    when(response.hasError()).thenReturn(false);
    when(web3j.ethSendRawTransaction("signed message").send()).thenReturn(response);

    EthSendTransaction result = service.signAndSend(credentials, rawTransaction);

    assertThat(result).isSameAs(response);
  }

  @Test
  public void signAndSend_throwsExceptionInCaseOfReponseError() throws IOException {
    Credentials credentials = mock(Credentials.class);
    RawTransaction rawTransaction = mock(RawTransaction.class);
    doReturn("signed message").when(service).signMessage(credentials, rawTransaction);
    EthSendTransaction response = mock(EthSendTransaction.class, Mockito.RETURNS_DEEP_STUBS);
    when(response.hasError()).thenReturn(true);
    when(response.getError().getMessage()).thenReturn("blockchain error");
    when(web3j.ethSendRawTransaction("signed message").send()).thenReturn(response);

    RuntimeException thrown = catchThrowableOfType(() -> {
      service.signAndSend(credentials, rawTransaction);
    }, RuntimeException.class);

    assertThat(thrown).hasMessage("blockchain error");
  }

  @Test
  public void transferTokens_asRegularUser() {
    User remitter = new User().setId(id("remitter")).setWallet("remitter wallet").setWalletAddress("remitter address");
    User beneficiary = new User().setWalletAddress("beneficiary address");
    when(communityRepository.isAdmin(remitter.getId(), id("community"))).thenReturn(false);

    User walletUser = new User().setWalletAddress("admin wallet address").setWallet("admin wallet");
    Community community = new Community().setId(id("community")).setTokenContractAddress("contract address").setAdminUser(walletUser);
    when(communityRepository.findById(id("community"))).thenReturn(Optional.of(community));

    EthSendTransaction response = mock(EthSendTransaction.class);
    when(response.hasError()).thenReturn(false);
    when(response.getTransactionHash()).thenReturn("transaction hash");

    Credentials credentials = mock(Credentials.class);
    doReturn(credentials).when(service).credentials("admin wallet", WALLET_PASSWORD);
    doReturn(response).when(service).contractTransferFrom(credentials, "contract address", "remitter address", "beneficiary address", new BigInteger("10000000000000000000"));

    String result = service.transferTokens(remitter, beneficiary, id("community"), TEN);

    assertThat(result).isEqualTo("transaction hash");
  }

  @Test
  public void transferTokens_asRegularUser_fails() {
    User remitter = new User().setWalletAddress("remitter address");
    User beneficiary = new User().setWalletAddress("beneficiary address");

    User walletUser = new User().setWalletAddress("admin wallet address").setWallet("admin wallet");
    Community community = new Community().setId(id("community")).setTokenContractAddress("contract address").setAdminUser(walletUser);
    when(communityRepository.findById(id("community"))).thenReturn(Optional.of(community));


    EthSendTransaction response = mock(EthSendTransaction.class, Mockito.RETURNS_DEEP_STUBS);
    when(response.hasError()).thenReturn(true);
    when(response.getError().getMessage()).thenReturn("blockchain error");

    Credentials credentials = mock(Credentials.class);
    doReturn(credentials).when(service).credentials("admin wallet", WALLET_PASSWORD);
    doReturn(response).when(service).contractTransferFrom(any(), any(), any(), any(), any());


    RuntimeException thrown = catchThrowableOfType(
      ()-> service.transferTokens(remitter, beneficiary, id("community"), TEN),
      RuntimeException.class);
    assertThat(thrown).hasMessage("Error processing transaction request: blockchain error");
  }

  @Test
  public void handleBlockchainException_noError() {
    Callable<String> action = () -> "result";

    String result = service.handleBlockchainException(action);

    assertThat(result).isEqualTo("result");
  }

  @Test
  public void handleBlockchainException_error() {
    Callable<Void> action = () -> {throw new RuntimeException("insufficient funds for gas");};

    assertThrows(DisplayableException.class, () -> service.handleBlockchainException(action));
  }

  @Test
  public void handleBlockchainException_passesThroughRandomError() {
    Callable<Void> action = () -> {throw new RuntimeException();};

    assertThrows(RuntimeException.class, () -> service.handleBlockchainException(action));
  }
}