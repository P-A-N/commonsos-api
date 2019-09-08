package commonsos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.persistence.TypedQuery;

import org.junit.jupiter.api.Test;

import commonsos.controller.command.PaginationCommand;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;

public class RepositoryTest extends AbstractRepositoryTest {

  private UserRepository repository = new UserRepository(emService) {};

  @Test
  public void getResultListNoPagination() {
    // prepare
    inTransaction(() -> repository.create(new User().setUsername("user1")));
    inTransaction(() -> repository.create(new User().setUsername("user2")));
    inTransaction(() -> repository.create(new User().setUsername("user3")));
    inTransaction(() -> repository.create(new User().setUsername("user4")));
    inTransaction(() -> repository.create(new User().setUsername("user5")));

    // execute (no pagination)
    TypedQuery<User> query = em().createQuery("FROM User u ORDER BY u.id", User.class);
    ResultList<User> result = repository.getResultList(query, null);
    
    // verify
    assertThat(result.getList().size()).isEqualTo(5);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user1");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("user2");
    assertThat(result.getList().get(2).getUsername()).isEqualTo("user3");
    assertThat(result.getList().get(3).getUsername()).isEqualTo("user4");
    assertThat(result.getList().get(4).getUsername()).isEqualTo("user5");
    assertThat(result.getPage()).isNull();
    assertThat(result.getSize()).isNull();
    assertThat(result.getSort()).isNull();
    assertThat(result.getLastPage()).isNull();
  }

  @Test
  public void getResultListAsc() {
    // prepare
    inTransaction(() -> repository.create(new User().setUsername("user1")));
    inTransaction(() -> repository.create(new User().setUsername("user2")));
    inTransaction(() -> repository.create(new User().setUsername("user3")));
    inTransaction(() -> repository.create(new User().setUsername("user4")));
    inTransaction(() -> repository.create(new User().setUsername("user5")));

    // execute (page 0 size 2 asc)
    TypedQuery<User> query = em().createQuery("FROM User u ORDER BY u.id", User.class);
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(2).setSort(SortType.ASC);
    ResultList<User> result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user1");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("user2");
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.ASC);
    assertThat(result.getLastPage()).isEqualTo(2);

