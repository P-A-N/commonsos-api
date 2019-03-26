package commonsos.repository;

import static commonsos.TestId.id;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Transaction;
import commonsos.repository.entity.User;

public class TransactionRepositoryTest extends RepositoryTest {

  TransactionRepository repository = new TransactionRepository(emService);

  @Test
  public void create() {
    Long id = inTransaction(() -> repository.create(new Transaction()
      .setCommunityId(id("community id"))
      .setRemitterId(id("remitter id"))
      .setBeneficiaryId(id("beneficiary id"))
      .setAdId(id("ad id"))
      .setDescription("description")
      .setCreatedAt(parse("2017-10-24T11:22:33Z"))
      .setAmount(TEN))
      .setBlockchainTransactionHash("blockchain id")
      .getId());

    Transaction result = em().find(Transaction.class, id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getCommunityId()).isEqualTo(id("community id"));
    assertThat(result.getRemitterId()).isEqualTo(id("remitter id"));
    assertThat(result.getBeneficiaryId()).isEqualTo(id("beneficiary id"));
    assertThat(result.getAdId()).isEqualTo(id("ad id"));
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getCreatedAt()).isEqualTo(parse("2017-10-24T11:22:33Z"));
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.00"));
    assertThat(result.getBlockchainTransactionHash()).isEqualTo("blockchain id");
  }

  @Test
  public void update() {
    Long id = inTransaction(() -> repository.create(new Transaction()
      .setCommunityId(id("community id"))
      .setAmount(TEN))
      .getId());

    inTransaction(() -> {
      Transaction existing = em().find(Transaction.class, id);
      repository.update(existing.setAmount(ONE));
    });

    Transaction result = em().find(Transaction.class, id);
    assertThat(result.getAmount()).isEqualByComparingTo(ONE);
  }

  @Test
  public void transactions() {
    User user = new User().setId(id("user1"));

    Long id1 = inTransaction(() -> repository.create(transaction(id("community1"), id("user1"), id("user2"))).getId());
    Long id2 = inTransaction(() -> repository.create(transaction(id("community1"), id("user2"), id("user1"))).getId());
    Long id3 = inTransaction(() -> repository.create(transaction(id("community2"), id("user1"), id("user2"))).getId());
    Long id4 = inTransaction(() -> repository.create(transaction(id("community2"), id("user2"), id("user1"))).getId());

    assertThat(repository.transactions(user, id("community1"))).extracting("id").containsExactly(id1, id2);
  }

  @Test
  public void findByBlockchainTransactionHash() {
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setBlockchainTransactionHash("other value")));
    Long id = inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setBlockchainTransactionHash("hash value")).getId());

    Optional<Transaction> result = repository.findByBlockchainTransactionHash("hash value");

    assertThat(result).isNotEmpty();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  public void findByHash_notFound() {
    assertThat(repository.findByBlockchainTransactionHash("hash value")).isEmpty();
  }

  private Transaction transaction(Long communityId, Long remitterId, Long beneficiary) {
    return new Transaction().setCommunityId(communityId).setBeneficiaryId(beneficiary).setRemitterId(remitterId);
  }

  @Test
  public void pendingTransactionsAmount() {
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setRemitterId(id("user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setRemitterId(id("user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setRemitterId(id("other user")).setAmount(TEN)));
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setRemitterId(id("user")).setAmount(ONE).setBlockchainCompletedAt(now())));
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("other community id")).setRemitterId(id("user")).setAmount(TEN)));

    BigDecimal amount = repository.pendingTransactionsAmount(id("user"), id("community id"));

    assertThat(amount).isEqualByComparingTo(new BigDecimal(20));
  }

  @Test
  public void hasPaid() {
    // prepare
    inTransaction(() -> repository.create(new Transaction().setCommunityId(id("community id")).setAdId(id("ad1"))));
    
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
  public void pendingTransactionsAmount_none() {
    BigDecimal amount = repository.pendingTransactionsAmount(id("user"), id("community"));

    assertThat(amount).isEqualByComparingTo(ZERO);
  }
}