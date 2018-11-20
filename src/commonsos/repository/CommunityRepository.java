package commonsos.repository;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.CommunityNotFoundException;
import commonsos.repository.entity.Community;

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

  public List<Community> list() {
    return em().createQuery("FROM Community WHERE tokenContractAddress IS NOT NULL", Community.class)
        .setLockMode(lockMode())
        .getResultList();
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
}
