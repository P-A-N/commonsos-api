package commonsos.service.multithread;

import java.util.Optional;

import javax.inject.Inject;

import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.notification.PushNotificationService;
import commonsos.service.sync.SyncService;
import commonsos.util.MessageUtil;

public class CreateTokenTransactionFromAdminTask extends AbstractTask {

  @Inject private BlockchainService blockchainService;
  @Inject private BlockchainEventService blockchainEventService;
  @Inject private SyncService syncService;
  @Inject private PushNotificationService pushNotificationService;
  @Inject private TokenTransactionRepository tokenTransactionRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private MessageThreadRepository messageThreadRepository;

  private final Community community;
  private final WalletType walletType;
  private final CommunityToken communityToken;
  private final User beneficiary;
  private final Long transactionId;

  public CreateTokenTransactionFromAdminTask(
      Community community, WalletType walletType, CommunityToken communityToken,
      User beneficiary, TokenTransaction transaction) {
    this.community = community;
    this.walletType = walletType;
    this.communityToken = communityToken;
    this.beneficiary = beneficiary;
    this.transactionId = transaction.getId();
  }
  
  @Override
  protected int getMaxRepeatCount() {return 1;}

  @Override
  public void runTask() {
    // create transaction
    TokenTransaction transaction = tokenTransactionRepository.findStrictById(transactionId);
    String blockchainTransactionHash = blockchainService.transferTokensFromCommunity(community, walletType, beneficiary, transaction.getAmount());

    tokenTransactionRepository.lockForUpdate(transaction);
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);
    tokenTransactionRepository.update(transaction);
    commitAndStartNewTran();

    blockchainEventService.checkTransaction(blockchainTransactionHash);

    // create message
    Optional<MessageThread> threadBetweenUser = messageThreadRepository.findDirectThread(beneficiary.getId(), MessageUtil.getSystemMessageCreatorId(), community.getId());
    MessageThread thread = threadBetweenUser.orElseGet(() -> syncService.createMessageThreadWithSystem(beneficiary, community));
    String messageText = MessageUtil.getSystemMessageTokenSend2(community.getName(), beneficiary.getUsername(), transaction.getAmount(), communityToken.getTokenSymbol());
    messageRepository.create(new Message()
        .setCreatedUserId(MessageUtil.getSystemMessageCreatorId())
        .setThreadId(thread.getId())
        .setText(messageText));
    commitAndStartNewTran();
    
    Integer unreadCount = messageRepository.unreadMessageCount(beneficiary.getId(), thread.getId());
    pushNotificationService.send(community, beneficiary, messageText, thread, unreadCount);
  }
  
  @Override
  public String toString() {
    return String.format("%s, communityId=%d, wallet=%s, beneficiaryId=%d, transactionId=%d", this.getClass().getSimpleName(), community.getId(), walletType.name(), beneficiary.getId(), transactionId);
  }
}
