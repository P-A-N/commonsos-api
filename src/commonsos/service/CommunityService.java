package commonsos.service;

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
import commonsos.command.admin.CreateCommunityCommand;
import commonsos.command.app.CommunityNotificationCommand;
import commonsos.command.app.UploadPhotoCommand;
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
import commonsos.repository.entity.CommunityStatus;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.image.ImageUploadService;
import commonsos.util.CommunityUtil;
import commonsos.util.PaginationUtil;
import commonsos.view.CommunityListView;
import commonsos.view.CommunityNotificationListView;
import commonsos.view.CommunityView;

@Singleton
public class CommunityService {
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
    if (Role.of(admin.getRole().getId()) != Role.NCL) throw new ForbiddenException();
    // check adminList
    List<Admin> adminList = new ArrayList<>();
    if (command.getAdminIdList() != null) {
      command.getAdminIdList().forEach(id -> {
        Admin a = adminRepository.findStrictById(id);
        if (a.getCommunity() != null) throw new BadRequestException(String.format("Admin has alrady belongs to community [adminId=%d]", a.getId()));
        if (!a.getRole().getId().equals(Role.COMMUNITY_ADMIN.getId())) throw new BadRequestException(String.format("Admin is not a community admin [adminId=%d]", a.getId()));
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
    blockchainService.transferEther(systemCredentials, mainCredentials.getAddress(), initialWei);
    // create token
    String tokenAddress = blockchainService.createToken(mainCredentials, command.getTokenSymbol(), command.getTokenName());
    
    // create community
    Community community = new Community()
        .setName(command.getCommunityName())
        .setStatus(CommunityStatus.PRIVATE)
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
    
    // set adminPageUrl
    repository.lockForUpdate(community);
    String adminPageUrl = String.format("https://%s/%d", config.adminPageHost(), community.getId());
    community = repository.update(community.setAdminPageUrl(adminPageUrl));
    
    // update admin's communityId
    Community c = new Community().setId(community.getId());
    adminList.forEach(a -> adminRepository.update(a.setCommunity(c)));
    
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
    if (Role.of(admin.getRole().getId()) != Role.NCL) throw new ForbiddenException();
    
    ResultList<Community> result = repository.list(pagination);

    CommunityListView listView = new CommunityListView();
    listView.setCommunityList(result.getList().stream().map(this::viewForAdmin).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public Community community(Long id) {
    return repository.findPublicStrictById(id);
  }

  public Community findCommunityForAdmin(Long id) {
    return repository.findStrictById(id);
  }

  public boolean isAdmin(Long userId, Long communityId) {
    return repository.isAdmin(userId, communityId);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long communityId) {
    Community community = repository.findPublicStrictById(communityId);
    if (!repository.isAdmin(user.getId(), communityId)) throw new ForbiddenException("User is not admin");
    
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
  
  public void updateNotificationUpdateAt(CommunityNotificationCommand command) {
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
    CommunityToken communityToken = blockchainService.getCommunityToken(community.getTokenContractAddress());
    List<Admin> adminList = adminRepository.findByCommunityId(community.getId(), null).getList();
    int totalMember = userRepository.search(community.getId(), null, null).getList().size();
    return CommunityUtil.viewForAdmin(community, communityToken, totalMember, adminList);
  }
  
  public CommunityView viewForAdmin(Community community, List<Long> adminIdList) {
    CommunityToken communityToken = blockchainService.getCommunityToken(community.getTokenContractAddress());
    List<Admin> adminList = new ArrayList<>();
    if (adminIdList != null) {
      adminList = adminIdList.stream().map(id -> adminRepository.findStrictById(id)).collect(Collectors.toList());
    }
    int totalMember = userRepository.search(community.getId(), null, null).getList().size();
    return CommunityUtil.viewForAdmin(community, communityToken, totalMember, adminList);
  }
}
