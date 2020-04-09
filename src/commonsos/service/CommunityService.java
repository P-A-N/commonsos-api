package commonsos.service;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;

import commonsos.Configuration;
import commonsos.command.PaginationCommand;
import commonsos.command.UploadPhotoCommand;
import commonsos.command.admin.CreateCommunityCommand;
import commonsos.command.admin.UpdateCommunityCommand;
import commonsos.command.admin.UpdateCommunityTokenNameCommand;
import commonsos.command.admin.UpdateCommunityTotalSupplyCommand;
import commonsos.command.app.UpdateCommunityNotificationCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdminRepository;
import commonsos.repository.CommunityNotificationRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;
import commonsos.repository.entity.PublishStatus;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.repository.entity.WalletType;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.blockchain.EthBalance;
import commonsos.service.image.ImageUploadService;
import commonsos.service.multithread.CreateCommunityTask;
import commonsos.service.multithread.CreateWordpressAccountTask;
import commonsos.service.multithread.TaskExecutorService;
import commonsos.service.multithread.UpdateCommunityTokenNameTask;
import commonsos.service.multithread.UpdateCommunityTotalSupplyTask;
import commonsos.util.AdminUtil;
import commonsos.util.CommunityUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.CommunityListView;
import commonsos.view.CommunityNotificationListView;
import commonsos.view.CommunityView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class CommunityService extends AbstractService {

  @Inject private CommunityRepository repository;
  @Inject private AdminRepository adminRepository;
  @Inject private UserRepository userRepository;
  @Inject private CommunityNotificationRepository notificationRepository;
  @Inject private ImageUploadService imageService;
  @Inject private BlockchainService blockchainService;
  @Inject private TaskExecutorService taskExecutorService;
  @Inject private DeleteService deleteService;
  @Inject private Configuration config;

  public Community createCommunity(Admin admin, CreateCommunityCommand command) {
    // check role
    if (!admin.getRole().equals(NCL)) throw new ForbiddenException();

    // check adminList
    List<Admin> adminList = new ArrayList<>();
    if (command.getAdminIdList() != null) {
      command.getAdminIdList().forEach(id -> {
        Admin a = adminRepository.findStrictById(id);
        if (a.getCommunity() != null) {
          log.info(String.format("Admin has alrady belongs to community [adminId=%d]", a.getId()));
          throw DisplayableException.ADMIN_BELONGS_TO_OTHER_COMMUNITY;
        }
        if (!a.getRole().equals(COMMUNITY_ADMIN)) {
          log.info(String.format("Admin is not a community admin [adminId=%d]", a.getId()));
          throw DisplayableException.ADMIN_IS_NOT_COMMUNITY_ADMIN;
        }
        adminRepository.lockForUpdate(a);
        adminList.add(a);
      });
    }
    // check fee
    BigDecimal fee = command.getTransactionFee() == null ? BigDecimal.ZERO : command.getTransactionFee().setScale(2, RoundingMode.DOWN);
    ValidateUtil.validateFee(fee);

    // validate wordpress id, emailAddress
    ValidateUtil.validateWordpressAccountId(command.getWordpressAccountId());
    ValidateUtil.validateWordpressAccountEmailAddress(command.getWordpressAccountEmailAddress());
    if (repository.isWordpressAccountIdTaken(command.getWordpressAccountId())) throw DisplayableException.getTakenException("wordpressAccountId");
    if (repository.isWordpressAccountEmailAddressTaken(command.getWordpressAccountEmailAddress())) throw DisplayableException.getTakenException("wordpressAccountEmailAddress");

    // check system balance
    BigDecimal initialEther = new BigDecimal(config.initialEther());
    Credentials systemCredentials = blockchainService.systemCredentials();
    BigDecimal systemBalance = blockchainService.getEthBalance(systemCredentials.getAddress());
    if (systemBalance.compareTo(initialEther) < 0) throw DisplayableException.NOT_ENOUGH_ETHER_TO_INITIATE_COMMUNITY;

    // create upload photo
    String photoUrl = command.getUploadPhotoCommand().getPhotoFile() == null ? null : imageService.create(command.getUploadPhotoCommand(), "");
    String coverPhotoUrl = command.getUploadCoverPhotoCommand().getPhotoFile() == null ? null : imageService.create(command.getUploadCoverPhotoCommand(), "");

    // create wallet
    String mainWallet = blockchainService.createWallet(config.communityWalletPassword());
    Credentials mainCredentials = blockchainService.credentials(mainWallet, config.communityWalletPassword());
    String feeWallet = blockchainService.createWallet(config.communityWalletPassword());
    Credentials feeCredentials = blockchainService.credentials(feeWallet, config.communityWalletPassword());
    
    // create community
    Community community = new Community()
        .setName(command.getCommunityName())
        .setPublishStatus(PublishStatus.PRIVATE)
        .setDescription(command.getDescription())
        .setPhotoUrl(photoUrl)
        .setCoverPhotoUrl(coverPhotoUrl)
        .setMainWallet(mainWallet)
        .setMainWalletAddress(mainCredentials.getAddress())
        .setFeeWallet(feeWallet)
        .setFeeWalletAddress(feeCredentials.getAddress())
        .setFee(fee)
        .setWordpressAccountId(command.getWordpressAccountId())
        .setWordpressAccountEmailAddress(command.getWordpressAccountEmailAddress());
    community = repository.create(community);
    
    // set adminPageUrl
    repository.lockForUpdate(community);
    String adminPageUrl = String.format("https://%s/%d", config.adminPageHost(), community.getId());
    community = repository.update(community.setAdminPageUrl(adminPageUrl));
    
    // update admin's communityId
    Community c = new Community().setId(community.getId());
    adminList.forEach(a -> adminRepository.update(a.setCommunity(c)));
    
    commitAndStartNewTran();
    
    CreateCommunityTask task = new CreateCommunityTask(community.getId(), command);
    taskExecutorService.execute(task);

    CreateWordpressAccountTask createWordpressAccountTask = new CreateWordpressAccountTask(community.getId(), command);
    taskExecutorService.execute(createWordpressAccountTask);
    
    return community;
  }
  
  public Community updateCommunity(Admin admin, UpdateCommunityCommand command) {
    // check exists
    Community community = repository.findStrictById(command.getCommunityId());
    
    // check role
    if (!AdminUtil.isUpdatableCommunity(admin, command.getCommunityId())) throw new ForbiddenException(String.format("[targetCommunityId=%d]", command.getCommunityId()));
    
    // check fee
    BigDecimal fee = command.getTransactionFee() == null ? BigDecimal.ZERO : command.getTransactionFee().setScale(2, RoundingMode.DOWN);
    ValidateUtil.validateFee(fee);
    
    // check status
    if (command.getStatus() == PRIVATE && community.getPublishStatus() == PUBLIC) throw DisplayableException.INVALID_UPDATE_STATUS_PUPLIC_TO_PRIVATE;

    // check admin
    List<Admin> oldAdminList = adminRepository.searchByCommunityIdAndRoleId(community.getId(), COMMUNITY_ADMIN.getId(), null).getList();
    List<Admin> newAdminList = new ArrayList<>();
    if (command.getAdminIdList() != null) {
      command.getAdminIdList().forEach(id -> {
        Admin a = adminRepository.findStrictById(id);
        if (a.getCommunity() != null && !a.getCommunity().equals(community)) {
          log.info(String.format("Admin has alrady belongs to community [adminId=%d]", a.getId()));
          throw DisplayableException.ADMIN_BELONGS_TO_OTHER_COMMUNITY;
        }
        if (!a.getRole().equals(COMMUNITY_ADMIN)) {
          log.info(String.format("Admin is not a community admin [adminId=%d]", a.getId()));
          throw DisplayableException.ADMIN_IS_NOT_COMMUNITY_ADMIN;
        }
        adminRepository.lockForUpdate(a);
        newAdminList.add(a);
      });
    }
    
    List<Admin> deleteAdminList = new ArrayList<>();
    oldAdminList.forEach(a -> {
      if (!newAdminList.contains(a)) {
        adminRepository.lockForUpdate(a);
        deleteAdminList.add(a);
      }
    });

    repository.lockForUpdate(community);
    community
      .setName(command.getCommunityName())
      .setFee(command.getTransactionFee())
      .setDescription(command.getDescription())
      .setPublishStatus(command.getStatus());
    repository.update(community);

    // update admin's communityId
    newAdminList.forEach(a -> adminRepository.update(a.setCommunity(community)));
    deleteAdminList.forEach(a -> adminRepository.update(a.setCommunity(null)));

    commitAndStartNewTran();

    return community;
  }
  
  public CommunityListView search(String filter, PaginationCommand pagination) {
    ResultList<Community> result = StringUtils.isEmpty(filter) ? repository.searchPublic(pagination) : repository.searchPublic(filter, pagination);

    CommunityListView listView = new CommunityListView();
    listView.setCommunityList(result.getList().stream().map(c -> CommunityUtil.viewForApp(c, blockchainService.tokenSymbol(c.getTokenContractAddress()))).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public CommunityListView searchForAdmin(Admin admin, PaginationCommand pagination) {
    // check role
    if (!admin.getRole().equals(NCL)) throw new ForbiddenException();
    
    ResultList<Community> result = repository.searchAll(pagination);

    CommunityListView listView = new CommunityListView();
    listView.setCommunityList(result.getList().stream().map(this::viewForAdmin).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public Community getCommunity(Long id) {
    return repository.findStrictById(id);
  }

  public Community getPublicCommunity(Long id) {
    return repository.findPublicStrictById(id);
  }

  public Community findCommunityForAdmin(Admin admin, Long id) {
    if (!AdminUtil.isSeeableCommunity(admin, id)) throw new ForbiddenException();
    return repository.findStrictById(id);
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findPublicStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");

    return updatePhoto(command, community);
  }

  public Community updatePhoto(Admin admin, UploadPhotoCommand command, Long communityId) {
    ValidateUtil.validateCommand(command);
    Community community = repository.findStrictById(communityId);
    if (!AdminUtil.isUpdatableCommunity(admin, community.getId())) throw new ForbiddenException(String.format("[targetCommunityId=%d]", community.getId()));
    
    updatePhoto(command, community);
    return community;
  }
  
  private String updatePhoto(UploadPhotoCommand command, Community community) {
    String url = imageService.create(command, "");
    imageService.delete(community.getPhotoUrl());
    
    repository.lockForUpdate(community);
    community.setPhotoUrl(url);
    repository.update(community);
    return url;
  }

  public String updateCoverPhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findPublicStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");

    return updateCoverPhoto(command, community);
  }

  public Community updateCoverPhoto(Admin admin, UploadPhotoCommand command, Long communityId) {
    ValidateUtil.validateCommand(command);
    Community community = repository.findStrictById(communityId);
    if (!AdminUtil.isUpdatableCommunity(admin, community.getId())) throw new ForbiddenException(String.format("[targetCommunityId=%d]", community.getId()));
    
    updateCoverPhoto(command, community);
    return community;
  }
  
  private String updateCoverPhoto(UploadPhotoCommand command, Community community) {
    String url = imageService.create(command, "");
    imageService.delete(community.getCoverPhotoUrl());
    
    repository.lockForUpdate(community);
    community.setCoverPhotoUrl(url);
    repository.update(community);
    return url;
  }

  public Community updateTokenName(Admin admin, UpdateCommunityTokenNameCommand command) {
    ValidateUtil.validateCommand(command);
    Community community = repository.findStrictById(command.getCommunityId());
    if (!AdminUtil.isUpdatableCommunity(admin, community.getId())) throw new ForbiddenException(String.format("[targetCommunityId=%d]", community.getId()));
    
    UpdateCommunityTokenNameTask task = new UpdateCommunityTokenNameTask(community, command.getTokenName());
    taskExecutorService.execute(task);
    
    return community;
  }

  public Community updateTotalSupply(Admin admin, UpdateCommunityTotalSupplyCommand command) {
    ValidateUtil.validateCommand(command);
    Community community = repository.findStrictById(command.getCommunityId());
    if (!AdminUtil.isUpdatableCommunity(admin, community.getId())) throw new ForbiddenException(String.format("[targetCommunityId=%d]", community.getId()));
    
    // check executable
    BigDecimal currentTotalSupply = blockchainService.totalSupply(community.getTokenContractAddress());
    BigDecimal newTotalSupply = command.getTotalSupply();
    boolean isBurn = newTotalSupply.compareTo(currentTotalSupply) < 0;
    BigDecimal absAmount = newTotalSupply.subtract(currentTotalSupply).abs();
    
    if (absAmount.compareTo(BigDecimal.ZERO) == 0) {
      log.info("there is no difference between current totalSupply and new TotalSupply. stopping the proccess.");
      return community;
    }
    
    if (isBurn) {
      BigDecimal balanceOfMainWallet = blockchainService.getTokenBalance(community, WalletType.MAIN).getBalance();
      if (balanceOfMainWallet.compareTo(absAmount) < 0) throw DisplayableException.NOT_ENOUGH_COIN_TO_BURN_FROM_COMMUNITY;
    }
    
    UpdateCommunityTotalSupplyTask task = new UpdateCommunityTotalSupplyTask(community, absAmount, isBurn);
    taskExecutorService.execute(task);
    
    return community;
  }
  
  public void updateNotificationUpdateAt(UpdateCommunityNotificationCommand command) {
    Optional<CommunityNotification> optionalNotification = notificationRepository.findByWordPressId(command.getWordpressId());
    if (optionalNotification.isPresent()) {
      CommunityNotification notification = optionalNotification.get();
      
      if (!notification.getCommunityId().equals(command.getCommunityId())) throw new BadRequestException(
          String.format("it is not a notification of community. wordpressId=%s communityId=%d", command.getWordpressId(), command.getCommunityId()));
      
      notificationRepository.lockForUpdate(notification);
      notification.setUpdatedNotificationAt(command.getUpdatedAtInstant());
      notificationRepository.update(notification);
    } else {
      CommunityNotification notification = new CommunityNotification()
          .setCommunityId(command.getCommunityId())
          .setWordpressId(command.getWordpressId())
          .setUpdatedNotificationAt(command.getUpdatedAtInstant());

      notificationRepository.create(notification);
    }
  }
  
  public void deleteCommunity(Admin admin, Long communityId) {
    Community community = repository.findStrictById(communityId);
    if (!AdminUtil.isDeletableCommunity(admin)) throw new ForbiddenException();
    deleteService.deleteCommunity(community);
  }
  
  public CommunityNotificationListView notificationList(Long communityId, PaginationCommand pagination) {
    repository.findPublicStrictById(communityId);
    
    ResultList<CommunityNotification> result = notificationRepository.searchByCommunityId(communityId, pagination);

    CommunityNotificationListView listView = new CommunityNotificationListView();
    listView.setNotificationList(CommunityUtil.notificationView(result.getList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
  
  public CommunityView viewForAdmin(Community community) {
    List<Admin> adminList = adminRepository.searchByCommunityIdAndRoleId(community.getId(), COMMUNITY_ADMIN.getId(), null).getList();
    return viewForAdminInternal(community, adminList);
  }

  private CommunityView viewForAdminInternal(Community community, List<Admin> adminList) {
    if (StringUtils.isEmpty(community.getTokenContractAddress())) {
      return CommunityUtil.viewForAdmin(community, adminList);
    } else {
      CommunityToken communityToken = blockchainService.getCommunityToken(community.getTokenContractAddress());
      EthBalance ethBalance = blockchainService.getEthBalance(community);
      int totalMember = userRepository.search(community.getId(), null, null).getList().size();

      return CommunityUtil.viewForAdmin(community, communityToken, ethBalance, totalMember, adminList);
    }
  }
}
