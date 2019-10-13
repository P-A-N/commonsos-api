package commonsos.repository;

import static commonsos.TestId.id;
import static commonsos.repository.entity.WalletType.FEE;
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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;

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
      .setRemitterUserId(id("remitter user id"))
      .setBeneficiaryUserId(id("beneficiary user id"))
      .setRemitterAdminId(id("remitter admin id"))
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
    assertThat(result.getRemitterUserId()).isEqualTo(id("remitter user id"));
    assertThat(result.getBeneficiaryUserId()).isEqualTo(id("beneficiary user id"));
    assertThat(result.getRemitterAdminId()).isEqualTo(id("remitter admin id"));
    assertThat(result.getAdId()).isEqualTo(id("ad id"));
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.00"));
    assertThat(result.getFee().stripTrailingZeros()).isEqualTo(new BigDecimal("1.5"));
    assertThat(result.getWalletDivision()).isEqualTo(MAIN);
    assertThat(result.getBlockchainTransactionHash()).isEqualTo("blockchain id");
    assertThat(result.isFromAdmin()).isFalse();
    assertThat(result.isFeeTransaction()).isFalse();
    assertThat(result.isRedistributionTransaction()).isFalse();
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
  public void searchUserTran() {
    User user = new User().setId(id("user1"));

    Long id1 = inTransaction(() -> repository.create(userTran("com1", "user1", "user2"))).getId();
    Long id2 = inTransaction(() -> repository.create(userTran("com1", "user2", "user1"))).getId();
    Long id3 = inTransaction(() -> repository.create(userTran("com2", "user1", "user2"))).getId();
    Long id4 = inTransaction(() -> repository.create(userTran("com2", "user2", "user1"))).getId();

    assertThat(repository.searchUserTran(user, id("com1"), null).getList()).extracting("id").containsExactly(id1, id2);
  }

  @Test
  public void searchUserTran_paginarion() {
    // prepare
    User user = new User().setId(id("user1"));

    inTransaction(() -> repository.create(userTran("com1", "user1", "user2")));
    inTransaction(() -> repository.create(userTran("com1", "user2", "user1")));
    inTransaction(() -> repository.create(userTran("com1", "user1", "user2")));
    inTransaction(() -> repository.create(userTran("com1", "user2", "user1")));
    inTransaction(() -> repository.create(userTran("com1", "user2", "user1")));

    // execute
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    ResultList<TokenTransaction> result = repository.searchUserTran(user, id("com1"), pagination);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(3);

    // execute
    pagination.setPage(1);
    result = repository.searchUserTran(user, id("com1"), pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
  }

  @Test
  public void searchCommunityTran() {
    // prepare
    Long id1 = inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    Long id2 = inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    Long id3 = inTransaction(() -> repository.create(adminTran("com1", "user1", FEE))).getId();
    Long id4 = inTransaction(() -> repository.create(userTran("com1", "user2", "user1"))).getId();
    Long id5 = inTransaction(() -> repository.create(adminTran("com2", "user1", MAIN))).getId();

    // execute & verify
    List<TokenTransaction> result = repository.searchCommunityTran(id("com1"), MAIN, null).getList();
    assertThat(result).extracting("id").containsExactly(id1, id2);

    // execute & verify
    result = repository.searchCommunityTran(id("com1"), FEE, null).getList();
    assertThat(result).extracting("id").containsExactly(id3);

    // execute & verify
    result = repository.searchCommunityTran(id("com1"), null, null).getList();
    assertThat(result).extracting("id").containsExactly();
  }

  @Test
  public void searchCommunityTran_pagination() {
    // prepare
    inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.ASC);
    List<TokenTransaction> result = repository.searchCommunityTran(id("com1"), MAIN, pagination).getList();
    assertThat(result).size().isEqualTo(3);

    // execute & verify
    pagination.setPage(1);
    result = repository.searchCommunityTran(id("com1"), MAIN, pagination).getList();
    assertThat(result).size().isEqualTo(2);
  }

  @Test
  public void searchUnredistributedFeeTransaction() {
    // prepare
    Long id1 = inTransaction(() -> repository.create(userTran("com1", "user1", "user2"))).getId();
    Long id2 = inTransaction(() -> repository.create(feeTran("com1", "user1", false))).getId();
    Long id3 = inTransaction(() -> repository.create(feeTran("com1", "user1", true))).getId();
    Long id4 = inTransaction(() -> repository.create(adminTran("com1", "user1", MAIN))).getId();
    Long id5 = inTransaction(() -> repository.create(adminTran("com1", "user1", FEE))).getId();
    Long id6 = inTransaction(() -> repository.create(feeTran("com1", "user1", false))).getId();

    // execute & verify
    List<TokenTransaction> result = repository.searchUnredistributedFeeTransaction(id("com1"));
    assertThat(result).extracting("id").containsExactly(id2, id6);

    // execute & verify
    result = repository.searchUnredistributedFeeTransaction(id("com2"));
    assertThat(result.size()).isEqualTo(0);
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

  private TokenTransaction userTran(String communityId, String remitter, String beneficiary) {
    return new TokenTransaction().setCommunityId(id(communityId)).setBeneficiaryUserId(id(beneficiary)).setRemitterUserId(id(remitter));
  }

  private TokenTransaction adminTran(String communityId, String beneficiary, WalletType wallet) {
    return new TokenTransaction().setCommunityId(id(communityId)).setBeneficiaryUserId(id(beneficiary)).setFromAdmin(true).setWalletDivision(wallet);
  }

  private TokenTransaction feeTran(String communityId, String remitter, boolean redistributed) {
    return new TokenTransaction().setCommunityId(id(communityId)).setRemitterUserId(id(remitter)).setFeeTransaction(true).setWalletDivision(FEE).setRedistributed(redistributed);
  }

  @Test
  public void pendingTransactionsAmount() {
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterUserId(id("user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterUserId(id("user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterUserId(id("other user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("community id")).setRemitterUserId(id("user")).setAmount(ONE).setBlockchainCompletedAt(now())));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("other community id")).setRemitterUserId(id("user")).setAmount(TEN)));

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
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterUserId(id("admin")).setBeneficiaryUserId(id("u1")).setAmount(TEN)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterUserId(id("u1")).setBeneficiaryUserId(id("u2")).setAmount(ONE)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterUserId(id("u1")).setBeneficiaryUserId(id("u3")).setAmount(ONE)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c1")).setRemitterUserId(id("u3")).setBeneficiaryUserId(id("u1")).setAmount(ONE)));
    inTransaction(() -> repository.create(new TokenTransaction().setCommunityId(id("c2")).setRemitterUserId(id("admin")).setBeneficiaryUserId(id("u1")).setAmount(TEN)));

    
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