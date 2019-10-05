package commonsos.service;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;

import commonsos.command.PaginationCommand;
import commonsos.command.admin.CreateTokenTransactionFromAdminCommand;
import commonsos.command.app.TransactionCreateCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.AdminRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.TokenBalance;
import commonsos.service.notification.PushNotificationService;
import commonsos.service.sync.SyncService;
import commonsos.util.AdminUtil;
import commonsos.util.MessageUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.view.TokenTransactionListView;
import commonsos.view.TokenTransactionView;
import lombok.extern.slf4j.Slf4j;
import spark.utils.StringUtils;

@Singleton
@Slf4j
public class TokenTransactionService {
  
  @Inject private TokenTransactionRepository repository;
  @Inject private UserRepository userRepository;
  @Inject private AdminRepository adminRepository;
  @Inject private AdRepository adRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private BlockchainService blockchainService;
  @Inject private BlockchainEventService blockchainEventService;
  @Inject private SyncService syncService;
  @Inject private PushNotificationService pushNotificationService;

  public TokenBalance getTokenBalanceForAdmin(Admin admin, Long communityId, WalletType walletType) {
    if (!AdminUtil.isSeeableCommunity(admin, communityId)) throw new ForbiddenException();
    if (Role.TELLER.equals(admin.getRole())) throw new ForbiddenException();
    Community com = communityRepository.findStrictById(communityId);
    TokenBalance tokenBalance = blockchainService.getTokenBalance(com, walletType);
    return tokenBalance;
  }
  
  private boolean isDebit(User user, TokenTransaction transaction) {
    return transaction.getRemitterUserId() != null && transaction.getRemitterUserId().equals(user.getId());
  }

  private boolean isDebitOfCommunity(TokenTransaction transaction) {
    return transaction.isFromAdmin();
  }

  @Deprecated
  public TokenTransactionListView transactionsForAdminUser(User admin, Long communityId, Long userId, PaginationCommand pagination) {
    if (!UserUtil.isAdmin(admin, communityId)) throw new ForbiddenException(String.format("user is not a admin.[userId=%d, communityId=%d]", admin.getId(), communityId));
    
    User user = userRepository.findStrictById(userId);
    return searchUserTranByUser(user, communityId, pagination);
  }

  public TokenTransactionListView searchUserTranByUser(User user, Long communityId, PaginationCommand pagination) {
    communityRepository.findPublicStrictById(communityId);
    ResultList<TokenTransaction> result = repository.searchUserTran(user, communityId, null);

    List<TokenTransactionView> transactionViews = result.getList().stream()
        .sorted(Comparator.comparing(TokenTransaction::getCreatedAt).reversed())
        .map(transaction -> viewForUser(user, transaction))
        .collect(toList());
    
    TokenTransactionListView listView = new TokenTransactionListView();
    listView.setPagination(PaginationUtil.toView(transactionViews, pagination));
    List<TokenTransactionView> paginationedViews = PaginationUtil.pagination(transactionViews, pagination);
    listView.setTransactionList(paginationedViews);
    
    return listView;
  }

  public TokenTransactionListView searchCommunityTranByAdmin(Admin admin, Long communityId, WalletType walletType, PaginationCommand pagination) {
    communityRepository.findStrictById(communityId);
    if (!AdminUtil.isSeeableCommunity(admin, communityId, false)) throw new ForbiddenException();
    
    ResultList<TokenTransaction> result = repository.searchCommunityTran(communityId, walletType, null);

    List<TokenTransactionView> transactionViews = result.getList().stream()
        .sorted(Comparator.comparing(TokenTransaction::getCreatedAt).reversed())
        .map(transaction -> viewForAdmin(transaction))
        .collect(toList());
    
    TokenTransactionListView listView = new TokenTransactionListView();
    listView.setPagination(PaginationUtil.toView(transactionViews, pagination));
    List<TokenTransactionView> paginationedViews = PaginationUtil.pagination(transactionViews, pagination);
    listView.setTransactionList(paginationedViews);
    
    return listView;
  }

  public TokenTransactionListView searchUserTranByAdmin(Long userId, Long communityId, PaginationCommand pagination) {
    User user = userRepository.findStrictById(userId);
    communityRepository.findStrictById(communityId);
    ResultList<TokenTransaction> result = repository.searchUserTran(user, communityId, null);

    List<TokenTransactionView> transactionViews = result.getList().stream()
        .sorted(Comparator.comparing(TokenTransaction::getCreatedAt).reversed())
        .map(transaction -> viewForAdmin(transaction))
        .collect(toList());
    
    TokenTransactionListView listView = new TokenTransactionListView();
    listView.setPagination(PaginationUtil.toView(transactionViews, pagination));
    List<TokenTransactionView> paginationedViews = PaginationUtil.pagination(transactionViews, pagination);
    listView.setTransactionList(paginationedViews);
    
    return listView;
  }

