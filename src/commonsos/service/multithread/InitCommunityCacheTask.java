package commonsos.service.multithread;

import javax.inject.Inject;

import commonsos.repository.entity.Community;
import commonsos.service.blockchain.BlockchainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitCommunityCacheTask extends AbstractTask {

  @Inject BlockchainService blockchainService;

  private final Community community;

  public InitCommunityCacheTask(Community community) {
    this.community = community;
  }

  @Override
  public void runTask() {
    log.info(String.format("Init community cache start. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
    blockchainService.getCommunityToken(community.getTokenContractAddress());
    log.info(String.format("Init community cache done. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
  }
  
  @Override
  public String toString() {
    Long communityId = community == null ? null : community.getId();
    return String.format("%s, communityId=%d", this.getClass().getSimpleName(), communityId);
  }
}
