package commonsos.service.community;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.BadRequestException;
import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;
import commonsos.util.CommunityUtil;

@Singleton
public class CommunityService {

  @Inject CommunityRepository repository;
  @Inject CommunityUtil communityUtil;

  public List<CommunityView> list() {
    return repository.list().stream().map(communityUtil::view).collect(toList());
  }

  public CommunityView view(Long id) {
    return communityUtil.view(community(id));
  }

  public Community community(Long id) {
    return repository.findById(id).orElseThrow(() -> new BadRequestException("community not found"));
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }
}