    // execute (page 1 size 2 asc)
    pagination = new PaginationCommand().setPage(1).setSize(2).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user3");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("user4");
    assertThat(result.getPage()).isEqualTo(1);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.ASC);
    assertThat(result.getLastPage()).isEqualTo(2);

    // execute (page 2 size 2 asc)
    pagination = new PaginationCommand().setPage(2).setSize(2).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user5");
    assertThat(result.getPage()).isEqualTo(2);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.ASC);
    assertThat(result.getLastPage()).isEqualTo(2);

    // execute (page 3 size 2 asc)
    pagination = new PaginationCommand().setPage(3).setSize(2).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getPage()).isEqualTo(3);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.ASC);
    assertThat(result.getLastPage()).isEqualTo(2);

    // execute (page 1 size 4 asc)
    pagination = new PaginationCommand().setPage(1).setSize(4).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    
    // execute (page 1 size 5 asc)
    pagination = new PaginationCommand().setPage(1).setSize(5).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
    
    // execute (page 1 size 6 asc)
    pagination = new PaginationCommand().setPage(1).setSize(6).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);

    // execute & verify (page 0 size 1 asc)
    pagination = new PaginationCommand().setPage(0).setSize(1).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getLastPage()).isEqualTo(4);

    // execute & verify (page 4 size 1 asc)
    pagination = new PaginationCommand().setPage(4).setSize(1).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getLastPage()).isEqualTo(4);

    // execute & verify (page 5 size 1 asc)
    pagination = new PaginationCommand().setPage(5).setSize(1).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getLastPage()).isEqualTo(4);

    // execute & verify (page 0 size 5 asc)
    pagination = new PaginationCommand().setPage(0).setSize(5).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(5);
    assertThat(result.getLastPage()).isEqualTo(0);

    // execute & verify (page 1 size 5 asc)
    pagination = new PaginationCommand().setPage(1).setSize(5).setSort(SortType.ASC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getLastPage()).isEqualTo(0);
  }
  
  @Test
  public void getResultListDesc() {
    // prepare
    inTransaction(() -> repository.create(new User().setUsername("user1")));
    inTransaction(() -> repository.create(new User().setUsername("user2")));
    inTransaction(() -> repository.create(new User().setUsername("user3")));
    inTransaction(() -> repository.create(new User().setUsername("user4")));
    inTransaction(() -> repository.create(new User().setUsername("user5")));

    // execute (page 0 size 2 desc)
    TypedQuery<User> query = em().createQuery("FROM User u ORDER BY u.id", User.class);
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(2).setSort(SortType.DESC);
    ResultList<User> result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user5");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("user4");
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.DESC);
    assertThat(result.getLastPage()).isEqualTo(2);

    // execute (page 1 size 2 desc)
    pagination = new PaginationCommand().setPage(1).setSize(2).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(2);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user3");
    assertThat(result.getList().get(1).getUsername()).isEqualTo("user2");
    assertThat(result.getPage()).isEqualTo(1);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.DESC);
    assertThat(result.getLastPage()).isEqualTo(2);

    // execute (page 2 size 2 desc)
    pagination = new PaginationCommand().setPage(2).setSize(2).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getList().get(0).getUsername()).isEqualTo("user1");
    assertThat(result.getPage()).isEqualTo(2);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.DESC);
    assertThat(result.getLastPage()).isEqualTo(2);
    
    // execute (page 3 size 2 desc)
    pagination = new PaginationCommand().setPage(3).setSize(2).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getPage()).isEqualTo(3);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.DESC);
    assertThat(result.getLastPage()).isEqualTo(2);


    // execute (page 1 size 4 desc)
    pagination = new PaginationCommand().setPage(1).setSize(4).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(1);
    
    // execute (page 1 size 5 desc)
    pagination = new PaginationCommand().setPage(1).setSize(5).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
    
    // execute (page 1 size 6 desc)
    pagination = new PaginationCommand().setPage(1).setSize(6).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);

    // execute & verify (page 0 size 1 desc)
    pagination = new PaginationCommand().setPage(0).setSize(1).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getLastPage()).isEqualTo(4);

    // execute & verify (page 4 size 1 desc)
    pagination = new PaginationCommand().setPage(4).setSize(1).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(1);
    assertThat(result.getLastPage()).isEqualTo(4);

    // execute & verify (page 5 size 1 desc)
    pagination = new PaginationCommand().setPage(5).setSize(1).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getLastPage()).isEqualTo(4);

    // execute & verify (page 0 size 5 desc)
    pagination = new PaginationCommand().setPage(0).setSize(5).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(5);
    assertThat(result.getLastPage()).isEqualTo(0);

    // execute & verify (page 1 size 5 desc)
    pagination = new PaginationCommand().setPage(1).setSize(5).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getLastPage()).isEqualTo(0);
  }

  @Test
  public void getResultList_emptyResult() {
    // prepare
    // nothing
    
    // execute (page 0 size 2 asc)
    TypedQuery<User> query = em().createQuery("FROM User u ORDER BY u.id", User.class);
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(2).setSort(SortType.ASC);
    ResultList<User> result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.ASC);
    assertThat(result.getLastPage()).isEqualTo(0);
    
    // execute (page 0 size 2 desc)
    pagination = new PaginationCommand().setPage(0).setSize(2).setSort(SortType.DESC);
    result = repository.getResultList(query, pagination);

    // verify
    assertThat(result.getList().size()).isEqualTo(0);
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(2);
    assertThat(result.getSort()).isEqualTo(SortType.DESC);
    assertThat(result.getLastPage()).isEqualTo(0);
  }
  
  @Test
  public void getResultList_invalid() {
    // execute (page -1)
    TypedQuery<User> query = em().createQuery("FROM User u ORDER BY u.id", User.class);
    PaginationCommand pagination = new PaginationCommand().setPage(-1).setSize(2).setSort(SortType.ASC);
    assertThrows(BadRequestException.class, () -> repository.getResultList(query, pagination));

    // execute (size 0)
    pagination.setPage(0).setSize(0).setSort(SortType.ASC);
    assertThrows(BadRequestException.class, () -> repository.getResultList(query, pagination));

    // execute (size -1)
    pagination.setPage(0).setSize(-1).setSort(SortType.ASC);
    assertThrows(BadRequestException.class, () -> repository.getResultList(query, pagination));

    // execute (sort null)
    pagination.setPage(0).setSize(2).setSort(null);
    assertThrows(BadRequestException.class, () -> repository.getResultList(query, pagination));
  }
}