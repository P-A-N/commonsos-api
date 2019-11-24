package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import commonsos.command.PaginationCommand;
import commonsos.command.UpdateEmailAddressTemporaryCommand;
import commonsos.command.UploadPhotoCommand;
import commonsos.command.admin.AdminLoginCommand;
import commonsos.command.admin.CreateAdminTemporaryCommand;
import commonsos.command.admin.UpdateAdminCommand;
import commonsos.command.admin.UpdateAdminPasswordCommand;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdminRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.TemporaryAdmin;
import commonsos.repository.entity.TemporaryAdminEmailAddress;
import commonsos.service.crypto.AccessIdService;
import commonsos.service.crypto.CryptoService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageUploadService;
import commonsos.session.AdminSession;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.AdminListView;

@Singleton
public class AdminService extends AbstractService {

  @Inject private AdminRepository adminRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private DeleteService deleteService;
  @Inject private CryptoService cryptoService;
  @Inject private AccessIdService accessIdService;
  @Inject private ImageUploadService imageService;
  @Inject private EmailService emailService;
  
  public Admin getAdmin(Long id) {
    return adminRepository.findStrictById(id);
  }

  public Admin getAdmin(Admin admin, Long id) {
    Admin target = adminRepository.findStrictById(id);
    if (!AdminUtil.isSeeableAdmin(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", id));
    
    return target;
  }

  public AdminListView searchAdmin(Admin admin, Long communityId, Long roleId, PaginationCommand pagination) {
    // validate role
    if (!AdminUtil.isSeeableAdmin(admin, communityId, roleId)) throw new ForbiddenException();
    
    ResultList<Admin> result = adminRepository.searchByCommunityIdAndRoleId(communityId, roleId, pagination);

    AdminListView listView = new AdminListView();
    listView.setAdminList(result.getList().stream().map(AdminUtil::view).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public Admin updateAdmin(Admin admin, UpdateAdminCommand command) {
    Admin target = adminRepository.findStrictById(command.getAdminId());
    if (!AdminUtil.isUpdatableAdmin(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", command.getAdminId()));
    validate(command);

    adminRepository.lockForUpdate(target);
    target
      .setAdminname(command.getAdminname())
      .setTelNo(command.getTelNo())
      .setDepartment(command.getDepartment());
    adminRepository.update(target);

    return target;
  }

  public Admin updateAdminPhoto(Admin admin, Long targetAdminId, UploadPhotoCommand command) {
    Admin target = adminRepository.findStrictById(targetAdminId);
    if (command.getPhotoFile() == null) throw new BadRequestException();
    if (!AdminUtil.isUpdatableAdmin(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", targetAdminId));

    String newPhotoUrl = command.getPhotoFile() == null ? null : imageService.create(command, "");
    String prePhotoUrl = target.getPhotoUrl();
    
    adminRepository.lockForUpdate(target);
    target
      .setPhotoUrl(newPhotoUrl);
    adminRepository.update(target);

    imageService.delete(prePhotoUrl);

    return target;
  }

  public void updateAdminEmailAddressTemporary(Admin admin, UpdateEmailAddressTemporaryCommand command) {
    Admin target = adminRepository.findStrictById(command.getId());
    if (!AdminUtil.isUpdatableAdmin(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", command.getId()));
    ValidateUtil.validateEmailAddress(command.getNewEmailAddress());

    String accessId = accessIdService.generateAccessId(id -> {
      String accessIdHash = cryptoService.hash(id);
      return !adminRepository.findTemporaryAdminEmailAddress(accessIdHash).isPresent();
    });

    TemporaryAdminEmailAddress tmpEmailAddress = new TemporaryAdminEmailAddress()
        .setAccessIdHash(cryptoService.hash(accessId))
        .setExpirationTime(Instant.now().plus(1, ChronoUnit.DAYS))
        .setInvalid(false)
        .setAdminId(target.getId())
        .setEmailAddress(command.getNewEmailAddress());
    
    adminRepository.createTemporaryEmail(tmpEmailAddress);

    emailService.sendUpdateAdminEmailTemporary(command.getNewEmailAddress(), target.getAdminname(), target.getId(), accessId);
  }

  public Admin updateAdminEmailAddressComplete(Long adminId, String accessId) {
    TemporaryAdminEmailAddress tmpEmailAddress = adminRepository.findStrictTemporaryAdminEmailAddress(cryptoService.hash(accessId));
    Admin admin = adminRepository.findStrictById(adminId);
    
    adminRepository.lockForUpdate(tmpEmailAddress);
    adminRepository.lockForUpdate(admin);
    tmpEmailAddress.setInvalid(true);
    admin.setEmailAddress(tmpEmailAddress.getEmailAddress());
    adminRepository.updateTemporaryEmail(tmpEmailAddress);
    adminRepository.update(admin);
    
    return admin;
  }

  public Admin updateAdminPassword(Admin admin, UpdateAdminPasswordCommand command) {
    Admin target = adminRepository.findStrictById(command.getAdminId());
    if (!AdminUtil.isUpdatableAdmin(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", command.getAdminId()));
    ValidateUtil.validatePassword(command.getNewPassword());

    adminRepository.lockForUpdate(target);
    target
      .setPasswordHash(cryptoService.encryptoPassword(command.getNewPassword()));
    adminRepository.update(target);

    return target;
  }

  public Admin updateLoggedinAt(Admin admin) {
    adminRepository.lockForUpdate(admin);
    
    admin.setLoggedinAt(Instant.now());
    return adminRepository.update(admin);
  }

  public void deleteAdmin(Admin admin, Long targetAdminId) {
    Admin target = adminRepository.findStrictById(targetAdminId);
    if (!AdminUtil.isDeletableAdmin(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", targetAdminId));

    deleteService.deleteAdmin(target);
  }

  public void createAdminTemporary(Admin admin, CreateAdminTemporaryCommand command) {
    if (!AdminUtil.isCreatableAdmin(admin, command.getCommunityId(), command.getRoleId())) throw new ForbiddenException(String.format("[communityId=%d, roleId=%d]", command.getCommunityId(), command.getRoleId()));
    
    validate(command);
    if (adminRepository.isEmailAddressTaken(command.getEmailAddress())) throw new DisplayableException("error.emailAddressTaken");
    
    Community community = command.getCommunityId() == null ? null : communityRepository.findPublicStrictById(command.getCommunityId());
    Role role = Role.of(command.getRoleId());
    String photoUrl = command.getUploadPhotoCommand().getPhotoFile() == null ? null : imageService.create(command.getUploadPhotoCommand(), "");

    String accessId = accessIdService.generateAccessId(id -> {
      String accessIdHash = cryptoService.hash(id);
      return !adminRepository.findTemporaryAdmin(accessIdHash).isPresent();
    });

    TemporaryAdmin tmpAdmin = new TemporaryAdmin()
        .setAccessIdHash(cryptoService.hash(accessId))
        .setExpirationTime(Instant.now().plus(1, ChronoUnit.DAYS))
        .setInvalid(false)
        .setEmailAddress(command.getEmailAddress())
        .setAdminname(command.getAdminname())
        .setPasswordHash(cryptoService.encryptoPassword(command.getPassword()))
        .setCommunity(community)
        .setRole(role)
        .setTelNo(command.getTelNo())
        .setDepartment(command.getDepartment())
        .setPhotoUrl(photoUrl);
    
    adminRepository.createTemporary(tmpAdmin);
    emailService.sendCreateAdminTemporary(tmpAdmin.getEmailAddress(), tmpAdmin.getAdminname(), accessId);
  }

  public Admin createAdminComplete(String accessId) {
    TemporaryAdmin tmpAdmin = adminRepository.findStrictTemporaryAdmin(cryptoService.hash(accessId));
    adminRepository.lockForUpdate(tmpAdmin);
    
    Admin admin = new Admin()
        .setEmailAddress(tmpAdmin.getEmailAddress())
        .setAdminname(tmpAdmin.getAdminname())
        .setPasswordHash(tmpAdmin.getPasswordHash())
        .setCommunity(tmpAdmin.getCommunity())
        .setRole(tmpAdmin.getRole())
        .setTelNo(tmpAdmin.getTelNo())
        .setDepartment(tmpAdmin.getDepartment())
        .setPhotoUrl(tmpAdmin.getPhotoUrl())
        .setLoggedinAt(Instant.now());
    
    adminRepository.updateTemporaryAdmin(tmpAdmin.setInvalid(true));
    return adminRepository.create(admin);
  }
  
  public Admin checkPassword(AdminLoginCommand command) {
    Admin admin = adminRepository.findByEmailAddress(command.getEmailAddress()).orElseThrow(AuthenticationException::new);
    if (!cryptoService.checkPassword(command.getPassword(), admin.getPasswordHash())) throw new AuthenticationException();
    return admin;
  }

  public AdminSession session(Admin admin) {
    return new AdminSession().setAdminId(admin.getId()).setAdminEmailAddress(admin.getEmailAddress());
  }

  private void validate(CreateAdminTemporaryCommand command) {
    ValidateUtil.validatePassword(command.getPassword());
    ValidateUtil.validateEmailAddress(command.getEmailAddress());
    ValidateUtil.validateTelNo(command.getTelNo());
    ValidateUtil.validateRole(command.getRoleId());
  }

  private void validate(UpdateAdminCommand command) {
    if (StringUtils.isEmpty(command.getAdminname())) throw new BadRequestException("adminname is empty");
    ValidateUtil.validateTelNo(command.getTelNo());
  }
}
