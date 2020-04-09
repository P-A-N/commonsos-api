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

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Files;

import com.fasterxml.jackson.databind.ObjectMapper;

import commonsos.Cache;
import commonsos.Configuration;
import commonsos.exception.DisplayableException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;
import commonsos.service.AbstractService;
import commonsos.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class BlockchainService extends AbstractService {

  public static final BigInteger ETHER_TRANSFER_GAS_LIMIT = new BigInteger("21000");
  public static final BigInteger TOKEN_TRANSFER_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_TRANSFER_FROM_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_APPROVE_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_SET_NAME_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_MINT_BURN_GAS_LIMIT = new BigInteger("90000");
  public static final BigInteger TOKEN_DEPLOYMENT_GAS_LIMIT = new BigInteger("4700000");

  public static final int NUMBER_OF_DECIMALS = 18;
  private static final BigInteger MAX_UINT_256 = new BigInteger("2").pow(256);
  public static final BigInteger INITIAL_TOKEN_AMOUNT = MAX_UINT_256.divide(TEN.pow(NUMBER_OF_DECIMALS)).subtract(ONE);

  private static int MAX_REPEAT_COUNT = 3;

  @Inject CommunityRepository communityRepository;
  @Inject TokenTransactionRepository tokenTransactionRepository;
  @Inject ObjectMapper objectMapper;
  @Inject Web3j web3j;
  @Inject Cache cache;
  @Inject Configuration config;

  public boolean isConnected() {
    try {
      web3j.ethBlockNumber().send();
      return true;
    } catch (Exception e) {
      log.warn("Blockchain error "+ e.getMessage());
      return false;
    }
  }

  public boolean isAllowed(User user, Community community, BigInteger floor) {
    log.info(String.format("Checking users allowance. communityId=%d, userId=%d, address=%s", community.getId(), user.getId(), user.getWalletAddress()));
    return isAllowed(user.getWalletAddress(), community, floor);
  }

  public boolean isAllowed(String address, Community community, BigInteger floor) {
    try {
      Token token = loadTokenReadOnly(community.getTokenContractAddress());
      BigInteger allowance = handle(() -> token.allowance(address, community.getMainWalletAddress()).send());
      log.info(String.format("Token allowance info. communityId=%d, address=%s, allowance=%d", community.getId(), address, allowance));
      return allowance.compareTo(floor) >= 0;
    } catch (Exception e) {
      throw new ServerErrorException(e);
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

  public Credentials systemCredentials() {
    try {
      return WalletUtils.loadCredentials(config.systemWalletPassword(), new File(config.systemWallet()));
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }

  public String transferTokensFromUserToUser(User remitter, User beneficiary, Long communityId, BigDecimal amount) {
    Community community = communityRepository.findStrictById(communityId);
    
    if (!isAllowed(remitter, community, toTokensWithoutDecimals(amount))) {
      String message = String.format(
          "community admin is not allowed to send token. [community=%s, user=%s]",
          community.getName(),
          remitter.getUsername());
      throw new ServerErrorException(message);
    }
    
    log.info(format("transfer token from %s(userId=%d, address=%s) to %s(userId=%d, address=%s). [communityId=%d amount=%.0f tokenContractAddress=%s]",
        remitter.getUsername(), remitter.getId(), remitter.getWalletAddress(),
        beneficiary.getUsername(), beneficiary.getId(), beneficiary.getWalletAddress(),
        community.getId(),
        amount,
        community.getTokenContractAddress()));
    
    Credentials credentials = credentials(community.getMainWallet(), config.communityWalletPassword());
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_TRANSFER_FROM_GAS_LIMIT);
    
    TransactionReceipt receipt = handle(() -> {
      return token.transferFrom(remitter.getWalletAddress(), beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });
    
    log.info(format("token sent. [transactionHash=%s]", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  public String transferTokensFee(User remitter, Long communityId, BigDecimal feeAmount) {
    Community community = communityRepository.findStrictById(communityId);
    
    if (!isAllowed(remitter, community, toTokensWithoutDecimals(feeAmount))) {
      String message = String.format(
          "community admin is not allowed to send token. [community=%s, user=%s]",
          community.getName(),
          remitter.getUsername());
      throw new ServerErrorException(message);
    }
    
    log.info(format("transfer token from %s(userId=%d, address=%s) to fee wallet(address=%s). [communityId=%d amount=%.0f tokenContractAddress=%s]",
        remitter.getUsername(), remitter.getId(), remitter.getWalletAddress(),
        community.getFeeWalletAddress(),
        community.getId(),
        feeAmount,
        community.getTokenContractAddress()));
    
    Credentials mainCredentials = credentials(community.getMainWallet(), config.communityWalletPassword());
    Token token = loadToken(
        mainCredentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_TRANSFER_FROM_GAS_LIMIT);
    
    TransactionReceipt receipt = handle(() -> {
      return token.transferFrom(remitter.getWalletAddress(), community.getFeeWalletAddress(), toTokensWithoutDecimals(feeAmount)).send();
    });

    log.info(format("token sent. [transactionHash=%s]", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  public String transferTokensFromCommunity(Community community, WalletType walletType, User beneficiary, BigDecimal amount) {
    log.info(format("Creating token transaction from community to user. [communityId=%d, wallet=%s, userId=%d, amount=%.0f, tokenContractAddress=%s]", community.getId(), walletType.name(), beneficiary.getId(), amount, community.getTokenContractAddress()));

    String hash;
    switch (walletType) {
    case MAIN:
      hash = transferTokensFromMainWallet(community, beneficiary, amount);
      break;
    case FEE:
      hash = transferTokensFromFeeWallet(community, beneficiary, amount);
      break;
    default:
      throw new ServerErrorException();
    }

    log.info(format("Creating token transaction finished. [transactionHash=%s]", hash));
    return hash;
  }

  private String transferTokensFromMainWallet(Community community, User beneficiary, BigDecimal amount) {
    Credentials credentials = credentials(community.getMainWallet(), config.communityWalletPassword());

    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_TRANSFER_GAS_LIMIT);

    TransactionReceipt receipt = handle(() -> {
      return token.transfer(beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });
    
    return receipt.getTransactionHash();
  }

  private String transferTokensFromFeeWallet(Community community, User beneficiary, BigDecimal amount) {
    Credentials feeCredentials = credentials(community.getFeeWallet(), config.communityWalletPassword());

    if (!isAllowed(feeCredentials.getAddress(), community, toTokensWithoutDecimals(amount))) {
      String message = String.format(
          "community admin is not allowed to send token. [community=%s, walletType=%s, address=%s]",
          community.getName(),
          WalletType.FEE.name(),
          feeCredentials.getAddress());
      throw new ServerErrorException(message);
    }

    Credentials mainCredentials = credentials(community.getMainWallet(), config.communityWalletPassword());
    Token token = loadToken(
        mainCredentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_TRANSFER_FROM_GAS_LIMIT);
    
    TransactionReceipt receipt = handle(() -> {
      return token.transferFrom(feeCredentials.getAddress(), beneficiary.getWalletAddress(), toTokensWithoutDecimals(amount)).send();
    });

    return receipt.getTransactionHash();
  }

  private BigInteger toTokensWithoutDecimals(BigDecimal amount) {
    return amount.multiply(BigDecimal.TEN.pow(NUMBER_OF_DECIMALS)).toBigIntegerExact();
  }

  private BigDecimal toTokensWithDecimals(BigInteger amount) {
    return new BigDecimal(amount).divide(BigDecimal.TEN.pow(NUMBER_OF_DECIMALS));
  }

  Token loadToken(Credentials remitterCredentials, String tokenContractAddress, BigInteger gasPrice, BigInteger gasLimit) {
    if (StringUtils.isEmpty(tokenContractAddress)) throw new DisplayableException("error.createTokenNotCompleted");
    
    TransactionManager transactionManager = new RawTransactionManager(web3j, remitterCredentials, ChainId.NONE, new PollingTransactionReceiptProcessor(web3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH)); // 15s * 40 = 10m
    return handle(() -> Token.load(tokenContractAddress, web3j, transactionManager, new StaticGasProvider(config.gasPrice(), TOKEN_TRANSFER_GAS_LIMIT)));
  }

  Token loadTokenReadOnly(String tokenContractAddress) {
    if (StringUtils.isEmpty(tokenContractAddress)) throw new DisplayableException("error.createTokenNotCompleted");
    
    String walletAddress = systemCredentials().getAddress();
    return handle(() -> Token.load(tokenContractAddress, web3j, new ReadonlyTransactionManager(web3j, walletAddress), new StaticGasProvider(config.gasPrice(), TOKEN_TRANSFER_GAS_LIMIT)));
  }

  public void transferEther(Community community, String beneficiaryAddress, BigInteger amount, boolean waitUntilCompleted) {
    Credentials credentials = credentials(community.getMainWallet(), config.communityWalletPassword());
    transferEther(credentials, beneficiaryAddress, amount, waitUntilCompleted);
  }

  public String transferEther(Credentials credentials, String beneficiaryAddress, BigDecimal amount, boolean waitUntilCompleted) {
    return transferEther(credentials, beneficiaryAddress, toTokensWithoutDecimals(amount), waitUntilCompleted);
  }

  public String transferEther(Credentials credentials, String beneficiaryAddress, BigInteger amount, boolean waitUntilCompleted) {
    TransactionReceiptProcessor trp;
    if (waitUntilCompleted) trp = new PollingTransactionReceiptProcessor(web3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH ); // 15s * 40 = 10m
    else trp = new NoOpProcessor(web3j);
    
    log.info(String.format("transferEther %d to %s", amount, beneficiaryAddress));
    TransactionReceipt receipt = handle(() -> {
      TransactionManager tm = new RawTransactionManager(web3j, credentials, ChainId.NONE, trp);
      Transfer transfer = new Transfer(web3j, tm);
      return transfer.sendFunds(beneficiaryAddress, new BigDecimal(amount), Unit.WEI).send();
    });
    log.info(String.format("Ether transaction receipt received for %s", receipt.getTransactionHash()));
    return receipt.getTransactionHash();
  }

  private <T> T handle(Callable<T> callable) {
    T result = null;
    for (int i = 0; i < MAX_REPEAT_COUNT; i++ ) {
      try {
        result = callable.call();
        break;
      } catch (Exception e) {
        if (e.getMessage().contains("Rate limit")) {
          log.error(String.format("Blockchain execution failed. [repeat_count=%d]", i), e);
          ThreadUtil.sleep(1000);
          continue;
        }
        if (e.getMessage().contains("insufficient funds for gas")) {
          throw DisplayableException.OUT_OF_ETHER;
        }
        throw new ServerErrorException(e);
      }
    }
    
    return result;
  }

  public String createToken(User owner, String symbol, String name) {
    Credentials credentials = credentials(owner.getWallet(), config.userWalletPassword());
    return createToken(credentials, symbol, name);
  }

  public String createToken(Credentials credentials, String symbol, String name) {
    log.info("Deploying token contract: " + name + " (" + symbol + "), owner: " + credentials.getAddress());
    Token token = handle(() -> {
      return Token.deploy(
          web3j,
          credentials,
          new StaticGasProvider(config.gasPrice(), TOKEN_DEPLOYMENT_GAS_LIMIT),
          name,
          symbol,
          INITIAL_TOKEN_AMOUNT).send();
    });
    if (!handle(() -> token.isValid())) {
      if (token.getTransactionReceipt().isPresent()) {
        throw new ServerErrorException("Deploying token contract " + token.getTransactionReceipt().get().getTransactionHash() + " failed");
      } else {
        throw new ServerErrorException("Deploying token contract " + token.getContractAddress() + " failed");
      }
    }

    log.info("Deploy successful, contract address: " + token.getContractAddress());
    return token.getContractAddress();
  }

  public BigDecimal getSystemEthBalance() {
    Credentials credentials = systemCredentials();
    EthGetBalance ethGetBalance = handle(() -> web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send());
    BigDecimal balance = toTokensWithDecimals(ethGetBalance.getBalance());
    return balance;
  }

  public EthBalance getEthBalance(Community community) {
    EthGetBalance ethGetBalance = handle(() -> web3j.ethGetBalance(community.getMainWalletAddress(), DefaultBlockParameterName.LATEST).send());
    BigDecimal balance = toTokensWithDecimals(ethGetBalance.getBalance());
    EthBalance ethBalance = new EthBalance()
        .setCommunityId(community.getId())
        .setBalance(balance);
    
    return ethBalance;
  }

  public BigDecimal getEthBalance(String address) {
    log.info("balance request for " + address);
    BigInteger balance = handle(() -> web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance());
    log.info("balance request for " + address + " complete, balance=" + balance.toString());
    return toTokensWithDecimals(balance);
  }

  public TokenBalance getTokenBalance(Long communityId, WalletType walletType) {
    Community community = communityRepository.findStrictById(communityId);
    return getTokenBalance(community, walletType);
  }
  
  public TokenBalance getTokenBalance(Community community, WalletType walletType) {
    String walletAddress;
    switch (walletType) {
    case MAIN:
      walletAddress = community.getMainWalletAddress();
      break;
    case FEE:
      walletAddress = community.getFeeWalletAddress();
      break;
    default:
      throw new ServerErrorException();
    }
    
    BigDecimal balance = getTokenBalanceFromBlockchain(community.getId(), walletAddress, community.getTokenContractAddress());
    TokenBalance tokenBalance = new TokenBalance()
        .setBalance(balance)
        .setCommunityId(community.getId())
        .setToken(getCommunityToken(community.getTokenContractAddress()));
    return tokenBalance;
  }

  public TokenBalance getTokenBalance(User user, Long communityId) {
    return getTokenBalance(user, communityId, false);
  }
  
  public TokenBalance getTokenBalance(User user, Long communityId, boolean isOmiteBalance) {
    Community community = communityRepository.findStrictById(communityId);
    TokenBalance tokenBalance = new TokenBalance()
        .setCommunityId(communityId)
        .setToken(getCommunityToken(community.getTokenContractAddress()));
    if (isOmiteBalance) {
      return tokenBalance;
    }
    
    String minimumNumberOfDecimalsForToken = config.minimumNumberOfDecimalsForToken();
    int scale = Integer.parseInt(minimumNumberOfDecimalsForToken);
    
    BigDecimal balance = null;
    BigDecimal balanceOnBlockchain = null;
    for (int i = 0; i < 5; i++) {
      balanceOnBlockchain = getTokenBalanceFromBlockchain(communityId, user.getWalletAddress(), community.getTokenContractAddress()).setScale(scale, BigDecimal.ROUND_DOWN);
      BigDecimal settleAmount = tokenTransactionRepository.getSettleBalanceFromTransactions(user.getId(), communityId).setScale(scale, BigDecimal.ROUND_DOWN);
      BigDecimal pendingAmount = tokenTransactionRepository.getPendingTransactionsAmount(user.getId(), communityId).setScale(scale, BigDecimal.ROUND_DOWN);
      
      if (balanceOnBlockchain.compareTo(settleAmount) == 0) {
        balance = settleAmount.subtract(pendingAmount);
        break;
      } else {
        log.info(String.format("balance mismatched. [balanceOnBlockchain=%f, settleAmount=%f, pendingAmount=%f]", balanceOnBlockchain, settleAmount, pendingAmount));
      }
    }
    
    if (balance == null) {
      log.error("failed to obtain user balance. returning the balance from blockcain");
      balance = balanceOnBlockchain;
    }
    
    tokenBalance.setBalance(balance);
    return tokenBalance;
  }
  
  private BigDecimal getTokenBalanceFromBlockchain(Long communityId, String walletAddress, String tokenContractAddress) {
    log.info("Token balance request for: " + walletAddress);
    Token token = loadTokenReadOnly(tokenContractAddress);
    BigInteger balance = handle(() -> token.balanceOf(walletAddress).send());
    log.info("Token balance request complete, balance " + balance.toString());
    return toTokensWithDecimals(balance);
  }
  
  public String tokenName(String tokenAddress) {
    String tokenName = cache.getTokenName(tokenAddress);
    
    if (tokenName == null) {
      tokenName = getTokenNameFromBlockchain(tokenAddress);
    }
    
    return tokenName;
  }
  
  private synchronized String getTokenNameFromBlockchain(String tokenAddress) {
    String tokenName = cache.getTokenName(tokenAddress);
    if (tokenName != null) return tokenName;

    log.info(String.format("Token name request for: tokenAddress=%s", tokenAddress));
    Token token = loadTokenReadOnly(tokenAddress);
    tokenName = handle(() -> token.name().send());
    log.info(String.format("Token name request complete: name=%s, tokenAddress=%s", tokenName, tokenAddress));

    cache.setTokenName(tokenAddress, tokenName == null ? "" : tokenName);
    return tokenName;
  }

  public String tokenSymbol(String tokenAddress) {
    String tokenSymbol = cache.getTokenSymbol(tokenAddress);
    
    if (tokenSymbol == null) {
      tokenSymbol = getTokenSymbolFromBlockchain(tokenAddress);
    }
    
    return tokenSymbol;
  }
  
  private synchronized String getTokenSymbolFromBlockchain(String tokenAddress) {
    String tokenSymbol = cache.getTokenSymbol(tokenAddress);
    if (tokenSymbol != null) return tokenSymbol;

    log.info(String.format("Token symbol request for: tokenAddress=%s", tokenAddress));
    Token token = loadTokenReadOnly(tokenAddress);
    tokenSymbol = handle(() -> token.symbol().send());
    log.info(String.format("Token symbol request complete: symbol=%s, tokenAddress=%s", tokenSymbol, tokenAddress));

    cache.setTokenSymbol(tokenAddress, tokenSymbol == null ? "" : tokenSymbol);
    return tokenSymbol;
  }

  public BigDecimal totalSupply(String tokenAddress) {
    BigDecimal totalSupply = cache.getTotalSupply(tokenAddress);
    
    if (totalSupply == null) {
      totalSupply = getTotalSupplyFromBlockchain(tokenAddress);
    }
    
    return totalSupply;
  }
  
  private synchronized BigDecimal getTotalSupplyFromBlockchain(String tokenAddress) {
    BigDecimal totalSupply = cache.getTotalSupply(tokenAddress);
    if (totalSupply != null) return totalSupply;

    log.info(String.format("Token total supply request for: tokenAddress=%s", tokenAddress));
    Token token = loadTokenReadOnly(tokenAddress);
    BigInteger totalSupplyInWei = handle(() -> token.totalSupply().send());
    log.info(String.format("Token total supply complete: totalSupply=%d, tokenAddress=%s", totalSupplyInWei, tokenAddress));

    totalSupply = totalSupplyInWei == null ? BigDecimal.ZERO : toTokensWithDecimals(totalSupplyInWei);
    cache.setTotalSupply(tokenAddress, totalSupply);
    return totalSupply;
  }
  
  public String updateTokenName(Community community, String newTokenName) {
    String currentTokenName = tokenName(community.getTokenContractAddress());
    log.info(format("Updating token name. [communityId=%d, currentTokenName=%s, newTokenName=%s]", community.getId(), currentTokenName, newTokenName));

    Credentials credentials = credentials(community.getMainWallet(), config.communityWalletPassword());
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_SET_NAME_GAS_LIMIT);
    
    TransactionReceipt receipt = handle(() -> {
      return token.setName(newTokenName).send();
    });
    
    String hash = receipt.getTransactionHash();
    log.info(format("Update token name done. [hash=%s]", hash));
    
    return hash;
  }
  
  public String updateTotalSupply(Community community, BigDecimal absAmount, boolean isBurn) {
    BigDecimal currentTotalSupply = totalSupply(community.getTokenContractAddress());
    log.info(format("Updating total supply. [communityId=%d, currentTotalSupply=%f, absAmount=%f, isBurn=%b]", community.getId(), currentTotalSupply, absAmount, isBurn));

    Credentials credentials = credentials(community.getMainWallet(), config.communityWalletPassword());
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_MINT_BURN_GAS_LIMIT);
    
    TransactionReceipt receipt = handle(() -> {
      return isBurn ? token.burn(toTokensWithoutDecimals(absAmount)).send() : token.mint(toTokensWithoutDecimals(absAmount)).send();
    });
    
    String hash = receipt.getTransactionHash();
    log.info(format("Update total supply done. [hash=%s]", hash));
    
    return hash;
  }
  
  public void approveFromUser(User user, Community community) {
    log.info(format("Approving token transfer. [communityId=%d, userId=%d, amount=%d]", community.getId(), user.getId(), INITIAL_TOKEN_AMOUNT));

    Credentials credentials = credentials(user.getWallet(), config.userWalletPassword());
    String hash = approveOwner(credentials, community);

    log.info(format("Approving token transfer has sent. [hash=%s]", hash));
  }
  
  public void approveFromFeeWallet(Community community) {
    log.info(format("Approving token transfer of fee wallet. [communityId=%d, amount=%d]", community.getId(), INITIAL_TOKEN_AMOUNT));

    Credentials credentials = credentials(community.getFeeWallet(), config.communityWalletPassword());
    String hash = approveOwner(credentials, community);

    log.info(format("Approving token transfer has sent. [hash=%s]", hash));
  }
  
  private String approveOwner(Credentials credentials, Community community) {
    Token token = loadToken(
        credentials,
        community.getTokenContractAddress(),
        config.gasPrice(),
        TOKEN_APPROVE_GAS_LIMIT);
    
    TransactionReceipt receipt = handle(() -> {
      return token.approve(community.getMainWalletAddress(), INITIAL_TOKEN_AMOUNT).send();
    });
    
    return receipt.getTransactionHash();
  }
  
  public CommunityToken getCommunityToken(String tokenAddress) {
    CommunityToken communityToken = new CommunityToken();
    communityToken.setTokenName(tokenName(tokenAddress));
    communityToken.setTokenSymbol(tokenSymbol(tokenAddress));
    communityToken.setTotalSupply(totalSupply(tokenAddress));
    
    return communityToken;
  }
}
