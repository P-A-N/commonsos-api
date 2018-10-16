package commonsos.service.transaction;

import static commonsos.TestId.id;
import static java.math.BigDecimal.TEN;
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
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.DisplayableException;
import commonsos.exception.BadRequestException;
import commonsos.repository.ad.Ad;
import commonsos.repository.community.Community;
import commonsos.repository.transaction.Transaction;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.PushNotificationService;
import commonsos.service.ad.AdService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.util.UserUtil;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

  @Mock TransactionRepository repository;
  @Mock UserRepository userRepository;
  @Mock UserUtil userUtil;
  @Mock BlockchainService blockchainService;
  @Mock AdService adService;
  @Mock PushNotificationService pushNotificationService;
  @Captor ArgumentCaptor<Transaction> captor;
  @InjectMocks @Spy TransactionService service;

  @Test
  public void createTransaction() {
    TransactionCreateCommand command = command("community", "beneficiary", "10", "description", "ad id");
    User user = new User().setId(id("remitter")).setJoinedCommunities(asList(new Community().setId(id("community"))));
    doReturn(new BalanceView().setBalance(TEN)).when(service).balance(user, id("community"));
    Ad ad = new Ad();
    when(adService.ad(id("ad id"))).thenReturn(ad);
    when(adService.isPayableByUser(user, ad)).thenReturn(true);
    User beneficiary = new User().setJoinedCommunities(asList(new Community().setId(id("community"))));
    when(userRepository.findById(id("beneficiary"))).thenReturn(Optional.of(beneficiary));
    when(blockchainService.transferTokens(user, beneficiary, command.getCommunityId(), new BigDecimal("10"))).thenReturn("blockchain hash");

    Transaction result = service.create(user, command);

    assertThat(result.getBlockchainTransactionHash()).isEqualTo("blockchain hash");
    verify(repository).create(captor.capture());
    Transaction transaction = captor.getValue();
    assertThat(transaction.getCommunityId()).isEqualTo(id("community"));
    assertThat(transaction.getAmount()).isEqualByComparingTo(TEN);
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
    service.create(new User(), command("community", "beneficiary", "-0.01", "description", "ad id"));
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_zeroAmount() {
    service.create(new User(), command("community", "beneficiary", "0.0", "description", "ad id"));
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_descriptionIsMandatory() {
    service.create(new User(), command("community", "beneficiary", "10.2", " ", null));
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_communityIdIsRequired() {
    service.create(new User(), command("community", "beneficiary", "10", "description", "ad id").setCommunityId(null));
  }

  @Test
  public void createTransaction_withoutAd() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", "").setAdId(null);
    User remitter = new User().setId(id("remitter")).setJoinedCommunities(asList(new Community().setId(id("community"))));
    User beneficiary = new User().setId(id("beneficiary")).setJoinedCommunities(asList(new Community().setId(id("community"))));
    when(userRepository.findById(any())).thenReturn(Optional.of(beneficiary));
    doReturn(new BalanceView().setBalance(new BigDecimal("10.20"))).when(service).balance(remitter, id("community"));

    service.create(remitter, command);

    verify(repository).create(any());
  }

  private TransactionCreateCommand command(String communityId, String beneficiary, String amount, String description, String adId) {
    return new TransactionCreateCommand()
      .setCommunityId(id(communityId))
      .setBeneficiaryId(id(beneficiary))
      .setAmount(new BigDecimal(amount))
      .setDescription(description)
      .setAdId(id(adId));
  }

  @Test
  public void createTransaction_insufficientBalance() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", "ad id");
    User user = new User().setId(id("remitter")).setJoinedCommunities(asList(new Community().setId(id("community"))));
    doReturn(new BalanceView().setBalance(TEN)).when(service).balance(user, id("community"));
    Ad ad = new Ad().setCreatedBy(id("beneficiary")).setCommunityId(id("community"));
    when(userRepository.findById(id("beneficiary"))).thenReturn(
        Optional.of(new User().setId(id("beneficiary")).setJoinedCommunities(asList(new Community().setId(id("community"))))));
    when(adService.ad(id("ad id"))).thenReturn(ad);
    when(adService.isPayableByUser(user, ad)).thenReturn(true);
    DisplayableException thrown = catchThrowableOfType(() -> service.create(user, command), DisplayableException.class);

    assertThat(thrown).hasMessage("error.notEnoughFunds");
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_unknownBeneficiary() {
    when(userRepository.findById(id("unknown"))).thenThrow(new BadRequestException());
    TransactionCreateCommand command = command("community", "unknown", "10.2", "description", "33");

    service.create(new User().setId(id("remitter")), command);
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_unknownAd() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", "unknown ad");
    User user = new User().setId(id("remitter"));

    service.create(user, command);
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_canNotPayYourself() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", null);
    User user = new User().setId(id("beneficiary"));

    service.create(user, command);
  }

  @Test(expected = BadRequestException.class)
  public void createTransaction_communitiesDiffer() {
    TransactionCreateCommand command = command("community", "beneficiary", "0.1", "description", "ad id");
    User user = new User().setId(id("remitter")).setJoinedCommunities(asList(new Community().setId(id("community"))));
    when(userRepository.findById(id("beneficiary"))).thenReturn(
        Optional.of(new User().setJoinedCommunities(asList(new Community().setId(id("other community"))))));
    Ad ad = new Ad();
    when(adService.ad(id("ad id"))).thenReturn(ad);
    when(adService.isPayableByUser(user, ad)).thenReturn(true);

    service.create(user, command);
  }

  @Test
  public void transactions() {
    User user = new User();
    Transaction transaction1 = new Transaction().setCreatedAt(now().minus(1, HOURS));
    Transaction transaction2 = new Transaction().setCreatedAt(now());
    when(repository.transactions(user, id("community"))).thenReturn(asList(transaction1, transaction2));
    TransactionView transactionView1 = new TransactionView();
    TransactionView transactionView2 = new TransactionView();
    doReturn(transactionView1).when(service).view(user, transaction1);
    doReturn(transactionView2).when(service).view(user, transaction2);

    List<TransactionView> result = service.transactions(user, id("community"));

    assertThat(result).isEqualTo(asList(transactionView2, transactionView1));
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