  public TokenTransactionView viewForUser(User user, TokenTransaction transaction) {
    TokenTransactionView view = new TokenTransactionView()
      .setCommunityId(transaction.getCommunityId())
      .setIsFromAdmin(transaction.isFromAdmin())
      .setAmount(transaction.getAmount())
      .setDescription(transaction.getDescription())
      .setCreatedAt(transaction.getCreatedAt())
      .setCompleted(transaction.getBlockchainCompletedAt() != null)
      .setDebit(isDebit(user, transaction));

    Optional<User> remitter = userRepository.findById(transaction.getRemitterUserId());
    Optional<User> beneficiary = userRepository.findById(transaction.getBeneficiaryUserId());
    
    if (remitter.isPresent()) view.setRemitter(UserUtil.publicViewForApp(remitter.get()));
    if (beneficiary.isPresent()) view.setBeneficiary(UserUtil.publicViewForApp(beneficiary.get()));
    
    return view;
  }

  public TokenTransactionView viewForAdmin(TokenTransaction transaction) {
    TokenTransactionView view = new TokenTransactionView()
      .setCommunityId(transaction.getCommunityId())
      .setWallet(transaction.getWalletDivision())
      .setIsFromAdmin(transaction.isFromAdmin())
      .setAmount(transaction.getAmount())
      .setCreatedAt(transaction.getCreatedAt())
      .setCompleted(transaction.getBlockchainCompletedAt() != null)
      .setDebit(isDebitOfCommunity(transaction));

    Optional<Admin> remitterAdmin = adminRepository.findById(transaction.getRemitterAdminId());
    Optional<User> remitter = userRepository.findById(transaction.getRemitterUserId());
    Optional<User> beneficiary = userRepository.findById(transaction.getBeneficiaryUserId());

    if (remitterAdmin.isPresent()) {
      view.setRemitterAdmin(AdminUtil.narrowView(remitterAdmin.get()));
    }
    if (remitter.isPresent()) {
      view.setRemitter(UserUtil.narrowViewForAdmin(remitter.get()));
    }
    if (beneficiary.isPresent()) {
      view.setBeneficiary(UserUtil.narrowViewForAdmin(beneficiary.get()));
    }
    
    return view;
  }

  public TokenTransaction create(User user, TransactionCreateCommand command) {
    // validation
    if (command.getCommunityId() == null) throw new BadRequestException("communityId is required");
    if (ZERO.compareTo(command.getAmount()) > -1)  throw new BadRequestException("sending negative point");
    if (user.getId().equals(command.getBeneficiaryId())) throw new BadRequestException("user is beneficiary");
    User beneficiary = userRepository.findStrictById(command.getBeneficiaryId());
    
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    if (command.getTransactionFee() == null || community.getFee().compareTo(command.getTransactionFee()) != 0) throw new DisplayableException("error.feeIncorrect");
    if (!UserUtil.isMember(user, community)) throw new DisplayableException("error.userIsNotCommunityMember");
    if (!UserUtil.isMember(beneficiary, community)) throw new DisplayableException("error.beneficiaryIsNotCommunityMember");

    if (command.getAdId() != null) {
      Ad ad = adRepository.findStrict(command.getAdId());
      if (!ad.getCommunityId().equals(community.getId())) throw new BadRequestException("communityId does not match with ad");
    }
    TokenBalance tokenBalance = blockchainService.getTokenBalance(user, command.getCommunityId());
    if (tokenBalance.getBalance().compareTo(command.getAmount()) < 0) throw new DisplayableException("error.notEnoughFunds");

    // create transaction
    TokenTransaction transaction = new TokenTransaction()
      .setCommunityId(command.getCommunityId())
      .setRemitterUserId(user.getId())
      .setAmount(command.getAmount())
      .setFee(community.getFee())
      .setBeneficiaryUserId(command.getBeneficiaryId())
      .setDescription(StringUtils.isBlank(command.getDescription()) ? null : command.getDescription())
      .setAdId(command.getAdId());

    repository.create(transaction);

    String blockchainTransactionHash = blockchainService.transferTokens(user, beneficiary, command.getCommunityId(), transaction.getAmount());
    
    repository.lockForUpdate(transaction);
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);
    repository.update(transaction);
    
    blockchainEventService.checkTransaction(blockchainTransactionHash);

    // create message
    MessageThread thread = null;
    if (command.getAdId() != null) {
      Optional<MessageThread> threadForAd = messageThreadRepository.byCreaterAndAdId(user.getId(), command.getAdId());
      thread = threadForAd.orElseGet(() -> syncService.createMessageThreadForAd(user, command.getAdId()));
    } else {
      Optional<MessageThread> threadBetweenUser = messageThreadRepository.betweenUsers(user.getId(), beneficiary.getId(), community.getId());
      thread = threadBetweenUser.orElseGet(() -> syncService.createMessageThreadWithUser(user, beneficiary, community));
    }
    
