package commonsos.repository.community;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.EntityManagerService;
import commonsos.Repository;

@Singleton
public class CommunityRepository extends Repository {

  @Inject
  public CommunityRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<Community> findById(Long id) {
    return ofNullable(em().find(Community.class, id));
  }

  public List<Community> list() {
    return em().createQuery("FROM Community WHERE tokenContractAddress IS NOT NULL", Community.class).getResultList();
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
