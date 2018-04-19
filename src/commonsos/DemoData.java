package commonsos;

import commonsos.domain.ad.AdCreateCommand;
import commonsos.domain.ad.AdService;
import commonsos.domain.auth.AccountCreateCommand;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserService;
import commonsos.domain.transaction.Transaction;
import commonsos.domain.transaction.TransactionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static commonsos.domain.ad.AdType.GIVE;
import static commonsos.domain.ad.AdType.WANT;

@Singleton
public class DemoData {

  @Inject UserService userService;
  @Inject TransactionService transactionService;
  @Inject AdService adService;

  public void install() {
    User worker = userService.create(new AccountCreateCommand().setUsername("worker").setPassword("secret00").setFirstName("Haruto").setLastName("Sato").setLocation("Shibuya, Tokyo, Japan"));
    User elderly1 = userService.create(new AccountCreateCommand().setUsername("elderly1").setPassword("secret00").setFirstName("Riku").setLastName("Suzuki").setLocation("Kaga, Ishikawa Prefecture, Japan"));
    User elderly2 = userService.create(new AccountCreateCommand().setUsername("elderly2").setPassword("secret00").setFirstName("Haru").setLastName("Takahashi").setLocation("Kaga, Ishikawa Prefecture, Japan"));
    User admin = userService.create(new AccountCreateCommand().setUsername("admin").setPassword("secret00").setFirstName("Coordinator").setLastName("Community").setLocation("Kaga, Ishikawa Prefecture, Japan"))
      .setAdmin(true);

    User bank = userService.create(new AccountCreateCommand().setUsername(UUID.randomUUID().toString()).setPassword(UUID.randomUUID().toString()).setFirstName("Bank").setLastName(" ")).setAdmin(true);

    transactionService.create(new Transaction().setRemitterId(bank.getId()).setAmount(new BigDecimal("10000000")).setBeneficiaryId(admin.getId()).setCreatedAt(OffsetDateTime.now()));
    transactionService.create(new Transaction().setRemitterId(admin.getId()).setAmount(new BigDecimal("2000")).setBeneficiaryId(elderly1.getId()).setDescription("Funds from municipality").setCreatedAt(OffsetDateTime.now()));
    transactionService.create(new Transaction().setRemitterId(admin.getId()).setAmount(new BigDecimal("2000")).setBeneficiaryId(elderly2.getId()).setDescription("Funds from municipality").setCreatedAt(OffsetDateTime.now()));

    adService.create(worker, new AdCreateCommand()
        .setType(GIVE)
        .setTitle("House cleaning")
        .setDescription("Vacuum cleaning, moist cleaning, floors etc")
        .setAmount(new BigDecimal("1299.01"))
        .setLocation("Kaga city")
      ).setPhotoUrl("/static/temp/sample-photo-apartment1.jpg");

    adService.create(elderly1, new AdCreateCommand()
        .setType(WANT)
        .setTitle("Shopping agent")
        .setDescription("Thank you for reading this article. I had traffic accident last year and chronic pain on left leg\uD83D\uDE22 I want anyone to help me by going shopping to a grocery shop once a week.")
        .setAmount(new BigDecimal("300"))
        .setLocation("Kumasakamachi 熊坂町")
      ).setPhotoUrl("/static/temp/shop.jpeg");

    adService.create(elderly2, new AdCreateCommand()
        .setType(WANT)
        .setTitle("小川くん、醤油かってきて")
        .setDescription("刺し身買ってきたから")
        .setAmount(new BigDecimal("1"))
        .setLocation("kaga")
      ).setPhotoUrl("/static/temp/soy.jpeg");
  }
}
