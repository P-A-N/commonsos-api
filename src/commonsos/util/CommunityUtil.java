package commonsos.util;

import javax.inject.Singleton;

import commonsos.repository.community.Community;
import commonsos.service.community.CommunityView;

@Singleton
public class CommunityUtil {

  public CommunityView view(Community community) {
    Long adminUserId = community.getAdminUser() == null ? null : community.getAdminUser().getId();
    return new CommunityView().setId(community.getId()).setName(community.getName()).setAdminUserId(adminUserId);
  }
}
