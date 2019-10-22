package commonsos.service;

import static commonsos.repository.entity.PublishStatus.PRIVATE;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.service.blockchain.BlockchainService.GAS_PRICE;
import static commonsos.service.blockchain.BlockchainService.TOKEN_TRANSFER_GAS_LIMIT;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;

import commonsos.Configuration;
import commonsos.command.PaginationCommand;
import commonsos.command.UploadPhotoCommand;
import commonsos.command.admin.CreateCommunityCommand;
import commonsos.command.admin.UpdateCommunityCommand;
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
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.blockchain.EthBalance;
import commonsos.service.image.ImageUploadService;
import commonsos.util.AdminUtil;
import commonsos.util.CommunityUtil;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityListView;
import commonsos.view.CommunityNotificationListView;
import commonsos.view.CommunityView;

@Singleton
public class CommunityService extends AbstractService {
  public static final String WALLET_PASSWORD = "test";

  @Inject private CommunityRepository repository;
  @Inject private AdminRepository adminRepository;
  @Inject private UserRepository userRepository;
  @Inject private CommunityNotificationRepository notificationRepository;
  @Inject private ImageUploadService imageService;
  @Inject private BlockchainService blockchainService;
  @Inject private Configuration config;

  public Community createCommunity(Admin admin, CreateCommunityCommand command) {
    // check role
    if (!admin.getRole().equals(NCL)) throw new ForbiddenException();
    // check adminList
    List<Admin> adminList = new ArrayList<>();
    if (command.getAdminIdList() != null) {
      command.getAdminIdList().forEach(id -> {
        Admin a = adminRepository.findStrictById(id);
        if (a.getCommunity() != null) throw new BadRequestException(String.format("Admin has alrady belongs to community [adminId=%d]", a.getId()));
        if (!a.getRole().equals(COMMUNITY_ADMIN)) throw new BadRequestException(String.format("Admin is not a community admin [adminId=%d]", a.getId()));
        adminRepository.lockForUpdate(a);
        adminList.add(a);
      });
    }
    // check fee
    BigDecimal fee = command.getTransactionFee() == null ? BigDecimal.ZERO : command.getTransactionFee().setScale(2, RoundingMode.DOWN);
    if (fee.compareTo(BigDecimal.valueOf(100L)) > 0) throw new BadRequestException(String.format("Fee is begger than 100 [fee=%f]", fee));
    if (fee.compareTo(BigDecimal.ZERO) < 0) throw new BadRequestException(String.format("Fee is less than 0 [fee=%f]", fee));
    // check system balance
    BigDecimal initialEther = new BigDecimal(config.initialEther());
    BigInteger initialWei = new BigInteger(config.initialWei());
    Credentials systemCredentials = blockchainService.systemCredentials();
    BigDecimal systemBalance = blockchainService.getBalance(systemCredentials.getAddress());
    if (systemBalance.compareTo(initialEther) < 0) throw new DisplayableException("error.notEnoughFunds");
    
    // create upload photo
    String photoUrl = command.getUploadPhotoCommand().getPhotoFile() == null ? null : imageService.create(command.getUploadPhotoCommand(), "");
    String coverPhotoUrl = command.getUploadCoverPhotoCommand().getPhotoFile() == null ? null : imageService.create(command.getUploadCoverPhotoCommand(), "");

    // create wallet
    String mainWallet = blockchainService.createWallet(WALLET_PASSWORD);
    Credentials mainCredentials = blockchainService.credentials(mainWallet, WALLET_PASSWORD);
    String feeWallet = blockchainService.createWallet(WALLET_PASSWORD);
    Credentials feeCredentials = blockchainService.credentials(feeWallet, WALLET_PASSWORD);
    
    // transfer ether to main wallet
    blockchainService.transferEther(systemCredentials, mainCredentials.getAddress(), initialWei, true);
    
    // create token
    String tokenAddress = blockchainService.createToken(mainCredentials, command.getTokenSymbol(), command.getTokenName());
    
    // create community
    Community community = new Community()
        .setName(command.getCommunityName())
        .setPublishStatus(PublishStatus.PRIVATE)
        .setDescription(command.getDescription())
        .setTokenContractAddress(tokenAddress)
        .setPhotoUrl(photoUrl)
        .setCoverPhotoUrl(coverPhotoUrl)
        .setMainWallet(mainWallet)
        .setMainWalletAddress(mainCredentials.getAddress())
        .setFeeWallet(feeWallet)
        .setFeeWalletAddress(feeCredentials.getAddress())
        .setFee(fee);
    community = repository.create(community);
    
    // approve main wallet from fee wallet
    blockchainService.transferEther(systemCredentials, feeCredentials.getAddress(), TOKEN_TRANSFER_GAS_LIMIT.multiply(BigInteger.TEN).multiply(GAS_PRICE), true);
    blockchainService.approveFromFeeWallet(community);
    
    // set adminPageUrl
    repository.lockForUpdate(community);
    String adminPageUrl = String.format("https://%s/%d", config.adminPageHost(), community.getId());
    community = repository.update(community.setAdminPageUrl(adminPageUrl));
    
    // update admin's communityId
    Community c = new Community().setId(community.getId());
    adminList.forEach(a -> adminRepository.update(a.setCommunity(c)));
    
    return community;
  }
  
