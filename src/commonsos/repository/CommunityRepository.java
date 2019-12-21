package commonsos.repository;

import static java.util.Optional.empty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import commonsos.command.PaginationCommand;
import commonsos.exception.CommunityNotFoundException;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.ResultList;

@Singleton
public class CommunityRepository extends Repository {

  @Inject
  public CommunityRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<Community> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM Community WHERE id = :id AND deleted IS FALSE", Community.class)
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public Community findStrictById(Long id) {
    return findById(id).orElseThrow(CommunityNotFoundException::new);
  }

  public Optional<Community> findPublicById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM Community WHERE id = :id AND publishStatus = 'PUBLIC' AND deleted IS FALSE", Community.class)
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public Community findPublicStrictById(Long id) {
    return findPublicById(id).orElseThrow(CommunityNotFoundException::new);
  }

  public ResultList<Community> searchAll(PaginationCommand pagination) {
    TypedQuery<Community> query = em().createQuery(
        "FROM Community" +
        " WHERE deleted IS FALSE" +
        " ORDER BY id", Community.class);
    
    ResultList<Community> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<Community> searchPublic(String filter, PaginationCommand pagination) {
    TypedQuery<Community> query = em().createQuery(
        "FROM Community" +
        " WHERE tokenContractAddress IS NOT NULL" +
        " AND LOWER(name) LIKE LOWER(:filter)" +
        " AND publishStatus = 'PUBLIC'" +
        " AND deleted IS FALSE" +
        " ORDER BY id", Community.class)
        .setParameter("filter", "%"+filter+"%");
    
    ResultList<Community> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<Community> searchPublic(PaginationCommand pagination) {
    TypedQuery<Community> query = em().createQuery(
        "FROM Community" +
        " WHERE tokenContractAddress IS NOT NULL" +
        " AND publishStatus = 'PUBLIC'" +
        " AND deleted IS FALSE" +
        " ORDER BY id", Community.class);
    
    ResultList<Community> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<CommunityUser> searchPublic(String filter, List<CommunityUser> communityUsers, PaginationCommand pagination) {
    if (communityUsers == null || communityUsers.isEmpty()) {
      return new ResultList<CommunityUser>().setList(Collections.emptyList());
    }
    
    List<Long> ids = communityUsers.stream().map(CommunityUser::getCommunity).map(Community::getId).collect(Collectors.toList());
    
    TypedQuery<CommunityUser> query = em().createQuery(
        "FROM CommunityUser" +
        " WHERE community.id IN (:ids)" +
        " AND community.tokenContractAddress IS NOT NULL" +
        " AND LOWER(community.name) LIKE LOWER(:filter)" +
        " AND community.publishStatus = 'PUBLIC'" +
        " AND community.deleted IS FALSE" +
        " ORDER BY community.id", CommunityUser.class)
        .setParameter("ids", ids)
        .setParameter("filter", "%"+filter+"%");
    
    ResultList<CommunityUser> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public ResultList<CommunityUser> searchPublic(List<CommunityUser> communityUsers, PaginationCommand pagination) {
    if (communityUsers == null || communityUsers.isEmpty()) {
      return new ResultList<CommunityUser>().setList(Collections.emptyList());
    }
    
    List<Long> ids = communityUsers.stream().map(CommunityUser::getCommunity).map(Community::getId).collect(Collectors.toList());
    
    TypedQuery<CommunityUser> query = em().createQuery(
        "FROM CommunityUser" +
        " WHERE community.id IN (:ids)" +
        " AND community.tokenContractAddress IS NOT NULL" +
        " AND community.publishStatus = 'PUBLIC'" +
        " AND community.deleted IS FALSE" +
        " ORDER BY community.id", CommunityUser.class)
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
    checkLocked(community);
    return em().merge(community);
  }
}
