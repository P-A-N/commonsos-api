package commonsos.domain.transaction;

import commonsos.DisplayableException;
import commonsos.domain.agreement.Agreement;
import commonsos.domain.agreement.AgreementService;
import commonsos.domain.auth.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

  @Mock TransactionRepository repository;
  @Mock AgreementService agreementService;
  @InjectMocks TransactionService transactionService;
  @Captor ArgumentCaptor<Transaction> captor;

  @Test
  public void claim() {
    Agreement agreement = new Agreement().setPoints(TEN).setId("123").setProviderId("worker").setConsumerId("elderly");
    when(agreementService.findByTransactionData("transactionData")).thenReturn(Optional.of(agreement));

    Transaction result = transactionService.claim(new User().setId("worker"), "transactionData");

    verify(agreementService).rewardClaimed(agreement);
    verify(repository).create(captor.capture());
    Transaction transaction = captor.getValue();
    assertThat(transaction.getAgreementId()).isEqualTo("123");
    assertThat(transaction.getAmount()).isEqualTo(TEN);
    assertThat(transaction.getBeneficiaryId()).isEqualTo("worker");
    assertThat(transaction.getRemitterId()).isEqualTo("elderly");
    assertThat(transaction.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
    assertThat(transaction).isEqualTo(result);
  }

  @Test
  public void claim_onlyProviderCanClaimReward() {
    when(agreementService.findByTransactionData("otherUserTransactionData")).thenReturn(Optional.of(new Agreement().setProviderId("worker")));

    DisplayableException thrown = catchThrowableOfType(() -> transactionService.claim(new User().setId("other user id"), "otherUserTransactionData"), DisplayableException.class);

    assertThat(thrown).hasMessage("Only service provider can claim this code");
  }

  @Test
  public void claim_onlyOnceAllowed() {
    Agreement agreement = new Agreement().setPoints(TEN).setId("123").setProviderId("worker").setConsumerId("elderly").setRewardClaimedAt(now());
    when(agreementService.findByTransactionData("transactionData")).thenReturn(Optional.of(agreement));

    DisplayableException thrown = catchThrowableOfType(() -> transactionService.claim(new User().setId("worker"), "transactionData"), DisplayableException.class);

    assertThat(thrown).hasMessage("This code has been already claimed");
  }

  @Test
  public void balance() {
    User user = new User().setId("worker");
    List<Transaction> transactions = asList(
      new Transaction().setRemitterId("worker").setBeneficiaryId("elderly").setAmount(ONE),
      new Transaction().setRemitterId("elderly").setBeneficiaryId("worker").setAmount(TEN));
    when(repository.transactions(user)).thenReturn(transactions);

    BigDecimal balance = transactionService.balance(user);

    assertThat(balance).isEqualTo(new BigDecimal("9"));
  }

  @Test
  public void transactions() {
    User user = new User().setId("worker");
    List<Transaction> transactions = asList(new Transaction().setBeneficiaryId("worker"));
    when(repository.transactions(user)).thenReturn(transactions);

    List<Transaction> result = transactionService.transactions(user);

    assertThat(result).isEqualTo(transactions);
  }
}