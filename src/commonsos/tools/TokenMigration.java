package commonsos.tools;

import static commonsos.service.blockchain.BlockchainService.INITIAL_TOKEN_AMOUNT;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import commonsos.JobService;
import commonsos.di.Web3jProvider;
import commonsos.repository.CommunityRepository;
import commonsos.repository.EntityManagerService;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.DelegateWalletTask;

public class TokenMigration {

  private static BlockchainService blockchainService;
  private static EntityManagerService emService;
  private static CommunityRepository communityRepository;
  private static UserRepository userRepository;
  private static TokenTransactionRepository transactionRepository;
  private static Credentials credentials;
  private static JobService jobService;
  private static Scanner scanner = new Scanner(System.in);


  public static void main(String[] args) throws Exception {
    checkArguments(args);
    prepareInstances();
    getCredentials(args);
    checkTXDone();

    System.out.println("Token migration started.\n");
    
    List<Community> communityList = emService.runInTransaction(() -> communityRepository.listPublic(null).getList());
    Set<Long> completedCommunity = new HashSet<>();
    showProgress(communityList, completedCommunity);
    
    for (int i = 0; i < communityList.size(); i++) {
      Community c = communityList.get(i);
      
      emService.runInTransaction(() -> {
        transferEtherToAdmin(c);
        String newTokenAddress = createToken(c);
        c.setTokenContractAddress(newTokenAddress);
        
        transferEtherToAdmin(c);
        allowAdmin(c);
        
        transferEtherToAdmin(c);
        transferToken(c);
        
        checkTokenBalance(c);
        
        communityRepository.update(c);
        completedCommunity.add(c.getId());
        return null;
      });

      showProgress(communityList, completedCommunity);
    }
    
    System.out.println("Done!");
    System.exit(0);
  }

  private static void checkArguments(String[] args) {
    if (args.length < 1) {
      System.out.println("Please specify Commons OS wallet file as first argument...");
      System.exit(0);
    }
  }

