package commonsos.service.multithread;

import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;

import java.math.BigInteger;

import javax.inject.Inject;

import org.web3j.crypto.Credentials;

import commonsos.Configuration;
import commonsos.command.admin.CreateCommunityCommand;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;
import commonsos.service.blockchain.BlockchainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateCommunityTask extends AbstractTask {

  @Inject CommunityRepository communityRepository;
  @Inject BlockchainService blockchainService;
  @Inject private Configuration config;

  private final Long communityId;
  private final CreateCommunityCommand command;

  public CreateCommunityTask(Long communityId, CreateCommunityCommand command) {
    this.communityId = communityId;
    this.command = command;
  }

  @Override
  public void runTask() {
    log.info(String.format("Create community task start. [communityId=%d]", communityId));
    Community community = communityRepository.findStrictById(communityId);
    
    Credentials systemCredentials = blockchainService.systemCredentials();
    Credentials mainCredentials = blockchainService.credentials(community.getMainWallet(), config.communityWalletPassword());
    Credentials feeCredentials = blockchainService.credentials(community.getFeeWallet(), config.communityWalletPassword());

    BigInteger initialWei = new BigInteger(config.initialWei());
    
    // transfer ether to main wallet
    blockchainService.transferEther(systemCredentials, mainCredentials.getAddress(), initialWei, true);

    // create token
    String tokenAddress = blockchainService.createToken(mainCredentials, command.getTokenSymbol(), command.getTokenName());

    communityRepository.lockForUpdate(community);
    community.setTokenContractAddress(tokenAddress);
    communityRepository.update(community);

    // approve main wallet from fee wallet
    blockchainService.transferEther(systemCredentials, feeCredentials.getAddress(), TOKEN_TRANSFER_GAS_LIMIT.multiply(BigInteger.TEN).multiply(config.gasPrice()), true);
    blockchainService.approveFromFeeWallet(community);
    
    log.info(String.format("Create community task done. [communityId=%d, communityName=%s]", community.getId(), community.getName()));
  }
  
  @Override
  public String toString() {
    return String.format("%s, communityId=%d", this.getClass().getSimpleName(), communityId);
  }
}
