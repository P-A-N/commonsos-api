package commonsos.service.multithread;

import java.util.Optional;

import javax.inject.Inject;

import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.notification.PushNotificationService;
import commonsos.service.sync.SyncService;
import commonsos.util.MessageUtil;

public class CreateTokenTransactionFromUserTask extends AbstractTask {

  @Inject private BlockchainService blockchainService;
  @Inject private BlockchainEventService blockchainEventService;
  @Inject private SyncService syncService;
  @Inject private PushNotificationService pushNotificationService;
  @Inject private TokenTransactionRepository tokenTransactionRepository;
  @Inject private UserRepository userRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private MessageThreadRepository messageThreadRepository;

  private final User user;
  private final User beneficiary;
  private final Community community;
  private final CommunityToken communityToken;
  private final Ad ad;
  private final Long transactionId;
  private final Long feeTransactionId;

  public CreateTokenTransactionFromUserTask(
      User user, User beneficiary,
      Community community, CommunityToken communityToken, Ad ad,
      TokenTransaction transaction, TokenTransaction feeTransaction) {
    this.user = user;
    this.beneficiary = beneficiary;
    this.community = community;
    this.communityToken = communityToken;
    this.ad = ad;
    this.transactionId = transaction.getId();
    this.feeTransactionId = feeTransaction == null ? null : feeTransaction.getId();
  }
  
  @Override
  protected int getMaxRepeatCount() {return 1;}

  @Override
  public void runTask() {
    // create transaction
    TokenTransaction transaction = tokenTransactionRepository.findStrictById(transactionId);
    String blockchainTransactionHash = blockchainService.transferTokensFromUserToUser(user, beneficiary, community.getId(), transaction.getAmount());

    tokenTransactionRepository.lockForUpdate(transaction);
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);
    tokenTransactionRepository.update(transaction);
    commitAndStartNewTran();
    
    blockchainEventService.checkTransaction(blockchainTransactionHash);

    // create fee transaction
    if (feeTransactionId != null) {
      TokenTransaction feeTransaction = tokenTransactionRepository.findStrictById(feeTransactionId);
      String blockchainTransactionHashOfFee = blockchainService.transferTokensFee(user, community.getId(), feeTransaction.getAmount());
      
      tokenTransactionRepository.lockForUpdate(feeTransaction);
      feeTransaction.setBlockchainTransactionHash(blockchainTransactionHashOfFee);
      tokenTransactionRepository.update(feeTransaction);
      commitAndStartNewTran();
      
      blockchainEventService.checkTransaction(blockchainTransactionHashOfFee);
    }

    // create message
    MessageThread thread = null;
    if (ad != null) {
      User adCreator = userRepository.findStrictById(ad.getCreatedUserId());
      User notAdCreator = adCreator.equals(user) ? beneficiary : user;
      Optional<MessageThread> threadForAd = messageThreadRepository.findByCreaterAndAdId(notAdCreator.getId(), ad.getId());
      thread = threadForAd.orElseGet(() -> syncService.createMessageThreadForAd(adCreator, notAdCreator, ad.getId()));
    } else {
      Optional<MessageThread> threadBetweenUser = messageThreadRepository.findDirectThread(user.getId(), beneficiary.getId(), community.getId());
      thread = threadBetweenUser.orElseGet(() -> syncService.createMessageThreadWithUser(user, beneficiary, community));
    }
    String messageText = MessageUtil.getSystemMessageTokenSendFromUser(user.getUsername(), beneficiary.getUsername(), transaction.getAmount(), communityToken.getTokenSymbol(), transaction.getDescription());
    messageRepository.create(new Message()
        .setCreatedUserId(MessageUtil.getSystemMessageCreatorId())
        .setThreadId(thread.getId())
        .setText(messageText));
    commitAndStartNewTran();
    
    pushNotificationService.send(user, user, messageText, thread, messageRepository.unreadMessageCount(user.getId(), thread.getId()));
    pushNotificationService.send(user, beneficiary, messageText, thread, messageRepository.unreadMessageCount(beneficiary.getId(), thread.getId()));
  }
  
  @Override
  public String toString() {
    return String.format("%s, userId=%d, beneficiaryId=%d, communityId=%d, transactionId=%d", this.getClass().getSimpleName(), user.getId(), beneficiary.getId(), community.getId(), transactionId);
  }
}
