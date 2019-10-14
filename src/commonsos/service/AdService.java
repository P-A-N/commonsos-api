package commonsos.service;

import static java.util.stream.Collectors.toList;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.command.PaginationCommand;
import commonsos.command.app.AdCreateCommand;
import commonsos.command.app.AdUpdateCommand;
import commonsos.command.app.UploadPhotoCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;
import commonsos.util.AdUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.UserUtil;
import commonsos.view.AdListView;
import commonsos.view.AdView;

@Singleton
public class AdService extends AbstractService {
  @Inject private AdRepository adRepository;
  @Inject private UserRepository userRepository;
  @Inject private TokenTransactionRepository transactionRepository;
  @Inject private DeleteService deleteService;
  @Inject private ImageUploadService imageService;

  public AdView create(User user, AdCreateCommand command) {
    if (!UserUtil.isMember(user, command.getCommunityId())) throw new ForbiddenException("only a member of community is allow to create ads");

    Ad ad = new Ad()
      .setCreatedUserId(user.getId())
      .setType(command.getType())
      .setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setPoints(command.getPoints())
      .setCommunityId(command.getCommunityId());

    return view(adRepository.create(ad), user);
  }

  public AdListView listFor(User user, Long communityId, String filter, PaginationCommand pagination) {
    ResultList<Ad> result = filter != null ?
      adRepository.ads(communityId, filter, pagination) :
      adRepository.ads(communityId, pagination);
    
    AdListView listView = new AdListView();
    listView.setAdList(result.getList().stream().map(ad -> view(ad, user)).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public AdListView myAdsView(User user, PaginationCommand pagination) {
    ResultList<Ad> result = adRepository.myAds(user.getId(), pagination);

    AdListView listView = new AdListView();
    listView.setAdList(result.getList().stream().map(ad -> view(ad, user)).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public AdView view(Ad ad, User user) {
    User createdUser = userRepository.findStrictById(ad.getCreatedUserId());
    
    return AdUtil.view(ad, createdUser, user);
  }

  public AdView view(User user, Long adId) {
    return view(ad(adId), user);
  }

  public Ad ad(Long id) {
    return adRepository.findStrict(id);
  }

  public Ad updateAd(User operator, AdUpdateCommand command) {
    Ad ad = ad(command.getId());
    if (!ad.getCreatedUserId().equals(operator.getId())) throw new ForbiddenException();
    if (transactionRepository.hasPaid(ad)) throw new BadRequestException();
    
    adRepository.lockForUpdate(ad);
    ad.setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setPoints(command.getPoints())
      .setLocation(command.getLocation())
      .setType(command.getType());
    return adRepository.update(ad);
  }

  public String updatePhoto(User user, UploadPhotoCommand command, Long adId) {
    Ad ad = adRepository.findStrict(adId);
    if (!ad.getCreatedUserId().equals(user.getId())) throw new ForbiddenException();

    String url = imageService.create(command, "");
    imageService.delete(ad.getPhotoUrl());

    adRepository.lockForUpdate(ad);
    ad.setPhotoUrl(url);
    adRepository.update(ad);
    return url;
  }

  public void deleteAdLogicallyByUser(User user, Long adId) {
    deleteService.deleteAdByUser(user, ad(adId));
  }

  public void deleteAdLogicallyByAdmin(User user, Long adId) {
    deleteService.deleteAdByAdmin(user, ad(adId));
  }
}
