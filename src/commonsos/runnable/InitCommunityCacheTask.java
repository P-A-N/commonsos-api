package commonsos.runnable;

import javax.inject.Inject;

import commonsos.repository.entity.Community;
import commonsos.service.blockchain.BlockchainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitCommunityCacheTask implements Runnable {

  @Inject BlockchainService blockchainService;

  private final Community community;

  public InitCommunityCacheTask(Community community) {
    this.community = community;
  }

  @Override
  public void run() {
    log.info(String.format("Init community cache start. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
    blockchainService.getCommunityToken(community.getTokenContractAddress());
    log.info(String.format("Init community cache done. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
  }
}
