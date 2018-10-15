package commonsos.service.community;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.BadRequestException;
import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;

@Singleton
public class CommunityService {

  @Inject CommunityRepository repository;

  public List<CommunityView> list() {
    return repository.list().stream().map(this::view).collect(toList());
  }

  public CommunityView view(Community community) {
    Long adminUserId = community.getAdminUser() == null ? null : community.getAdminUser().getId();
    return new CommunityView().setId(community.getId()).setName(community.getName()).setAdminUserId(adminUserId);
  }

  public CommunityView view(Long id) {
    return view(community(id));
  }

  public Community community(Long id) {
    return repository.findById(id).orElseThrow(BadRequestException::new);
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }
}
