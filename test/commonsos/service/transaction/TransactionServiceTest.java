package commonsos.service.transaction;

import static commonsos.TestId.id;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.BadRequestException;
import commonsos.DisplayableException;
import commonsos.repository.ad.Ad;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.service.PushNotificationService;
import commonsos.service.ad.AdService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.user.UserService;
import commonsos.service.view.UserView;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

  @Mock AdService adService;
  @Mock UserService userService;
  @Mock BlockchainService blockchainService;
  @Captor ArgumentCaptor<Transaction> captor;
  @Mock TransactionRepository repository;
  @Mock PushNotificationService pushNotificationService;
  @InjectMocks @Spy TransactionService service;

  @Test
  public void createTransaction() {
    TransactionCreateCommand command = command("beneficiary", "10", "description", "ad id");
    User user = new User().setId(id("remitter")).setCommunityId(id("community"));
    doReturn(BigDecimal.TEN).when(service).balance(user);
    Ad ad = new Ad();
    when(adService.ad(id("ad id"))).thenReturn(ad);
    when(adService.isPayableByUser(user, ad)).thenReturn(true);
    User beneficiary = new User().setCommunityId(id("community"));
    when(userService.user(id("beneficiary"))).thenReturn(beneficiary);
    when(blockchainService.transferTokens(user, beneficiary, new BigDecimal("10"))).thenReturn("blockchain hash");

    Transaction result = service.create(user, command);

    assertThat(result.getBlockchainTransactionHash()).isEqualTo("blockchain hash");
    verify(repository).create(captor.capture());
    Transaction transaction = captor.getValue();
    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.TEN);
    assertThat(transaction.getBeneficiaryId()).isEqualTo(id("beneficiary"));
    assertThat(transaction.getRemitterId()).isEqualTo(id("remitter"));
    assertThat(transaction.getDescription()).isEqualTo("description");
    assertThat(transaction.getAdId()).isEqualTo(id("ad id"));
    assertThat(transaction.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
    verify(repository).update(transaction);
    assertThat(transaction.getBlockchainTransactionHash()).isEqualTo("blockchain hash");
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_negativeAmount() {
    service.create(new User(), command("beneficiary", "-0.01", "description", "ad id"));
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_zeroAmount() {
    service.create(new User(), command("beneficiary", "0.0", "description", "ad id"));
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_descriptionIsMandatory() {
    service.create(new User(), command("beneficiary", "10.2", " ", null));
  }

  public void createTransaction_withoutAd() {
    TransactionCreateCommand command = command("beneficiary", "10.2", "description", null);
    User user = new User().setId(id("remitter"));
    doReturn(new BigDecimal("10.20")).when(service).balance(user);

    service.create(user, command);

    verify(repository).create(any());
  }

  private TransactionCreateCommand command(String beneficiary, String amount, String description, String adId) {
    return new TransactionCreateCommand()
      .setBeneficiaryId(id(beneficiary))
      .setAmount(new BigDecimal(amount))
      .setDescription(description)
      .setAdId(id(adId));
  }

  @Test
  public void createTransaction_insufficientBalance() {
    TransactionCreateCommand command = command("beneficiary", "10.2", "description", "ad id");
    User user = new User().setId(id("remitter"));
    doReturn(BigDecimal.TEN).when(service).balance(user);
    Ad ad = new Ad().setCreatedBy(id("beneficiary"));
    when(adService.ad(id("ad id"))).thenReturn(ad);
    when(adService.isPayableByUser(user, ad)).thenReturn(true);
    DisplayableException thrown = catchThrowableOfType(() -> service.create(user, command), DisplayableException.class);

    assertThat(thrown).hasMessage("error.notEnoughFunds");
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_unknownBeneficiary() {
    when(userService.user(id("unknown"))).thenThrow(new BadRequestException());
    TransactionCreateCommand command = command("unknown", "10.2", "description", "33");

    service.create(new User().setId(id("remitter")), command);
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_unknownAd() {
    TransactionCreateCommand command = command("beneficiary", "10.2", "description", "unknown ad");
    User user = new User().setId(id("remitter"));
    when(adService.ad(id("unknown ad"))).thenThrow(new BadRequestException());

    service.create(user, command);
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_canNotPayYourself() {
    TransactionCreateCommand command = command("beneficiary", "10.2", "description", null);
    User user = new User().setId(id("beneficiary"));

    service.create(user, command);
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_communitiesDiffer() {
    TransactionCreateCommand command = command("beneficiary", "0.1", "description", "ad id");
    User user = new User().setId(id("remitter")).setCommunityId(id("community"));
    when(userService.user(id("beneficiary"))).thenReturn(new User().setCommunityId(id("other community")));
    doReturn(new BigDecimal("0.2")).when(service).balance(user);
    Ad ad = new Ad();
    when(adService.ad(id("ad id"))).thenReturn(ad);
    when(adService.isPayableByUser(user, ad)).thenReturn(true);

    service.create(user, command);
  }

  @Test
  public void balance() {
    User user = new User().setId(id("user id"));
    when(blockchainService.tokenBalance(user)).thenReturn(BigDecimal.TEN);
    when(repository.pendingTransactionsAmount(id("user id"))).thenReturn(BigDecimal.ONE);

    BigDecimal result = service.balance(user);

    assertThat(result).isEqualByComparingTo(new BigDecimal("9"));
  }

  @Test
  public void view() {
    Instant createdAt = Instant.now();
    UserView beneficiary = new UserView();
    UserView remitter = new UserView();
    when(userService.view(id("beneficiary id"))).thenReturn(beneficiary);
    when(userService.view(id("remitter id"))).thenReturn(remitter);

    TransactionView view = service.view(
      new User().setId(id("remitter id")),
      new Transaction()
        .setBeneficiaryId(id("beneficiary id"))
        .setRemitterId(id("remitter id"))
        .setAmount(BigDecimal.TEN)
        .setDescription("description")
        .setBlockchainCompletedAt(now())
        .setCreatedAt(createdAt));

    assertThat(view.getBeneficiary()).isEqualTo(beneficiary);
    assertThat(view.getRemitter()).isEqualTo(remitter);
    assertThat(view.getAmount()).isEqualTo(BigDecimal.TEN);
    assertThat(view.getDescription()).isEqualTo("description");
    assertThat(view.getCreatedAt()).isEqualTo(createdAt);
    assertThat(view.isCompleted()).isEqualTo(true);
    assertThat(view.isDebit()).isTrue();
  }

  @Test
  public void transactions() {
    User user = new User();
    Transaction transaction1 = new Transaction().setCreatedAt(now().minus(1, HOURS));
    Transaction transaction2 = new Transaction().setCreatedAt(now());
    when(repository.transactions(user)).thenReturn(asList(transaction1, transaction2));
    TransactionView transactionView1 = new TransactionView();
    TransactionView transactionView2 = new TransactionView();
    doReturn(transactionView1).when(service).view(user, transaction1);
    doReturn(transactionView2).when(service).view(user, transaction2);

    List<TransactionView> result = service.transactions(user);

    assertThat(result).isEqualTo(asList(transactionView2, transactionView1));
  }

  @Test
  public void markTransactionCompleted() {
    Transaction transaction = new Transaction()
      .setRemitterId(id("remitter id"))
      .setBeneficiaryId(id("beneficiary id"))
      .setAmount(new BigDecimal("100.005"))
      .setDescription("Gift");
    when(repository.findByBlockchainTransactionHash("hash")).thenReturn(of(transaction));

    User remitter = new User();
    when(userService.user(id("remitter id"))).thenReturn(remitter);
    when(userService.fullName(remitter)).thenReturn("John Doe");

    User beneficiary = new User();
    when(userService.user(id("beneficiary id"))).thenReturn(beneficiary);


    service.markTransactionCompleted("hash");


    assertThat(transaction.getBlockchainCompletedAt()).isCloseTo(now(), within(1, SECONDS));
    verify(repository).update(transaction);
    verify(pushNotificationService).send(beneficiary, "John Doe\n+100.01 Gift");
  }

  @Test
  public void markTransactionCompleted_txNotFound() {
    when(repository.findByBlockchainTransactionHash("hash")).thenReturn(empty());

    service.markTransactionCompleted("hash");

    verify(repository).findByBlockchainTransactionHash("hash");
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void markTransactionCompleted_alreadyCompleted() {
    Transaction transaction = new Transaction().setBlockchainCompletedAt(now());
    when(repository.findByBlockchainTransactionHash("hash")).thenReturn(of(transaction));

    service.markTransactionCompleted("hash");

    verify(repository).findByBlockchainTransactionHash("hash");
    verifyNoMoreInteractions(repository);
  }
}