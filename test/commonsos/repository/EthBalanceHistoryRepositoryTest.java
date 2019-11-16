package commonsos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthBalanceHistory;
import commonsos.repository.entity.SortType;

public class EthBalanceHistoryRepositoryTest extends AbstractRepositoryTest {

  private EthBalanceHistoryRepository repository = spy(new EthBalanceHistoryRepository(emService));
  private CommunityRepository communityRepository = spy(new CommunityRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
    doNothing().when(communityRepository).checkLocked(any());
  }
  
  @Test
  public void findById() {
    // prepare
    Community com = inTransaction(() -> communityRepository.create(new Community().setName("com")));
    EthBalanceHistory history = inTransaction(() -> repository.create(new EthBalanceHistory()
        .setCommunity(com)
        .setBaseDate(LocalDate.parse("2019-11-16"))
        .setEthBalance(new BigDecimal("123456789.12"))));

    // execute & verify
    Optional<EthBalanceHistory> result = repository.findById(history.getId());
    assertThat(result.get().getId()).isEqualTo(history.getId());
    assertThat(result.get().getCommunity().getName()).isEqualTo("com");
    assertThat(result.get().getBaseDate()).isEqualTo(LocalDate.parse("2019-11-16"));
    assertThat(result.get().getEthBalance()).isEqualTo(new BigDecimal("123456789.12"));
  }
  
  @Test
  public void findByCommunityIdAndBaseDate() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    EthBalanceHistory h1 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-11-16"))));
    EthBalanceHistory h2 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-11-17"))));
    EthBalanceHistory h3 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com2).setBaseDate(LocalDate.parse("2019-11-16"))));

    // execute & verify
    Optional<EthBalanceHistory> result = repository.findByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-11-16"));
    assertThat(result.get().getId()).isEqualTo(h1.getId());
    
    // execute & verify
    result = repository.findByCommunityIdAndBaseDate(com2.getId(), LocalDate.parse("2019-11-16"));
    assertThat(result.get().getId()).isEqualTo(h3.getId());

    // execute & verify
    result = repository.findByCommunityIdAndBaseDate(com2.getId(), LocalDate.parse("2019-11-30"));
    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void searchByCommunityId() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    Community com3 = inTransaction(() -> communityRepository.create(new Community().setName("com3")));
    EthBalanceHistory h1 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    EthBalanceHistory h2 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    EthBalanceHistory h3 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com2)));

    // execute & verify
    List<EthBalanceHistory> result = repository.searchByCommunityId(com1.getId(), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo(h1.getId());
    assertThat(result.get(1).getId()).isEqualTo(h2.getId());

    // execute & verify
    result = repository.searchByCommunityId(com2.getId(), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getId()).isEqualTo(h3.getId());

    // execute & verify
    result = repository.searchByCommunityId(com3.getId(), null).getList();
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void searchByCommunityId_pagination() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1)));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    List<EthBalanceHistory> result = repository.searchByCommunityId(com1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(10);

    // execute & verify
    pagination.setPage(1);
    result = repository.searchByCommunityId(com1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void searchByCommunityIdAndBaseDate() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    EthBalanceHistory h1 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-30"))));
    EthBalanceHistory h2 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-31"))));
    EthBalanceHistory h3 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2020-01-01"))));
    EthBalanceHistory h4 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2020-01-02"))));
    EthBalanceHistory h5 = inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com2).setBaseDate(LocalDate.parse("2020-01-01"))));

    // execute & verify
    List<EthBalanceHistory> result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, null, null).getList();
    assertThat(result.size()).isEqualTo(4);
    assertThat(result).contains(h1, h2, h3, h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-12-29"), null, null).getList();
    assertThat(result.size()).isEqualTo(4);
    assertThat(result).contains(h1, h2, h3, h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-12-30"), null, null).getList();
    assertThat(result.size()).isEqualTo(4);
    assertThat(result).contains(h1, h2, h3, h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-12-31"), null, null).getList();
    assertThat(result.size()).isEqualTo(3);
    assertThat(result).contains(h2, h3, h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2020-01-02"), null, null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result).contains(h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2020-01-03"), null, null).getList();
    assertThat(result.size()).isEqualTo(0);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, LocalDate.parse("2020-01-03"), null).getList();
    assertThat(result.size()).isEqualTo(4);
    assertThat(result).contains(h1, h2, h3, h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, LocalDate.parse("2020-01-02"), null).getList();
    assertThat(result.size()).isEqualTo(4);
    assertThat(result).contains(h1, h2, h3, h4);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, LocalDate.parse("2020-01-01"), null).getList();
    assertThat(result.size()).isEqualTo(3);
    assertThat(result).contains(h1, h2, h3);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, LocalDate.parse("2019-12-30"), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result).contains(h1);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, LocalDate.parse("2019-12-29"), null).getList();
    assertThat(result.size()).isEqualTo(0);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-12-31"), LocalDate.parse("2019-12-31"), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result).contains(h2);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-12-31"), LocalDate.parse("2020-01-01"), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result).contains(h2, h3);

    // execute & verify
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), LocalDate.parse("2019-12-31"), LocalDate.parse("2019-12-30"), null).getList();
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void searchByCommunityIdAndBaseDate_pagination() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-01"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-02"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-03"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-04"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-05"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-06"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-07"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-08"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-09"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-10"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-11"))));
    inTransaction(() -> repository.create(new EthBalanceHistory().setCommunity(com1).setBaseDate(LocalDate.parse("2019-12-12"))));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    List<EthBalanceHistory> result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, null, pagination).getList();
    assertThat(result.size()).isEqualTo(10);

    // execute & verify
    pagination.setPage(1);
    result = repository.searchByCommunityIdAndBaseDate(com1.getId(), null, null, pagination).getList();
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void create_update() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));

    // execute create
    EthBalanceHistory history = inTransaction(() -> repository.create(new EthBalanceHistory()
        .setCommunity(com1)
        .setBaseDate(LocalDate.parse("2019-11-16"))
        .setEthBalance(new BigDecimal("123456789.12"))));

    // verify create
    Optional<EthBalanceHistory> result = repository.findById(history.getId());
    assertThat(result.get().getId()).isEqualTo(history.getId());
    assertThat(result.get().getCommunity().getName()).isEqualTo("com1");
    assertThat(result.get().getBaseDate()).isEqualTo(LocalDate.parse("2019-11-16"));
    assertThat(result.get().getEthBalance()).isEqualTo(new BigDecimal("123456789.12"));
    assertThat(result.get().getCreatedBy()).isNotNull();
    assertThat(result.get().getUpdatedBy()).isNull();
    assertThat(result.get().getCreatedAt()).isNotNull();
    assertThat(result.get().getUpdatedAt()).isNull();

    // prepare
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    
    // execute update
    inTransaction(() -> repository.update(history
        .setCommunity(com2)
        .setBaseDate(LocalDate.parse("2019-11-17"))
        .setEthBalance(new BigDecimal("1"))));

    // verify create
    result = repository.findById(history.getId());
    assertThat(result.get().getId()).isEqualTo(history.getId());
    assertThat(result.get().getCommunity().getName()).isEqualTo("com2");
    assertThat(result.get().getBaseDate()).isEqualTo(LocalDate.parse("2019-11-17"));
    assertThat(result.get().getEthBalance()).isEqualTo(new BigDecimal("1.00"));
    assertThat(result.get().getCreatedBy()).isNotNull();
    assertThat(result.get().getUpdatedBy()).isNotNull();
    assertThat(result.get().getCreatedAt()).isNotNull();
    assertThat(result.get().getUpdatedAt()).isNotNull();
  }
}