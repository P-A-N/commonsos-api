package commonsos.service;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.Configuration;
import commonsos.command.PaginationCommand;
import commonsos.command.admin.CreateTokenTransactionFromAdminCommand;
import commonsos.command.app.CreateTokenTransactionFromUserCommand;
import commonsos.command.batch.CreateTokenTransactionForRedistributionCommand;
import commonsos.command.batch.RedistributionBatchCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.AdminRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.RedistributionRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.TokenBalance;
import commonsos.service.multithread.CreateTokenTransactionFromAdminTask;
import commonsos.service.multithread.CreateTokenTransactionFromUserTask;
import commonsos.service.multithread.TaskExecutorService;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.TokenTransactionListView;
import commonsos.view.TokenTransactionView;
import lombok.extern.slf4j.Slf4j;
import spark.utils.StringUtils;

@Singleton
@Slf4j
public class TokenTransactionService extends AbstractService {

  @Inject private TokenTransactionRepository repository;
  @Inject private UserRepository userRepository;
  @Inject private AdminRepository adminRepository;
  @Inject private AdRepository adRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private RedistributionRepository redistributionRepository;
  @Inject private BlockchainService blockchainService;
  @Inject private BlockchainEventService blockchainEventService;
  @Inject private TaskExecutorService taskExecutorService;
  @Inject private Configuration config;

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
      .setIsFeeTransaction(transaction.isFeeTransaction())
      .setIsRedistributionTransaction(transaction.isRedistributionTransaction())
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
      .setIsFeeTransaction(transaction.isFeeTransaction())
      .setIsRedistributionTransaction(transaction.isRedistributionTransaction())
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

  public TokenTransaction create(User user, CreateTokenTransactionFromUserCommand command) {
    // validation
    if (command.getCommunityId() == null) throw new BadRequestException("communityId is required");
    if (ZERO.compareTo(command.getAmount()) > -1)  throw new BadRequestException("sending negative point");
    if (user.getId().equals(command.getBeneficiaryId())) throw new BadRequestException("user is beneficiary");
    User beneficiary = userRepository.findStrictById(command.getBeneficiaryId());
    
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    if (command.getTransactionFee() == null || community.getFee().compareTo(command.getTransactionFee()) != 0) throw new DisplayableException("error.feeIncorrect");
    if (!UserUtil.isMember(user, community)) throw new DisplayableException("error.userIsNotCommunityMember");
    if (!UserUtil.isMember(beneficiary, community)) throw new DisplayableException("error.beneficiaryIsNotCommunityMember");

    Ad ad = null;
    if (command.getAdId() != null) {
      ad = adRepository.findPublicStrictById(command.getAdId());
      if (!ad.getCommunityId().equals(community.getId())) throw new BadRequestException("communityId does not match with ad");
    }
    TokenBalance tokenBalance = blockchainService.getTokenBalance(user, command.getCommunityId());
    if (tokenBalance.getBalance().compareTo(command.getAmount()) < 0) throw new DisplayableException("error.notEnoughFunds");
    
    BigDecimal feeAmount = command.getAmount().multiply(community.getFee().divide(BigDecimal.valueOf(100)));
    if (community.getFee().compareTo(ZERO) > 0) {
      if (tokenBalance.getBalance().compareTo(command.getAmount().add(feeAmount)) < 0) throw new DisplayableException("error.notEnoughFundsForFee");
    }
    
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

    // create transaction for fee
    TokenTransaction feeTransaction = null;
    if (community.getFee().compareTo(ZERO) > 0) {
      feeTransaction = new TokenTransaction()
        .setCommunityId(command.getCommunityId())
        .setRemitterUserId(user.getId())
        .setAmount(feeAmount)
        .setFee(community.getFee())
        .setFeeTransaction(true)
        .setWalletDivision(WalletType.FEE);

      repository.create(feeTransaction);
    }
    commitAndStartNewTran();
    
    CreateTokenTransactionFromUserTask task = new CreateTokenTransactionFromUserTask(user, beneficiary, community, tokenBalance.getToken(), ad, transaction, feeTransaction);
    taskExecutorService.execute(task);

    return transaction;
  }

