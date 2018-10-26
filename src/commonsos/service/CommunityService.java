package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.BadRequestException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.util.CommunityUtil;
import commonsos.view.CommunityView;

@Singleton
public class CommunityService {

  @Inject CommunityRepository repository;

  public List<CommunityView> list() {
    return repository.list().stream().map(CommunityUtil::view).collect(toList());
  }

  public CommunityView view(Long id) {
    return CommunityUtil.view(community(id));
  }

  public Community community(Long id) {
    return repository.findById(id).orElseThrow(() -> new BadRequestException("community not found"));
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }
}
