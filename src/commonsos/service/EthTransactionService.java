package commonsos.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.EthBalance;
import commonsos.util.AdminUtil;

@Singleton
public class EthTransactionService {
  
  @Inject private CommunityRepository communityRepository;
  @Inject private BlockchainService blockchainService;

  public EthBalance getEthBalance(Admin admin, Long communityId) {
    if (!AdminUtil.isSeeableCommunity(admin, communityId)) throw new ForbiddenException();
    Community community = communityRepository.findStrictById(communityId);
    EthBalance balance = blockchainService.getEthBalance(community);
    return balance;
  }
}