  public TokenTransaction create(Admin admin, CreateTokenTransactionFromAdminCommand command) {
    ValidateUtil.validateCommand(command);
    
    Community community = communityRepository.findStrictById(command.getCommunityId());
    if (community.getPublishStatus() != PUBLIC) throw DisplayableException.COMMUNITY_IS_NOT_PUBLIC;
    
    User beneficiary = userRepository.findStrictById(command.getBeneficiaryUserId());
    if (!UserUtil.isMember(beneficiary, community)) throw DisplayableException.getNotMemberOfCommunity("beneficiary");
    
    if (!AdminUtil.isCreatableTokenTransaction(admin, command.getCommunityId())) throw new ForbiddenException();
    
    if (ZERO.compareTo(command.getAmount()) > -1)  throw DisplayableException.getInvalidException("amount");

    TokenBalance tokenBalance = getTokenBalanceForAdmin(admin, command.getCommunityId(), command.getWallet());
    if (tokenBalance.getBalance().compareTo(command.getAmount()) < 0) throw DisplayableException.NOT_ENOUGH_FUNDS;

    // create transaction
    TokenTransaction transaction = new TokenTransaction()
      .setCommunityId(command.getCommunityId())
      .setBeneficiaryUserId(command.getBeneficiaryUserId())
      .setFromAdmin(true)
      .setRemitterAdminId(admin.getId())
      .setWalletDivision(command.getWallet())
      .setAmount(command.getAmount());

    repository.create(transaction);
    commitAndStartNewTran();

    CreateTokenTransactionFromAdminTask task = new CreateTokenTransactionFromAdminTask(community, command.getWallet(), tokenBalance.getToken(), beneficiary, transaction);
    taskExecutorService.execute(task);
    
    return transaction;
  }

  public void create(RedistributionBatchCommand batchCommand) {
    Map<Community, List<CreateTokenTransactionForRedistributionCommand>> commandMap = batchCommand.getCommandMap();
    commandMap.forEach((com, commandList) -> {
      // get sum of fees
      List<TokenTransaction> feeTranList = repository.searchUnredistributedFeeTransaction(com.getId());
      BigDecimal feeSum = feeTranList.stream().map(TokenTransaction::getAmount).reduce(ZERO, BigDecimal::add);
      
      // get sum of rate
      List<Redistribution> redList = redistributionRepository.searchByCommunityId(com.getId(), null).getList();
      BigDecimal rateSum = redList.stream().map(Redistribution::getRate).reduce(ZERO, BigDecimal::add);
      
      // verify communities token balance
      BigDecimal tokenBalance = blockchainService.getTokenBalance(com, WalletType.FEE).getBalance();
      if (tokenBalance.compareTo(feeSum.multiply(rateSum.divide(BigDecimal.valueOf(100L)))) < 0) {
        log.error("not enough token for redistribution. [communityId=%d, balanceOfFeeWallet=%f, rate=%f%%]", com.getId(), feeSum, rateSum);
        return;
      }
      
      // mark transaction as redistributed
      feeTranList.forEach(feeTran -> {
        repository.lockForUpdate(feeTran);
        feeTran.setRedistributed(true);
        repository.update(feeTran);
      });
      
      // create token transaction of redistribution
      List<TokenTransaction> tranList = new ArrayList<>();
      commandList.forEach(tranCommand -> {
        String minimumNumberOfDecimalsForRedistribution = config.minimumNumberOfDecimalsForRedistribution();
        int scale = Integer.parseInt(minimumNumberOfDecimalsForRedistribution);
        
        BigDecimal amount = feeSum.multiply(tranCommand.getRate().divide(BigDecimal.valueOf(100L))).setScale(scale, BigDecimal.ROUND_DOWN);
        if (amount.compareTo(ZERO) <= 0) return;
        
        TokenTransaction transaction = new TokenTransaction()
          .setCommunityId(com.getId())
          .setBeneficiaryUserId(tranCommand.getUser().getId())
          .setFromAdmin(true)
          .setRedistributionTransaction(true)
          .setWalletDivision(WalletType.FEE)
          .setAmount(amount);
        repository.create(transaction);
        
        tranList.add(transaction);
      });
      
      // commit create of transaction
      commitAndStartNewTran();
      
      // create transaction to blockchain
      Map<Long, User> userMap = commandList.stream().map(CreateTokenTransactionForRedistributionCommand::getUser)
          .collect(Collectors.toMap(User::getId, Function.identity()));
      tranList.forEach(tran -> {
        String blockchainTransactionHash = blockchainService.transferTokensFromCommunity(com, WalletType.FEE, userMap.get(tran.getBeneficiaryUserId()), tran.getAmount());

        repository.lockForUpdate(tran);
        tran.setBlockchainTransactionHash(blockchainTransactionHash);
        repository.update(tran);
        
        blockchainEventService.checkTransaction(blockchainTransactionHash);
        
        // commit update of transaction
        commitAndStartNewTran();
      });
    });
  }

  public void markTransactionCompleted(String blockChainTransactionHash) {
    Optional<TokenTransaction> result = repository.findByBlockchainTransactionHash(blockChainTransactionHash);
    if (!result.isPresent()) {
      return;
    }

    TokenTransaction transaction = result.get();

    if (transaction.getBlockchainCompletedAt() != null) {
      log.info(format("TokenTransaction %s already marked completed at %s", transaction.getBlockchainTransactionHash(), transaction.getBlockchainCompletedAt()));
      return;
    }

    repository.lockForUpdate(transaction);
    transaction.setBlockchainCompletedAt(now());
    repository.update(transaction);

    log.info(format("TokenTransaction %s marked completed", transaction.getBlockchainTransactionHash()));
  }
}
