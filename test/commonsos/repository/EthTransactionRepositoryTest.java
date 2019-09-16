package commonsos.repository;

import static commonsos.TestId.id;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.repository.entity.EthTransaction;

public class EthTransactionRepositoryTest extends AbstractRepositoryTest {

  EthTransactionRepository repository = spy(new EthTransactionRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
  }
  
  @Test
  public void findByBlockchainTransactionHash() {
    // prepare
    EthTransaction ethT1 = inTransaction(() -> repository.create(new EthTransaction().setBlockchainTransactionHash("hash1")));
    EthTransaction ethT2 = inTransaction(() -> repository.create(new EthTransaction().setBlockchainTransactionHash("hash2")));
    inTransaction(() -> repository.create(new EthTransaction()));

    // execute & verify
    Optional<EthTransaction> result = repository.findByBlockchainTransactionHash("hash1");
    assertThat(result.get().getId()).isEqualTo(ethT1.getId());

    // execute & verify
    result = repository.findByBlockchainTransactionHash("hash2");
    assertThat(result.get().getId()).isEqualTo(ethT2.getId());

    // execute & verify
    result = repository.findByBlockchainTransactionHash("");
    assertFalse(result.isPresent());

    // execute & verify
    result = repository.findByBlockchainTransactionHash(null);
    assertFalse(result.isPresent());
  }

  @Test
  public void create() {
    Long id = inTransaction(() -> repository.create(new EthTransaction()
      .setCommunityId(id("community id"))
      .setBlockchainTransactionHash("blockchain id")
      .setAmount(TEN)
      .setDescription("description")
      .setBlockchainCompletedAt(Instant.EPOCH))).getId();

    EthTransaction result = em().find(EthTransaction.class, id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getCommunityId()).isEqualTo(id("community id"));
    assertThat(result.getBlockchainTransactionHash()).isEqualTo("blockchain id");
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.00"));
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getBlockchainCompletedAt()).isEqualTo(Instant.EPOCH);
  }

  @Test
  public void update() {
    Long id = inTransaction(() -> repository.create(new EthTransaction()
      .setCommunityId(id("community id"))
      .setAmount(TEN))).getId();

    inTransaction(() -> {
      EthTransaction existing = em().find(EthTransaction.class, id);
      repository.update(existing.setAmount(ONE));
    });

    EthTransaction result = em().find(EthTransaction.class, id);
    assertThat(result.getAmount()).isEqualByComparingTo(ONE);
  }
}