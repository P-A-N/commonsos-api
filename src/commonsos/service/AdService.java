package commonsos.service;

import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static java.util.stream.Collectors.toList;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import commonsos.command.PaginationCommand;
import commonsos.command.UploadPhotoCommand;
import commonsos.command.admin.UpdateAdByAdminCommand;
import commonsos.command.admin.UpdateAdPublishStatusByAdminCommand;
import commonsos.command.app.CreateAdCommand;
import commonsos.command.app.UpdateAdCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;
import commonsos.util.AdUtil;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.view.AdListView;
import commonsos.view.AdView;

@Singleton
public class AdService extends AbstractService {
  @Inject private AdRepository adRepository;
  @Inject private UserRepository userRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private TokenTransactionRepository transactionRepository;
  @Inject private DeleteService deleteService;
  @Inject private ImageUploadService imageService;

  public AdView createAd(User user, CreateAdCommand command) {
    validateCommande(command);
    if (!UserUtil.isMember(user, command.getCommunityId())) throw new ForbiddenException("only a member of community is allow to create ads");

    Ad ad = new Ad()
      .setCreatedUserId(user.getId())
      .setType(command.getType())
      .setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setPoints(command.getPoints())
      .setCommunityId(command.getCommunityId())
      .setPublishStatus(PUBLIC);

    return viewForApp(adRepository.create(ad), user);
  }

  public AdListView searchAds(User user, Long communityId, String filter, PaginationCommand pagination) {
    ResultList<Ad> result = filter != null ?
      adRepository.searchPublicByCommunityId(communityId, filter, pagination) :
      adRepository.searchPublicByCommunityId(communityId, pagination);
    
    AdListView listView = new AdListView();
    listView.setAdList(result.getList().stream().map(ad -> viewForApp(ad, user)).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public AdListView searchMyAds(User user, PaginationCommand pagination) {
    ResultList<Ad> result = adRepository.searchByCreatorId(user.getId(), pagination);

    AdListView listView = new AdListView();
    listView.setAdList(result.getList().stream().map(ad -> viewForApp(ad, user)).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public AdListView searchAdsByAdmin(Admin admin, Long communityId, PaginationCommand pagination) {
    if (!AdminUtil.isSeeableAd(admin, communityId)) throw new ForbiddenException();

    ResultList<Ad> result = adRepository.searchByCommunityId(communityId, pagination);

    AdListView listView = new AdListView();
    listView.setAdList(result.getList().stream().map(this::viewForAdmin).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));

    return listView;
  }

  public AdView viewForApp(Ad ad, User user) {
    User createdUser = userRepository.findStrictById(ad.getCreatedUserId());
    
    return AdUtil.viewForApp(ad, createdUser, user);
  }

  public AdView viewForAdmin(Ad ad) {
    User createdUser = userRepository.findStrictById(ad.getCreatedUserId());
    Community community = communityRepository.findStrictById(ad.getCommunityId());
    
    return AdUtil.viewForAdmin(ad, createdUser, community);
  }

  public Ad getAd(Long id) {
    return adRepository.findStrictById(id);
  }

  public Ad getPublicAd(Long id) {
    return adRepository.findPublicStrictById(id);
  }

  public Ad updateAd(User operator, UpdateAdCommand command) {
    validateCommand(command);
    
    Ad ad = getAd(command.getId());
    if (!ad.getCreatedUserId().equals(operator.getId())) throw new ForbiddenException();
    if (transactionRepository.hasPaid(ad)) throw new DisplayableException("error.hasPaid");
    
    adRepository.lockForUpdate(ad);
    ad.setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setPoints(command.getPoints())
      .setLocation(command.getLocation())
      .setType(command.getType());
    return adRepository.update(ad);
  }

  public Ad updateAdByAdmin(Admin admin, UpdateAdByAdminCommand command) {
    validateCommand(command);
    Ad targetAd = adRepository.findStrictById(command.getId());
    
    if (!AdminUtil.isUpdatableAd(admin, targetAd)) throw new ForbiddenException();
    if (transactionRepository.hasPaid(targetAd)) throw new DisplayableException("error.hasPaid");
    
    adRepository.lockForUpdate(targetAd);
    targetAd.setTitle(command.getTitle())
      .setStatus(command.getStatus())
      .setDescription(command.getDescription())
      .setPoints(command.getPoints())
      .setLocation(command.getLocation())
      .setType(command.getType());
    return adRepository.update(targetAd);
  }

  public Ad updateAdPublishStatusByAdmin(Admin admin, UpdateAdPublishStatusByAdminCommand command) {
    validateCommand(command);
    Ad targetAd = adRepository.findStrictById(command.getId());
    
    if (!AdminUtil.isUpdatableAd(admin, targetAd)) throw new ForbiddenException();
    if (transactionRepository.hasPaid(targetAd)) throw new DisplayableException("error.hasPaid");
    
    adRepository.lockForUpdate(targetAd);
    targetAd.setPublishStatus(command.getPublishStatus());
    return adRepository.update(targetAd);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long adId) {
    Ad ad = adRepository.findStrictById(adId);
    if (!ad.getCreatedUserId().equals(user.getId())) throw new ForbiddenException();

    String url = imageService.create(command, "");
    imageService.delete(ad.getPhotoUrl());

    adRepository.lockForUpdate(ad);
    ad.setPhotoUrl(url);
    adRepository.update(ad);
    return url;
  }

  public void deleteAdLogicallyByUser(User user, Long adId) {
    deleteService.deleteAdByUser(user, getAd(adId));
  }

  public void deleteAdLogicallyByAdmin(User user, Long adId) {
    deleteService.deleteAdByAdmin(user, getAd(adId));
  }

  private void validateCommande(CreateAdCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw new BadRequestException("title is empty");
    if (command.getPoints() == null) throw new BadRequestException("points is null");
    if (command.getType() == null) throw new BadRequestException("type is null");
  }

  private void validateCommand(UpdateAdCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw new BadRequestException("title is empty");
    if (command.getPoints() == null) throw new BadRequestException("points is null");
    if (command.getType() == null) throw new BadRequestException("type is null");
  }

  private void validateCommand(UpdateAdByAdminCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw new BadRequestException("title is empty");
    if (command.getPoints() == null) throw new BadRequestException("points is null");
    if (command.getType() == null) throw new BadRequestException("type is null");
  }

  private void validateCommand(UpdateAdPublishStatusByAdminCommand command) {
    if (command.getPublishStatus() == null) throw new BadRequestException("publishStatus is null");
  }
}
