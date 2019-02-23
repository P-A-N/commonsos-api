package commonsos.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.command.UploadPhotoCommand;
import commonsos.service.image.ImageUploadService;
import commonsos.util.CommunityUtil;
import commonsos.view.CommunityView;

@Singleton
public class CommunityService {

  @Inject CommunityRepository repository;
  @Inject private ImageUploadService imageService;
  @Inject BlockchainService blockchainService;

  public List<CommunityView> usersCommunitylist(User user, String filter) {
    List<CommunityView> communityList = StringUtils.isEmpty(filter) ? list() : list(filter);
    Set<Long> idSet = user.getCommunityUserList().stream().map(CommunityUser::getCommunity).map(Community::getId).collect(toSet());
    
    return communityList.stream().filter(c -> idSet.contains(c.getId())).collect(toList());
  }
  
  public List<CommunityView> list(String filter) {
    if (StringUtils.isEmpty(filter)) {
      return list();
    } else {
      return repository.list(filter).stream().map(c -> CommunityUtil.view(c, blockchainService.tokenSymbol(c.getId()))).collect(toList());
    }
  }
  
  public List<CommunityView> list() {
    return repository.list().stream().map(c -> CommunityUtil.view(c, blockchainService.tokenSymbol(c.getId()))).collect(toList());
  }

  public Community community(Long id) {
    return repository.findById(id).orElseThrow(() -> new BadRequestException("community not found"));
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");
    
    String url = imageService.create(command);
    imageService.delete(community.getPhotoUrl());
    
    community.setPhotoUrl(url);
    repository.update(community);
    return url;
  }

  public String updateCoverPhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");
    
    String url = imageService.create(command);
    imageService.delete(community.getCoverPhotoUrl());
    
    community.setCoverPhotoUrl(url);
    repository.update(community);
    return url;
  }
}