  public Community updateCommunity(Admin admin, UpdateCommunityCommand command) {
    // check exists
    Community community = repository.findStrictById(command.getCommunityId());
    
    // check role
    if (!AdminUtil.isUpdatableCommunity(admin, command.getCommunityId())) throw new ForbiddenException(String.format("[targetCommunityId=%d]", command.getCommunityId()));
    
    // check fee
    BigDecimal fee = command.getTransactionFee() == null ? BigDecimal.ZERO : command.getTransactionFee().setScale(2, RoundingMode.DOWN);
    if (fee.compareTo(BigDecimal.valueOf(100L)) > 0) throw new BadRequestException(String.format("Fee is begger than 100 [fee=%f]", fee));
    if (fee.compareTo(BigDecimal.ZERO) < 0) throw new BadRequestException(String.format("Fee is less than 0 [fee=%f]", fee));
    
    // check status
    if (command.getStatus() == PRIVATE && community.getPublishStatus() == PUBLIC) throw new DisplayableException("error.invalid_update_status_puplic_to_private");

    // check admin
    List<Admin> oldAdminList = adminRepository.findByCommunityIdAndRoleId(community.getId(), COMMUNITY_ADMIN.getId(), null).getList();
    List<Admin> newAdminList = new ArrayList<>();
    if (command.getAdminIdList() != null) {
      command.getAdminIdList().forEach(id -> {
        Admin a = adminRepository.findStrictById(id);
        if (a.getCommunity() != null && !a.getCommunity().equals(community)) throw new BadRequestException(String.format("Admin has alrady belongs to community [adminId=%d]", a.getId()));
        if (!a.getRole().equals(COMMUNITY_ADMIN)) throw new BadRequestException(String.format("Admin is not a community admin [adminId=%d]", a.getId()));
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
    
    return community;
  }
  
  public CommunityListView list(String filter, PaginationCommand pagination) {
    ResultList<Community> result = StringUtils.isEmpty(filter) ? repository.listPublic(pagination) : repository.listPublic(filter, pagination);

    CommunityListView listView = new CommunityListView();
    listView.setCommunityList(result.getList().stream().map(c -> CommunityUtil.viewForApp(c, blockchainService.tokenSymbol(c.getTokenContractAddress()))).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public CommunityListView searchForAdmin(Admin admin, PaginationCommand pagination) {
    // check role
    if (!admin.getRole().equals(NCL)) throw new ForbiddenException();
    
    ResultList<Community> result = repository.list(pagination);

    CommunityListView listView = new CommunityListView();
    listView.setCommunityList(result.getList().stream().map(this::viewForAdmin).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public Community community(Long id) {
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
    
    String url = imageService.create(command, "");
    imageService.delete(community.getCoverPhotoUrl());
    
    repository.lockForUpdate(community);
    community.setCoverPhotoUrl(url);
    repository.update(community);
    return url;
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
  
  public CommunityNotificationListView notificationList(Long communityId, PaginationCommand pagination) {
    repository.findPublicStrictById(communityId);
    
    ResultList<CommunityNotification> result = notificationRepository.findByCommunityId(communityId, pagination);

    CommunityNotificationListView listView = new CommunityNotificationListView();
    listView.setNotificationList(CommunityUtil.notificationView(result.getList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
  
  public CommunityView viewForAdmin(Community community) {
    List<Admin> adminList = adminRepository.findByCommunityIdAndRoleId(community.getId(), COMMUNITY_ADMIN.getId(), null).getList();
    return viewForAdminInternal(community, adminList);
  }
  
  public CommunityView viewForAdmin(Community community, List<Long> adminIdList) {
    List<Admin> adminList = new ArrayList<>();
    if (adminIdList != null) {
      adminList = adminIdList.stream().map(id -> adminRepository.findStrictById(id)).collect(Collectors.toList());
    }
    return viewForAdminInternal(community, adminList);
  }
  
  private CommunityView viewForAdminInternal(Community community, List<Admin> adminList) {
    CommunityToken communityToken = blockchainService.getCommunityToken(community.getTokenContractAddress());
    EthBalance ethBalance = blockchainService.getEthBalance(community);
    int totalMember = userRepository.search(community.getId(), null, null).getList().size();
    return CommunityUtil.viewForAdmin(community, communityToken, ethBalance, totalMember, adminList);
  }
}
