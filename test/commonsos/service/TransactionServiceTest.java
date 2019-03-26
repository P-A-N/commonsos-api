package commonsos.service;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.repository.AdRepository;
import commonsos.repository.TransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.command.TransactionCreateCommand;
import commonsos.view.BalanceView;
import commonsos.view.TransactionView;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransactionServiceTest {

  @Mock TransactionRepository repository;
  @Mock UserRepository userRepository;
  @Mock AdRepository adRepository;
  @Mock BlockchainService blockchainService;
  @Captor ArgumentCaptor<Transaction> captor;
  @InjectMocks @Spy TransactionService service;

  @BeforeEach
  public void setup() {
    when(userRepository.findStrictById(any())).thenCallRealMethod();
  }
  
  @Test
  public void createTransaction() {
    TransactionCreateCommand command = command("community", "beneficiary", "10", "description", "ad id");
    User user = new User().setId(id("remitter")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(id("community")))));
    doReturn(new BalanceView().setBalance(TEN)).when(service).balance(user, id("community"));
    Ad ad = new Ad().setPoints(TEN).setCreatedBy(id("remitter")).setType(AdType.WANT);
    when(adRepository.findStrict(id("ad id"))).thenReturn(ad);
    User beneficiary = new User().setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(id("community")))));
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

  @Test
  public void createTransaction_negativeAmount() {
    assertThrows(BadRequestException.class, () -> service.create(new User(), command("community", "beneficiary", "-0.01", "description", "ad id")));
  }

  @Test
  public void createTransaction_zeroAmount() {
    assertThrows(BadRequestException.class, () -> service.create(new User(), command("community", "beneficiary", "0.0", "description", "ad id")));
  }

  @Test
  public void createTransaction_descriptionIsMandatory() {
    assertThrows(BadRequestException.class, () -> service.create(new User(), command("community", "beneficiary", "10.2", " ", null)));
  }

  @Test
  public void createTransaction_communityIdIsRequired() {
    assertThrows(BadRequestException.class, () -> service.create(new User(), command("community", "beneficiary", "10", "description", "ad id").setCommunityId(null)));
  }

  @Test
  public void createTransaction_withoutAd() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", "").setAdId(null);
    User remitter = new User().setId(id("remitter")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(id("community")))));
    User beneficiary = new User().setId(id("beneficiary")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(id("community")))));
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
    User user = new User().setId(id("remitter")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(id("community")))));
    doReturn(new BalanceView().setBalance(TEN)).when(service).balance(user, id("community"));
    Ad ad = new Ad().setCreatedBy(id("beneficiary")).setPoints(TEN).setType(AdType.GIVE).setCommunityId(id("community"));
    when(userRepository.findById(id("beneficiary"))).thenReturn(
        Optional.of(new User().setId(id("beneficiary")).setCommunityUserList(asList(
            new CommunityUser().setCommunity(new Community().setId(id("community")))))));
    when(adRepository.findStrict(id("ad id"))).thenReturn(ad);
    DisplayableException thrown = catchThrowableOfType(() -> service.create(user, command), DisplayableException.class);

    assertThat(thrown).hasMessage("error.notEnoughFunds");
  }

  @Test
  public void createTransaction_unknownBeneficiary() {
    when(userRepository.findById(id("unknown"))).thenThrow(new BadRequestException());
    TransactionCreateCommand command = command("community", "unknown", "10.2", "description", "33");

    assertThrows(BadRequestException.class, () -> service.create(new User().setId(id("remitter")), command));
  }

  @Test
  public void createTransaction_unknownAd() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", "unknown ad");
    User user = new User().setId(id("remitter"));

    assertThrows(BadRequestException.class, () -> service.create(user, command));
  }

  @Test
  public void createTransaction_canNotPayYourself() {
    TransactionCreateCommand command = command("community", "beneficiary", "10.2", "description", null);
    User user = new User().setId(id("beneficiary"));

    assertThrows(BadRequestException.class, () -> service.create(user, command));
  }

  @Test
  public void createTransaction_communitiesDiffer() {
    TransactionCreateCommand command = command("community", "beneficiary", "0.1", "description", "ad id");
    User user = new User().setId(id("remitter")).setCommunityUserList(asList(
        new CommunityUser().setCommunity(new Community().setId(id("community")))));
    when(userRepository.findById(id("beneficiary"))).thenReturn(
        Optional.of(new User().setCommunityUserList(asList(
            new CommunityUser().setCommunity(new Community().setId(id("other community")))))));
    Ad ad = new Ad().setPoints(TEN).setCreatedBy(id("remitter")).setType(AdType.WANT);
    when(adRepository.findStrict(id("ad id"))).thenReturn(ad);

    assertThrows(BadRequestException.class, () -> service.create(user, command));
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