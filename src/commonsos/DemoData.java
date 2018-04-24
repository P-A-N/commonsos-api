package commonsos;

import commonsos.domain.ad.Ad;
import commonsos.domain.ad.AdCreateCommand;
import commonsos.domain.ad.AdService;
import commonsos.domain.auth.AccountCreateCommand;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserService;
import commonsos.domain.message.MessageService;
import commonsos.domain.message.MessageThreadView;
import commonsos.domain.transaction.Transaction;
import commonsos.domain.transaction.TransactionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.UUID;

import static commonsos.domain.ad.AdType.GIVE;
import static commonsos.domain.ad.AdType.WANT;
import static java.math.BigDecimal.TEN;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;

@Singleton
public class DemoData {

  @Inject UserService userService;
  @Inject TransactionService transactionService;
  @Inject AdService adService;
  @Inject MessageService messageService;

  public void install() {

    User worker = userService.create(new AccountCreateCommand().setUsername("worker").setPassword("secret00").setFirstName("Haruto").setLastName("Sato").setLocation("Shibuya, Tokyo, Japan"))
      .setAvatarUrl("https://image.jimcdn.com/app/cms/image/transf/none/path/s09a03e3ad80f8a02/image/i788e42d25ed4115e/version/1493969515/image.jpg")
      .setDescription("I am an Engineer, currently unemployed. I like helping elderly people, I can help with daily chores.");

    User elderly1 = userService.create(new AccountCreateCommand().setUsername("elderly1").setPassword("secret00").setFirstName("Riku").setLastName("Suzuki").setLocation("Kaga, Ishikawa Prefecture, Japan"))
      .setAvatarUrl("https://i.pinimg.com/originals/df/5c/70/df5c70b3b4895c4d9424de3845771182.jpg")
      .setDescription("I'm a retired person. I need personal assistance daily basis.");

    User elderly2 = userService.create(new AccountCreateCommand().setUsername("elderly2").setPassword("secret00").setFirstName("Haru").setLastName("Takahashi").setLocation("Kaga, Ishikawa Prefecture, Japan"))
      .setAvatarUrl("https://qph.fs.quoracdn.net/main-qimg-42b85e5f162e21ce346da83e8fa569bd-c").setDescription("Just jump in and lets play poker!");

    User admin = userService.create(new AccountCreateCommand().setUsername("admin").setPassword("secret00").setFirstName("Coordinator").setLastName("Community").setLocation("Kaga, Ishikawa Prefecture, Japan"))
      .setAdmin(true).setAvatarUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPlkwhBse_JCK37_0WA3m_PHUpFncOVLM0s0c4cCqpV27UteuJ")
      .setDescription("I'm a coordinator of a my community. Contact me if you have problem to solve.");

    User bank = userService.create(new AccountCreateCommand().setUsername(UUID.randomUUID().toString()).setPassword(UUID.randomUUID().toString()).setFirstName("Bank").setLastName(" "))
      .setAdmin(true).setAvatarUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPlkwhBse_JCK37_0WA3m_PHUpFncOVLM0s0c4cCqpV27UteuJ").setDescription("Not a real user.");

    transactionService.create(new Transaction().setRemitterId(bank.getId()).setAmount(new BigDecimal("10000000")).setBeneficiaryId(admin.getId()).setDescription("Initial emission to community").setCreatedAt(now().minus(9, DAYS)));

    transactionService.create(new Transaction().setRemitterId(admin.getId()).setAmount(new BigDecimal("2000")).setBeneficiaryId(elderly1.getId()).setDescription("Funds from municipality").setCreatedAt(now().minus(5, DAYS)));
    transactionService.create(new Transaction().setRemitterId(admin.getId()).setAmount(new BigDecimal("2000")).setBeneficiaryId(elderly2.getId()).setDescription("Funds from municipality").setCreatedAt(now().minus(4, DAYS)));

    Ad workerAd = adService.create(worker, new AdCreateCommand()
      .setType(GIVE)
      .setTitle("House cleaning")
      .setDescription("Vacuum cleaning, moist cleaning, floors etc")
      .setAmount(new BigDecimal("1299.01"))
      .setLocation("Kaga city")
    ).setPhotoUrl("/static/temp/sample-photo-apartment1.jpg");

    Ad elderly1Ad = adService.create(elderly1, new AdCreateCommand()
        .setType(WANT)
        .setTitle("Shopping agent")
        .setDescription("Thank you for reading this article. I had traffic accident last year and chronic pain on left leg\uD83D\uDE22 I want anyone to help me by going shopping to a grocery shop once a week.")
        .setAmount(new BigDecimal("300"))
        .setLocation("Kumasakamachi 熊坂町")
      ).setPhotoUrl("/static/temp/shop.jpeg");

    Ad elderly2Ad = adService.create(elderly2, new AdCreateCommand()
        .setType(WANT)
        .setTitle("小川くん、醤油かってきて")
        .setDescription("刺し身買ってきたから")
        .setAmount(new BigDecimal("20"))
        .setLocation("kaga")
      ).setPhotoUrl("/static/temp/soy.jpeg");

    transactionService.create(new Transaction().setBeneficiaryId(worker.getId()).setRemitterId(elderly1.getId()).setAdId(workerAd.getId()).setDescription("Ad: House cleaning (agreed price)").setAmount(new BigDecimal("999.99")).setCreatedAt(now().minus(2, DAYS)));
    transactionService.create(new Transaction().setBeneficiaryId(worker.getId()).setRemitterId(elderly2.getId()).setAdId(workerAd.getId()).setDescription("Ad: House cleaning").setAmount(new BigDecimal("1299.01")).setCreatedAt(now().minus(1, DAYS)));
    transactionService.create(new Transaction().setBeneficiaryId(elderly2.getId()).setRemitterId(elderly1.getId()).setAdId(elderly1Ad.getId()).setDescription("Ad: Shopping agent").setAmount(new BigDecimal("300")).setCreatedAt(now().minus(3, HOURS)));
    transactionService.create(new Transaction().setBeneficiaryId(worker.getId()).setRemitterId(elderly2.getId()).setAdId(elderly2Ad.getId()).setDescription("Ad: 小川くん、醤油かってきて").setAmount(TEN.add(TEN)).setCreatedAt(now().minus(1, HOURS)));

    MessageThreadView thread = messageService.threadForAd(worker, elderly1Ad.getId());
    MessageThreadView thread2 = messageService.threadForAd(elderly1, elderly2Ad.getId());
  }
}