    String messageText = MessageUtil.getSystemMessageTokenSend1(user.getUsername(), beneficiary.getUsername(), command.getAmount(), tokenBalance.getToken().getTokenSymbol(), command.getDescription());
    Map<String, String> params = ImmutableMap.of(
        "type", "new_message",
        "threadId", Long.toString(thread.getId()));
    messageRepository.create(new Message()
        .setCreatedUserId(MessageUtil.getSystemMessageCreatorId())
        .setThreadId(thread.getId())
        .setText(messageText));
    pushNotificationService.send(user, messageText, params);
    pushNotificationService.send(beneficiary, messageText, params);

    return transaction;
  }

  public TokenTransaction create(Admin admin, CreateTokenTransactionFromAdminCommand command) {
    // validation
    if (command.getCommunityId() == null) throw new BadRequestException("communityId is required");
    if (command.getAmount() == null) throw new BadRequestException("amount is required");
    if (command.getWallet() == null) throw new BadRequestException("wallet is required");
    if (command.getBeneficiaryUserId() == null) throw new BadRequestException("beneficiaryUser is required");
    
    Community community = communityRepository.findStrictById(command.getCommunityId());
    if (community.getStatus() != PUBLIC) throw new DisplayableException("error.CommunityIsNotPublic");
    
    User beneficiary = userRepository.findStrictById(command.getBeneficiaryUserId());
    if (!UserUtil.isMember(beneficiary, community)) throw new DisplayableException("error.beneficiaryIsNotCommunityMember");
    
    if (!AdminUtil.isCreatableTokenTransaction(admin, command.getCommunityId())) throw new ForbiddenException();
    
    if (ZERO.compareTo(command.getAmount()) > -1)  throw new BadRequestException("sending negative point");

    TokenBalance tokenBalance = getTokenBalanceForAdmin(admin, command.getCommunityId(), command.getWallet());
    if (tokenBalance.getBalance().compareTo(command.getAmount()) < 0) throw new DisplayableException("error.notEnoughFunds");

    // create transaction
    TokenTransaction transaction = new TokenTransaction()
      .setCommunityId(command.getCommunityId())
      .setBeneficiaryUserId(command.getBeneficiaryUserId())
      .setFromAdmin(true)
      .setRemitterAdminId(admin.getId())
      .setWalletDivision(command.getWallet())
      .setAmount(command.getAmount())
      .setFee(ZERO)
      .setRedistributed(true);

    repository.create(transaction);

    String blockchainTransactionHash = blockchainService.transferTokensFromCommunity(community, command.getWallet(), beneficiary, command.getAmount());

    repository.lockForUpdate(transaction);
    transaction.setBlockchainTransactionHash(blockchainTransactionHash);
    repository.update(transaction);
    
    blockchainEventService.checkTransaction(blockchainTransactionHash);

    // create message
    Optional<MessageThread> threadBetweenUser = messageThreadRepository.betweenUsers(beneficiary.getId(), MessageUtil.getSystemMessageCreatorId(), community.getId());
    MessageThread thread = threadBetweenUser.orElseGet(() -> syncService.createMessageThreadWithSystem(beneficiary, community));

    String messageText = MessageUtil.getSystemMessageTokenSend2(community.getName(), beneficiary.getUsername(), command.getAmount(), tokenBalance.getToken().getTokenSymbol());
    Map<String, String> params = ImmutableMap.of(
        "type", "new_message",
        "threadId", Long.toString(thread.getId()));
    messageRepository.create(new Message()
        .setCreatedUserId(MessageUtil.getSystemMessageCreatorId())
        .setThreadId(thread.getId())
        .setText(messageText));
    pushNotificationService.send(beneficiary, messageText, params);

    return transaction;
  }

  public void markTransactionCompleted(String blockChainTransactionHash) {
    Optional<TokenTransaction> result = repository.findByBlockchainTransactionHash(blockChainTransactionHash);
    if (!result.isPresent()) {
      return;
    }

    TokenTransaction transaction = result.get();

    if (transaction.getBlockchainCompletedAt() != null) {
      log.info(format("Transaction %s already marked completed at %s", transaction.getBlockchainTransactionHash(), transaction.getBlockchainCompletedAt()));
      return;
    }

    repository.lockForUpdate(transaction);
    transaction.setBlockchainCompletedAt(now());
    repository.update(transaction);

    log.info(format("Transaction %s marked completed", transaction.getBlockchainTransactionHash()));
  }
}
