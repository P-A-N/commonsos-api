package commonsos.repository;

import static javax.persistence.LockModeType.PESSIMISTIC_READ;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.hibernate.ScrollableResults;

import commonsos.ThreadValue;
import commonsos.controller.command.PaginationCommand;
import commonsos.repository.entity.ResultList;
import commonsos.util.PaginationUtil;

public abstract class Repository {
  private final EntityManagerService entityManagerService;

  public Repository(EntityManagerService entityManagerService) {
    this.entityManagerService = entityManagerService;
  }

  protected EntityManager em() {
    return entityManagerService.get();
  }

  protected LockModeType lockMode() {
    return ThreadValue.isReadOnly() ? PESSIMISTIC_READ : PESSIMISTIC_WRITE;
  }
  
  protected <T> ResultList<T> getResultList(TypedQuery<T> query, PaginationCommand pagination) {
    if (pagination == null) {
      List<T> list = query.getResultList();
      return new ResultList<T>().setList(list);
    }

    PaginationUtil.validateCommand(pagination);
    
    switch (pagination.getSort()) {
    case DESC:
      return getResultListDesc(query, pagination);
    case ASC:
    default:
      return getResultListAsc(query, pagination);
    }
  }
  
  private <T> ResultList<T> getResultListAsc(TypedQuery<T> query, PaginationCommand pagination) {
    ScrollableResults scrollable = query.unwrap(org.hibernate.query.Query.class).scroll();
    ResultList<T> resultList = new ResultList<>(pagination);
    if (scrollable.last()) {
      resultList.setLastPage(scrollable.getRowNumber() / pagination.getSize());
      scrollable.first();
      
      if (scrollable.scroll(pagination.getPage() * pagination.getSize())) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < pagination.getSize(); i++) {
          list.add((T) scrollable.get(0));
          if (!scrollable.next()) break;
        }

        resultList.setList(list);
      } else {
        resultList.setList(Collections.emptyList());
      }
    } else {
      resultList.setList(Collections.emptyList());
      resultList.setLastPage(0);
    }
    
    return resultList;
  }
  
  private <T> ResultList<T> getResultListDesc(TypedQuery<T> query, PaginationCommand pagination) {
    ScrollableResults scrollable = query.unwrap(org.hibernate.query.Query.class).scroll();
    ResultList<T> resultList = new ResultList<>(pagination);
    if (scrollable.last()) {
      resultList.setLastPage(scrollable.getRowNumber() / pagination.getSize());
      scrollable.beforeFirst();
      
      if (scrollable.scroll(-1 - (pagination.getPage() * pagination.getSize()))) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < pagination.getSize(); i++) {
          list.add((T) scrollable.get(0));
          if (!scrollable.previous()) break;
        }

        resultList.setList(list);
      } else {
        resultList.setList(Collections.emptyList());
      }
    } else {
      resultList.setList(Collections.emptyList());
      resultList.setLastPage(0);
    }
    
    return resultList;
  }
}
