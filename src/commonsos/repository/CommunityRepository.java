package commonsos.repository;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import commonsos.exception.CommunityNotFoundException;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.ResultList;
import commonsos.service.command.PaginationCommand;

@Singleton
public class CommunityRepository extends Repository {

  @Inject
  public CommunityRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<Community> findById(Long id) {
    return ofNullable(em().find(Community.class, id, lockMode()));
  }

  public Community findStrictById(Long id) {
    return findById(id).orElseThrow(CommunityNotFoundException::new);
  }

  public ResultList<Community> list(String filter, PaginationCommand pagination) {
    TypedQuery<Community> query = em().createQuery(
        "FROM Community" +
        " WHERE tokenContractAddress IS NOT NULL" +
        " AND LOWER(name) LIKE LOWER(:filter)" +
        " ORDER BY id", Community.class)
        .setLockMode(lockMode())
        .setParameter("filter", "%"+filter+"%");
    
    ResultList<Community> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<Community> list(PaginationCommand pagination) {
    TypedQuery<Community> query = em().createQuery(
        "FROM Community" +
        " WHERE tokenContractAddress IS NOT NULL" +
        " ORDER BY id", Community.class)
        .setLockMode(lockMode());
    
    ResultList<Community> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<CommunityUser> list(String filter, List<CommunityUser> communityUsers, PaginationCommand pagination) {
    if (communityUsers == null || communityUsers.isEmpty()) {
      return new ResultList<CommunityUser>().setList(Collections.emptyList());
    }
    
    List<Long> ids = communityUsers.stream().map(CommunityUser::getCommunity).map(Community::getId).collect(Collectors.toList());
    
    TypedQuery<CommunityUser> query = em().createQuery(
        "FROM CommunityUser" +
        " WHERE community.id IN (:ids)" +
        " AND community.tokenContractAddress IS NOT NULL" +
        " AND LOWER(community.name) LIKE LOWER(:filter)" +
        " ORDER BY community.id", CommunityUser.class)
        .setLockMode(lockMode())
        .setParameter("ids", ids)
        .setParameter("filter", "%"+filter+"%");
    
    ResultList<CommunityUser> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<CommunityUser> list(List<CommunityUser> communityUsers, PaginationCommand pagination) {
    if (communityUsers == null || communityUsers.isEmpty()) {
      return new ResultList<CommunityUser>().setList(Collections.emptyList());
    }
    
    List<Long> ids = communityUsers.stream().map(CommunityUser::getCommunity).map(Community::getId).collect(Collectors.toList());
    
    TypedQuery<CommunityUser> query = em().createQuery(
        "FROM CommunityUser" +
        " WHERE community.id IN (:ids)" +
        " AND community.tokenContractAddress IS NOT NULL" +
        " ORDER BY community.id", CommunityUser.class)
        .setLockMode(lockMode())
        .setParameter("ids", ids);
    
    ResultList<CommunityUser> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public Community create(Community community) {
    em().persist(community);
    return community;
  }

  public boolean isAdmin(Long userId, Long communityId) {
    Optional<Community> community = findById(communityId);
    return community.isPresent()
        && community.get().getAdminUser() != null
        && community.get().getAdminUser().getId().equals(userId);
  }

  public Community update(Community community) {
    em().merge(community);
    return community;
  }
}
