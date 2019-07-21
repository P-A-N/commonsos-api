package commonsos.service.blockchain;

import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;
import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH;
import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_FREQUENCY;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Files;

import com.fasterxml.jackson.databind.ObjectMapper;

import commonsos.Cache;
import commonsos.Configuration;
import commonsos.exception.DisplayableException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class BlockchainService {

  public static final BigInteger ETHER_TRANSFER_GAS_LIMIT = new BigInteger("21000");
  public static final BigInteger TOKEN_TRANSFER_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_TRANSFER_FROM_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_APPROVE_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_DEPLOYMENT_GAS_LIMIT = new BigInteger("4700000");
  public static final BigInteger GAS_PRICE = new BigInteger("18000000000");

  private static final int NUMBER_OF_DECIMALS = 18;
  private static final BigInteger MAX_UINT_256 = new BigInteger("2").pow(256);
  public static final BigInteger INITIAL_TOKEN_AMOUNT = MAX_UINT_256.divide(TEN.pow(NUMBER_OF_DECIMALS)).subtract(ONE);

  @Inject CommunityRepository communityRepository;
  @Inject ObjectMapper objectMapper;
  @Inject Web3j web3j;
  @Inject Cache cache;
  @Inject Configuration config;

  public boolean isConnected() {
    try {
      web3j.ethBlockNumber().send();
      return true;
    }
    catch (Exception e) {
      log.warn("Blockchain error "+ e.getMessage());
      return false;
    }
  }

  public String createWallet(String password) {
    File filePath = null;
    try {
      File tmp = tempDir();
      String fileName = WalletUtils.generateFullNewWalletFile(password, tmp);

      filePath = Paths.get(tmp.getAbsolutePath(), fileName).toFile();
      return Files.readString(filePath);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    } finally {
      if (filePath != null) filePath.delete();
    }
  }

  File tempDir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  public Credentials credentials(String wallet, String password) {
    try {
      WalletFile walletFile = objectMapper.readValue(wallet, WalletFile.class);
      ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
      return Credentials.create(keyPair);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }

  public String transferTokens(User remitter, User beneficiary, Long communityId, BigDecimal amount) {
    Community community = communityRepository.findStrictById(communityId);
    User admin = community.getAdminUser();
    
    log.info(format("Creating token transaction from %s to %s amount %.0f contract %s", remitter.getWalletAddress(), beneficiary.getWalletAddress(), amount, community.getTokenContractAddress()));
    
    Credentials credentials = credentials(admin.getWallet(), config.walletPassword());
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        GAS_PRICE,
        TOKEN_TRANSFER_FROM_GAS_LIMIT);
    
    TransactionReceipt receipt = handleBlockchainException(() -> {
      return token.transferFrom(remitter.getWalletAddress(), beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });
    
    log.info(format("Token transaction sent, hash %s", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  private BigInteger toTokensWithoutDecimals(BigDecimal amount) {
    return amount.multiply(BigDecimal.TEN.pow(NUMBER_OF_DECIMALS)).toBigIntegerExact();
  }

  private BigDecimal toTokensWithDecimals(BigInteger amount) {
    return new BigDecimal(amount).divide(BigDecimal.TEN.pow(NUMBER_OF_DECIMALS));
  }

  Token loadToken(Credentials remitterCredentials, String tokenContractAddress, BigInteger gasPrice, BigInteger gasLimit) {
    TransactionManager transactionManager = new RawTransactionManager(web3j, remitterCredentials, ChainId.NONE, new NoOpProcessor(web3j));
    return Token.load(tokenContractAddress, web3j, transactionManager, new StaticGasProvider(GAS_PRICE, TOKEN_TRANSFER_GAS_LIMIT));
  }

  Token loadTokenReadOnly(String walletAddress, String tokenContractAddress) {
    return Token.load(tokenContractAddress, web3j, new ReadonlyTransactionManager(web3j, walletAddress), new StaticGasProvider(GAS_PRICE, TOKEN_TRANSFER_GAS_LIMIT));
  }

  public void transferEther(User remitter, String beneficiaryAddress, BigInteger amount) {
    Credentials credentials = credentials(remitter.getWallet(), config.walletPassword());
    transferEther(credentials, beneficiaryAddress, amount);
  }

  public void transferEther(Credentials credentials, String beneficiaryAddress, BigInteger amount) {
    log.info(String.format("transferEther %d to %s", amount, beneficiaryAddress));
    TransactionReceipt receipt = handleBlockchainException(() -> {
      TransactionManager tm = new RawTransactionManager(web3j, credentials, ChainId.NONE, new PollingTransactionReceiptProcessor(web3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH ));
      Transfer transfer = new Transfer(web3j, tm);
      return transfer.sendFunds(beneficiaryAddress, new BigDecimal(amount), Unit.WEI).send();
    });
    log.info(String.format("Ether transaction receipt received for %s", receipt.getTransactionHash()));
  }

  private <T> T handleBlockchainException(Callable<T> callable) {
    try {
      return callable.call();
    }
    catch (Exception e) {
      if (e.getMessage().contains("insufficient funds for gas"))
        throw new DisplayableException("error.outOfEther");
      throw new RuntimeException(e);
    }
  }

  public String createToken(User owner, String symbol, String name) {
    return handleBlockchainException(() -> {
      Credentials credentials = credentials(owner.getWallet(), config.walletPassword());
      log.info("Deploying token contract: " + name + " (" + symbol + "), owner: " + owner.getWalletAddress());
      Token token = handleBlockchainException(() -> {
        return Token.deploy(
            web3j,
            credentials,
            new StaticGasProvider(GAS_PRICE, TOKEN_DEPLOYMENT_GAS_LIMIT),
            name,
            symbol,
            INITIAL_TOKEN_AMOUNT).send();
      });
      if (!token.isValid()) {
        if (token.getTransactionReceipt().isPresent()) {
          throw new RuntimeException("Deploying token contract " + token.getTransactionReceipt().get().getTransactionHash() + " failed");
        } else {
          throw new RuntimeException("Deploying token contract " + token.getContractAddress() + " failed");
        }
      }
      
      log.info("Deploy successful, contract address: " + token.getContractAddress());
      return token.getContractAddress();
    });
  }

  public BigDecimal tokenBalance(User user, Long communityId) {
    try {
      log.info("Token balance request for: " + user.getWalletAddress());
      Community community = communityRepository.findStrictById(communityId);
      Token token = loadTokenReadOnly(user.getWalletAddress(), community.getTokenContractAddress());
      BigInteger balance = token.balanceOf(user.getWalletAddress()).send();
      log.info("Token balance request complete, balance " + balance.toString());
      return toTokensWithDecimals(balance);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BigDecimal tokenTotalSupply(Long communityId) {
    BigDecimal totalSupply = cache.getTokenTotalSupply(communityId);
    
    if (totalSupply == null) {
      totalSupply = getTokenTotalSupplyFromBlockchain(communityId);
    }
    
    return totalSupply;
  }
  
  public String tokenName(Long communityId) {
    String tokenName = cache.getTokenName(communityId);
    
    if (tokenName == null) {
      tokenName = getTokenNameFromBlockchain(communityId);
    }
    
    return tokenName;
  }
  
  public String tokenSymbol(Long communityId) {
    String tokenSymbol = cache.getTokenSymbol(communityId);
    
    if (tokenSymbol == null) {
      tokenSymbol = getTokenSymbolFromBlockchain(communityId);
    }
    
    return tokenSymbol;
  }

  private synchronized BigDecimal getTokenTotalSupplyFromBlockchain(Long communityId) {
    BigDecimal totalSupply = cache.getTokenTotalSupply(communityId);
    if (totalSupply != null) return totalSupply;

    try {
      log.info(String.format("Token total supply request for: communityId=%d", communityId));
      Community community = communityRepository.findStrictById(communityId);
      Token token = loadTokenReadOnly(community.getAdminUser().getWalletAddress(), community.getTokenContractAddress());
      totalSupply = toTokensWithDecimals(token.totalSupply().send());
      log.info(String.format("Token total supply request complete: totalSupply=%d, communityId=%d", totalSupply, communityId));
      
      cache.setTokenTotalSupply(communityId, totalSupply);
      return totalSupply;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private synchronized String getTokenNameFromBlockchain(Long communityId) {
    String tokenName = cache.getTokenName(communityId);
    if (tokenName != null) return tokenName;

    try {
      log.info(String.format("Token name request for: communityId=%d", communityId));
      Community community = communityRepository.findStrictById(communityId);
      Token token = loadTokenReadOnly(community.getAdminUser().getWalletAddress(), community.getTokenContractAddress());
      tokenName = token.name().send();
      log.info(String.format("Token name request complete: name=%s, communityId=%d", tokenName, communityId));
      
      cache.setTokenName(communityId, tokenName == null ? "" : tokenName);
      return tokenName;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private synchronized String getTokenSymbolFromBlockchain(Long communityId) {
    String tokenSymbol = cache.getTokenSymbol(communityId);
    if (tokenSymbol != null) return tokenSymbol;

    try {
      log.info(String.format("Token symbol request for: communityId=%d", communityId));
      Community community = communityRepository.findStrictById(communityId);
      Token token = loadTokenReadOnly(community.getAdminUser().getWalletAddress(), community.getTokenContractAddress());
      tokenSymbol = token.symbol().send();
      log.info(String.format("Token symbol request complete: symbol=%s, communityId=%d", tokenSymbol, communityId));
      
      cache.setTokenSymbol(communityId, tokenSymbol == null ? "" : tokenSymbol);
      return tokenSymbol;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
