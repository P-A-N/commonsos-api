package commonsos.repository;

import static commonsos.TestId.id;
import static commonsos.repository.entity.WalletType.MAIN;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;

public class TokenTransactionRepositoryTest extends AbstractRepositoryTest {

  TokenTransactionRepository repository = spy(new TokenTransactionRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
  }
  
  @Test
  public void create() {
    Long id = inTransaction(() -> repository.create(new TokenTransaction()
      .setCommunityId(id("community id"))
      .setRemitterId(id("remitter id"))
      .setBeneficiaryId(id("beneficiary id"))
      .setAdId(id("ad id"))
      .setDescription("description")
      .setAmount(TEN)
      .setFee(new BigDecimal("1.5"))
      .setWalletDivision(MAIN)
      .setBlockchainTransactionHash("blockchain id"))).getId();

    TokenTransaction result = em().find(TokenTransaction.class, id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getCommunityId()).isEqualTo(id("community id"));
    assertThat(result.getRemitterId()).isEqualTo(id("remitter id"));
    assertThat(result.getBeneficiaryId()).isEqualTo(id("beneficiary id"));
    assertThat(result.getAdId()).isEqualTo(id("ad id"));
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.00"));
    assertThat(result.getFee().stripTrailingZeros()).isEqualTo(new BigDecimal("1.5"));
    assertThat(result.getWalletDivision()).isEqualTo(MAIN);
    assertThat(result.getBlockchainTransactionHash()).isEqualTo("blockchain id");
  }

  @Test
  public void update() {
    Long id = inTransaction(() -> repository.create(new TokenTransaction()
      .setCommunityId(id("community id"))
      .setAmount(TEN))).getId();

    inTransaction(() -> {
      TokenTransaction existing = em().find(TokenTransaction.class, id);
      repository.update(existing.setAmount(ONE));
    });

    TokenTransaction result = em().find(TokenTransaction.class, id);
    assertThat(result.getAmount()).isEqualByComparingTo(ONE);
  }

  @Test
  public void transactions() {
    User user = new User().setId(id("user1"));

    Long id1 = inTransaction(() -> repository.create(transaction(id("community1"), id("user1"), id("user2"))).getId());
    Long id2 = inTransaction(() -> repository.create(transaction(id("community1"), id("user2"), id("user1"))).getId());
    Long id3 = inTransaction(() -> repository.create(transaction(id("community2"), id("user1"), id("user2"))).getId());
    Long id4 = inTransaction(() -> repository.create(transaction(id("community2"), id("user2"), id("user1"))).getId());

    assertThat(repository.transactions(user, id("community1"), null).getList()).extracting("id").containsExactly(id1, id2);
  }

  @Test
  public void transactions_paginarion() {
    // prepare
    User user = new User().setId(id("user1"));

    inTransaction(() -> repository.create(transaction(id("community1"), id("user1"), id("user2"))));
    inTransaction(() -> repository.create(transaction(id("community1"), id("user2"), id("user1"))));
    inTransaction(() -> repository.create(transaction(id("community1"), id("user1"), id("user2"))));
    inTransaction(() -> repository.create(transaction(id("community1"), id("user2"), id("user1"))));
    inTransaction(() -> repository.create(transaction(id("community1"), id("user2"), id("user1"))));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<TokenTransaction> result = repository.transactions(user, id("community1"), pagination);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.transactions(user, id("community1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void findByBlockchainTransactionHash() {
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setBlockchainTransactionHash("other value")));
    Long id = inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setBlockchainTransactionHash("hash value")).getId());

    Optional<TokenTransaction> result = repository.findByBlockchainTransactionHash("hash value");

    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  public void findByHash_notFound() {
    assertThat(repository.findByBlockchainTransactionHash("hash value")).isEmpty();
  }

  private TokenTransaction transaction(Long communityId, Long remitterId, Long beneficiary) {
    return new TokenTransaction().setCommunityId(communityId).setBeneficiaryId(beneficiary).setRemitterId(remitterId);
  }

  @Test
  public void pendingTransactionsAmount() {
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterId(id("user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterId(id("user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterId(id("other user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterId(id("user")).setAmount(ONE).setBlockchainCompletedAt(now())));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("other community id")).setRemitterId(id("user")).setAmount(TEN)));

    BigDecimal amount = repository.pendingTransactionsAmount(id("user"), id("community id"));

    assertThat(amount).isEqualByComparingTo(new BigDecimal(20));
  }

  @Test
  public void hasPaid() {
    // prepare
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setAdId(id("ad1"))));
    
    // execute
    boolean result = repository.hasPaid(new Ad().setId(id("ad1")));
    
    // verify
    assertThat(result).isTrue();

    // execute
    result = repository.hasPaid(new Ad().setId(id("ad2")));
    
    // verify
    assertThat(result).isFalse();
  }

  @Test
  public void getBalanceFromTransactions() {
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterId(id("admin")).setBeneficiaryId(id("u1")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterId(id("u1")).setBeneficiaryId(id("u2")).setAmount(ONE)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterId(id("u1")).setBeneficiaryId(id("u3")).setAmount(ONE)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterId(id("u3")).setBeneficiaryId(id("u1")).setAmount(ONE)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c2")).setRemitterId(id("admin")).setBeneficiaryId(id("u1")).setAmount(TEN)));

    
//    ThreadValue.setReadOnly(true);
    BigDecimal balance = repository.getBalanceFromTransactions(new User().setId(id("u1")), id("c1"));
    assertThat(balance).isEqualByComparingTo(new BigDecimal(9));

    balance = repository.getBalanceFromTransactions(new User().setId(id("u2")), id("c1"));
    assertThat(balance).isEqualByComparingTo(new BigDecimal(1));

    balance = repository.getBalanceFromTransactions(new User().setId(id("u3")), id("c1"));
    assertThat(balance).isEqualByComparingTo(new BigDecimal(0));

    balance = repository.getBalanceFromTransactions(new User().setId(id("u1")), id("c2"));
    assertThat(balance).isEqualByComparingTo(new BigDecimal(10));

    balance = repository.getBalanceFromTransactions(new User().setId(id("u2")), id("c2"));
    assertThat(balance).isEqualByComparingTo(new BigDecimal(0));
  }

  @Test
  public void pendingTransactionsAmount_none() {
    BigDecimal amount = repository.pendingTransactionsAmount(id("user"), id("community"));

    assertThat(amount).isEqualByComparingTo(ZERO);
  }

  @Test
  public void pendingTransactionsCount() {
    long count = repository.pendingTransactionsCount();
    assertThat(count).isEqualTo(0L);
    
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id 1"))));
    count = repository.pendingTransactionsCount();
    assertThat(count).isEqualTo(1L);
    
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id 2")).setBlockchainCompletedAt(Instant.now())));
    count = repository.pendingTransactionsCount();
    assertThat(count).isEqualTo(1L);
  }
}