  private static void prepareInstances() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override protected void configure() {
        bind(Web3j.class).toProvider(Web3jProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
      }
    });

    blockchainService = injector.getInstance(BlockchainService.class);
    emService = injector.getInstance(EntityManagerService.class);
    communityRepository = injector.getInstance(CommunityRepository.class);
    userRepository = injector.getInstance(UserRepository.class);
    transactionRepository = injector.getInstance(TokenTransactionRepository.class);
    jobService = injector.getInstance(JobService.class);
  }

  private static void getCredentials(String[] args) throws Exception {
    System.out.print("Please unlock Commons OS wallet: ");
    String password = scanner.nextLine();
    credentials = WalletUtils.loadCredentials(password, new File(args[0]));
  }

  private static void checkTXDone() {
    long count = transactionRepository.pendingTransactionsCount();
    if (count != 0) {
      System.out.println(String.format("Transaction isn't done yet. [pendingTransactionCount=%d]", count));
      System.exit(0);
    }
  }

  private static String createToken(Community c) {
    System.out.println(String.format("Creating community's new token [Community name=%s]", c.getName()));
    
    System.out.print("Please enter community's token name: ");
    String tokenName = scanner.nextLine();
    System.out.print("Please enter community's token symbol: ");
    String tokenSymbol = scanner.nextLine();
    
    String newTokenAddress = blockchainService.createToken(c.getAdminUser(), tokenSymbol, tokenName);

    System.out.println(String.format("Finished creating community's token [Community name=%s, Token address=%s]", c.getName(), newTokenAddress));
    System.out.println();
    return newTokenAddress;
  }

  private static void transferEtherToAdmin(Community c) {
    BigDecimal balance = blockchainService.getBalance(c.getAdminUser().getWalletAddress());
    System.out.println(String.format("Balance of admin=%f [admin address=%s]", balance, c.getAdminUser().getWalletAddress()));
    if (balance.compareTo(BigDecimal.TEN.pow(4)) < 0) {
      System.out.println(String.format("Transfer ether to admin [admin address=%s]", c.getAdminUser().getWalletAddress()));
      blockchainService.transferEther(credentials, c.getAdminUser().getWalletAddress(), BigInteger.TEN.pow(18 + 5));
      System.out.println(String.format("Finish transfer ether to admin [admin address=%s]", c.getAdminUser().getWalletAddress()));
    }
    
    System.out.println();
  }

  private static void allowAdmin(Community c) throws Exception {
    waitUntilTransferredEther(c.getAdminUser().getWalletAddress());
    
    List<User> userList = userRepository.search(c.getId(), null, null).getList();
    for (int i = 0; i < userList.size(); i++) {
      User u = userList.get(i);
      if (u.getId().equals(c.getAdminUser().getId())) continue;

      System.out.println(String.format("Allowing admin [users=%s, admin=%s]", u.getUsername(), c.getAdminUser().getUsername()));
      DelegateWalletTask task = new DelegateWalletTask(u, c);
      jobService.submit(u, task);
      Thread.sleep(500);
    }
  }

  private static void transferToken(Community c) throws Exception {
    waitUntilTransferredEther(c.getAdminUser().getWalletAddress());
    
    List<User> userList = userRepository.search(c.getId(), null, null).getList();
    for (int i = 0; i < userList.size(); i++) {
      User u = userList.get(i);
      if (u.getId().equals(c.getAdminUser().getId())) continue;
      waitUntilAllowed(u, c);

      BigDecimal balance = transactionRepository.getBalanceFromTransactions(u, c.getId());
      System.out.println(String.format("Transfering token [users=%s, community=%s, amount=%f]", u.getUsername(), c.getAdminUser().getUsername(), balance));
      blockchainService.transferTokens(c.getAdminUser(), u, c.getId(), balance);
      Thread.sleep(500);
    }
  }

  private static void checkTokenBalance(Community c) throws Exception {
    List<User> userList = userRepository.search(c.getId(), null, null).getList();
    for (int i = 0; i < userList.size(); i++) {
      User u = userList.get(i);
      if (u.getId().equals(c.getAdminUser().getId())) continue;
      
      waitUntilTransferredToken(u, c);
      BigDecimal balanceFromTransactions = transactionRepository.getBalanceFromTransactions(u, c.getId());
      BigDecimal balanceFromBlockchain = blockchainService.getTokenBalance(u, c.getId()).getBalance();
      if (balanceFromTransactions.compareTo(balanceFromBlockchain) == 0) {
        System.out.println(String.format("Token balance is OK. [users=%s, community=%s, balance=%f]", u.getUsername(), c.getName(), balanceFromBlockchain));
      } else {
        System.out.println(String.format("Token balance isn't OK. [users=%s, community=%s, balanceFromTransactions=%f, balanceFromBlockchain=%f]", u.getUsername(), c.getName(), balanceFromTransactions, balanceFromBlockchain));
      }
    }
  }
  
  private static void waitUntilTransferredEther(String address) throws Exception {
    BigDecimal balance = blockchainService.getBalance(address);
    
    while (balance.compareTo(BigDecimal.valueOf(1000)) < 0) {
      System.out.println(String.format("Waiting for transferred ether [address=%s, balance=%f]", address, balance));
      Thread.sleep(10000);
      balance = blockchainService.getBalance(address);
      if (balance.compareTo(BigDecimal.valueOf(1000)) >= 0) {
        System.out.println(String.format("Transferred ether [address=%s, balance=%f]", address, balance));
      }
    }
  }
  
  private static void waitUntilAllowed(User u, Community c) throws Exception {
    while (!blockchainService.isAllowed(u, c, INITIAL_TOKEN_AMOUNT.divide(BigInteger.TEN))) {
      System.out.println(String.format("Waiting for allowd [user=%s, community=%s]", u.getUsername(), c.getName()));
      Thread.sleep(10000);
      if (blockchainService.isAllowed(u, c, INITIAL_TOKEN_AMOUNT.divide(BigInteger.TEN))) {
        System.out.println(String.format("Allowing completed [user=%s, community=%s]", u.getUsername(), c.getName()));
      }
    }
  }
  
  private static void waitUntilTransferredToken(User u, Community c) throws Exception {
    BigDecimal balanceFromTransactions = transactionRepository.getBalanceFromTransactions(u, c.getId());
    if (balanceFromTransactions.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }
    
    BigDecimal balanceFromBlockchain = blockchainService.getTokenBalance(u, c.getId()).getBalance();
    
    while (balanceFromBlockchain.compareTo(BigDecimal.ZERO) == 0) {
      System.out.println(String.format("Waiting for transferred token [users=%s, community=%s, balance=%f]", u.getUsername(), c.getName(), balanceFromBlockchain));
      Thread.sleep(10000);
      balanceFromBlockchain = blockchainService.getTokenBalance(u, c.getId()).getBalance();
      if (balanceFromBlockchain.compareTo(BigDecimal.ZERO) > 0) {
        System.out.println(String.format("Transferred token [users=%s, community=%s, balance=%f]", u.getUsername(), c.getName(), balanceFromBlockchain));
      }
    }
  }

  private static void showProgress(List<Community> communityList, Set<Long> completedCommunity) {
    System.out.println("Printing progresses...");
    System.out.println(" id\t name\t\t tokenAddress\t\t\t\t\t progress");
    System.out.println("----------------------------------------------------------------------------");
    communityList.forEach(c -> {
      String progress = completedCommunity.contains(c.getId()) ? "DONE" : "";
      System.out.println(String.format(" %d\t %s\t %s\t %s", c.getId(), c.getName(), c.getTokenContractAddress(), progress));
    });
    System.out.println();
  }
}
