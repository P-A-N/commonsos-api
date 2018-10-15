package commonsos;

import static commonsos.repository.ad.AdType.GIVE;
import static commonsos.repository.ad.AdType.WANT;
import static commonsos.service.blockchain.BlockchainService.GAS_PRICE;
import static commonsos.service.blockchain.BlockchainService.TOKEN_DEPLOYMENT_GAS_LIMIT;
import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.ad.AdCreateCommand;
import commonsos.service.ad.AdService;
import commonsos.service.ad.AdView;
import commonsos.service.auth.AccountCreateCommand;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.message.MessagePostCommand;
import commonsos.service.message.MessageService;
import commonsos.service.message.MessageThreadView;
import commonsos.service.transaction.TransactionCreateCommand;
import commonsos.service.transaction.TransactionService;
import commonsos.service.user.UserService;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DemoData {

  @Inject EntityManagerService emService;
  @Inject UserService userService;
  @Inject UserRepository userRepository;
  @Inject TransactionService transactionService;
  @Inject AdService adService;
  @Inject MessageService messageService;
  @Inject BlockchainService blockchainService;
  @Inject CommunityRepository communityRepository;
  @Inject Web3j web3j;

  public void install() {

    if (!emService.get().createQuery("FROM User", User.class).setMaxResults(1).getResultList().isEmpty()) {
      log.info("At least one user found from database");
      return;
    }

    log.info("Installing demo data!");

    BigInteger initialEtherAmountForAdmin = TOKEN_DEPLOYMENT_GAS_LIMIT.add(new BigInteger("1000").multiply(TOKEN_TRANSFER_GAS_LIMIT)).multiply(GAS_PRICE);

    Credentials commonsos = commonsosCredentials();

    User admin = emService.runInTransaction(() -> userService.create(new AccountCreateCommand().setUsername("admin").setPassword("secret00").setFirstName("Coordinator").setLastName("Community").setLocation("Kaga, Ishikawa Prefecture, Japan"))
      .setAvatarUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPlkwhBse_JCK37_0WA3m_PHUpFncOVLM0s0c4cCqpV27UteuJ")
      .setDescription("I'm a coordinator of Kaga City community. Contact me if you have problem to solve."));
    blockchainService.transferEther(commonsos, admin.getWalletAddress(), initialEtherAmountForAdmin);
    Community kagaCommunity = createCommunity(admin, "Kaga city", "KAGA", "Kaga coin");
    sampleData(admin, kagaCommunity);

    // second community
    User admin2 = emService.runInTransaction(() -> userService.create(new AccountCreateCommand().setUsername("admin2").setPassword("secret02").setFirstName("Coordinator").setLastName("Community").setLocation("Shibuya, Tokyo"))
      .setAvatarUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPlkwhBse_JCK37_0WA3m_PHUpFncOVLM0s0c4cCqpV27UteuJ")
      .setDescription("I'm a coordinator of Shibuya People community. Contact me if you have problem to solve."));
    blockchainService.transferEther(commonsos, admin2.getWalletAddress(), initialEtherAmountForAdmin);
    createCommunity(admin2, "Shibuya People", "SHI", "Shibuya coin");

    // third community
    User admin3 = emService.runInTransaction(() -> userService.create(new AccountCreateCommand().setUsername("admin3").setPassword("secret03").setFirstName("Coordinator").setLastName("Community").setLocation("Tokyo, Japan"))
      .setAvatarUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPlkwhBse_JCK37_0WA3m_PHUpFncOVLM0s0c4cCqpV27UteuJ")
      .setDescription("I'm a coordinator of Commons Inc community. Contact me if you have problem to solve."));
    blockchainService.transferEther(commonsos, admin3.getWalletAddress(), initialEtherAmountForAdmin);
    createCommunity(admin3, "Commons Inc", "ICOM", "Commons Inc coin");
  }

  private void sampleData(User admin, Community community) {
    User worker = emService.runInTransaction(() ->
      userService.create(new AccountCreateCommand()
        .setUsername("worker")
        .setPassword("secret00")
        .setFirstName("Haruto")
        .setLastName("Sato")
        .setLocation("Shibuya, Tokyo, Japan")
        .setDescription("I am an Engineer, currently unemployed. I like helping elderly people, I can help with daily chores.")
        .setWaitUntilCompleted(true)
      )
      .setAvatarUrl("https://image.jimcdn.com/app/cms/image/transf/none/path/s09a03e3ad80f8a02/image/i788e42d25ed4115e/version/1493969515/image.jpg")
    );

    User elderly1 = emService.runInTransaction(() ->
      userService.create(new AccountCreateCommand()
        .setUsername("elderly1")
        .setPassword("secret00")
        .setFirstName("Riku")
        .setLastName("Suzuki")
        .setLocation("Kaga, Ishikawa Prefecture, Japan")
        .setDescription("I'm a retired person. I need personal assistance daily basis.")
        .setWaitUntilCompleted(true)
      )
      .setAvatarUrl("https://i.pinimg.com/originals/df/5c/70/df5c70b3b4895c4d9424de3845771182.jpg")
    );

    User elderly2 = emService.runInTransaction(() ->
      userService.create(new AccountCreateCommand()
        .setUsername("elderly2")
        .setPassword("secret00")
        .setFirstName("Haru")
        .setLastName("Takahashi")
        .setLocation("Kaga, Ishikawa Prefecture, Japan")
        .setDescription("Just jump in and lets play poker!")
        .setWaitUntilCompleted(true)
      )
      .setAvatarUrl("https://qph.fs.quoracdn.net/main-qimg-42b85e5f162e21ce346da83e8fa569bd-c")
    );

    waitTransactionCompleted(
      transactionService.create(admin, new TransactionCreateCommand().setCommunityId(community.getId()).setAmount(new BigDecimal("2000")).setBeneficiaryId(elderly1.getId()).setDescription("Funds from municipality"))
    );

    waitTransactionCompleted(
      transactionService.create(admin, new TransactionCreateCommand().setCommunityId(community.getId()).setAmount(new BigDecimal("2000")).setBeneficiaryId(elderly2.getId()).setDescription("Funds from municipality"))
    );

    AdView workerAd = emService.runInTransaction(() -> adService.create(worker, new AdCreateCommand()
      .setType(GIVE)
      .setTitle("House cleaning")
      .setDescription("Vacuum cleaning, moist cleaning, floors etc")
      .setPoints(new BigDecimal("1299.01"))
      .setLocation("Kaga city")
      .setPhotoUrl("/static/temp/sample-photo-apartment1.jpg"))
    );

    AdView elderly1Ad = emService.runInTransaction(() -> adService.create(elderly1, new AdCreateCommand()
      .setType(WANT)
      .setTitle("Shopping agent")
      .setDescription("Thank you for reading this article. I had traffic accident last year and chronic pain on left leg\uD83D\uDE22 I want anyone to help me by going shopping to a grocery shop once a week.")
      .setPoints(new BigDecimal("300"))
      .setLocation("Kumasakamachi 熊坂町")
      .setPhotoUrl("/static/temp/shop.jpeg")
    ));

    AdView elderly2Ad = emService.runInTransaction(() -> adService.create(elderly2, new AdCreateCommand()
      .setType(WANT)
      .setTitle("小川くん、醤油かってきて")
      .setDescription("刺し身買ってきたから")
      .setPoints(new BigDecimal("20"))
      .setLocation("kaga")
      .setPhotoUrl("/static/temp/soy.jpeg")
    ));

    MessageThreadView workerAdElderly1Thread = emService.runInTransaction(() -> messageService.threadForAd(elderly1, workerAd.getId()));
    messageService.postMessage(elderly1, new MessagePostCommand().setThreadId(workerAdElderly1Thread.getId()).setText("Hello!"));
    messageService.postMessage(elderly1, new MessagePostCommand().setThreadId(workerAdElderly1Thread.getId()).setText("I would like you to do cleaning in my appartement"));
    messageService.postMessage(worker, new MessagePostCommand().setThreadId(workerAdElderly1Thread.getId()).setText("Hi, what about tomorrow in the afternoon?"));
    messageService.postMessage(elderly1, new MessagePostCommand().setThreadId(workerAdElderly1Thread.getId()).setText("But I have a very little appartement, could it be cheaper?"));
    messageService.postMessage(worker, new MessagePostCommand().setThreadId(workerAdElderly1Thread.getId()).setText("No problem, it will be special price for you: 999.99"));
    transactionService.create(elderly1, new TransactionCreateCommand().setCommunityId(community.getId()).setBeneficiaryId(worker.getId()).setAdId(workerAd.getId()).setDescription("Ad: House cleaning (agreed price)").setAmount(new BigDecimal("999.99")));


    MessageThreadView workerAdElderly2Thread = emService.runInTransaction(() -> messageService.threadForAd(elderly2, workerAd.getId()));
    messageService.postMessage(elderly2, new MessagePostCommand().setThreadId(workerAdElderly2Thread.getId()).setText("Hi! Would like to arrange cleaning on a weekly basis"));
    messageService.postMessage(worker, new MessagePostCommand().setThreadId(workerAdElderly2Thread.getId()).setText("Hi! Ok, would it be ok to do the first cleaning next Tuesday?"));
    messageService.postMessage(elderly2, new MessagePostCommand().setThreadId(workerAdElderly2Thread.getId()).setText("Yes, waiting for you."));
    transactionService.create(elderly2, new TransactionCreateCommand().setCommunityId(community.getId()).setBeneficiaryId(worker.getId()).setAdId(workerAd.getId()).setDescription("Ad: House cleaning").setAmount(new BigDecimal("1299.01")));

    MessageThreadView elderly1AdThread = emService.runInTransaction(() -> messageService.threadForAd(elderly2, elderly1Ad.getId()));
    messageService.postMessage(elderly2, new MessagePostCommand().setThreadId(elderly1AdThread.getId()).setText("Hi, I can bring you some food from the shop"));
    transactionService.create(elderly1, new TransactionCreateCommand().setCommunityId(community.getId()).setBeneficiaryId(elderly2.getId()).setAdId(elderly1Ad.getId()).setDescription("Ad: Shopping agent").setAmount(new BigDecimal("300")));

    MessageThreadView elderly2AdThread = emService.runInTransaction(() -> messageService.threadForAd(worker, elderly2Ad.getId()));
    transactionService.create(elderly2, new TransactionCreateCommand().setCommunityId(community.getId()).setBeneficiaryId(worker.getId()).setAdId(elderly2Ad.getId()).setDescription("Ad: 小川くん、醤油かってきて").setAmount(BigDecimal.TEN.add(BigDecimal.TEN)));
  }

  private void waitTransactionCompleted(Transaction transaction) {
    PollingTransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 1000, 300);

    try {
      receiptProcessor.waitForTransactionReceipt(transaction.getBlockchainTransactionHash());
    }
    catch (Exception e) {
      log.warn("Failed", e);
      throw new RuntimeException(e);
    }
  }

  private Community createCommunity(User admin, String name, String tokenSymbol, String tokenName) {
    String tokenAddress = blockchainService.createToken(admin, tokenSymbol, tokenName);
    Community community = emService.runInTransaction(() -> communityRepository.create(new Community().setName(name).setAdminUser(admin).setTokenContractAddress(tokenAddress)));

    emService.runInTransaction(() -> userRepository.update(admin));
    return community;
  }

  private Credentials commonsosCredentials() {
    try {
      String walletFile = getEnv("COMMONSOS_WALLET_FILE");
      log.info("Loading CommonsOS wallet from " + walletFile);
      return WalletUtils.loadCredentials(getEnv("COMMONSOS_WALLET_PASSWORD"), walletFile);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getEnv(String variable) {
    String value = System.getenv(variable);
    if (value == null) throw new RuntimeException(String.format("Environment variable %s not defined", variable));
    return value;
  }
}
