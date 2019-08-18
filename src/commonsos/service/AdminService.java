package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.AuthenticationException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdminRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.Role;
import commonsos.repository.entity.TemporaryAdmin;
import commonsos.service.command.AdminLoginCommand;
import commonsos.service.command.CreateAdminTemporaryCommand;
import commonsos.service.command.PaginationCommand;
import commonsos.service.crypto.AccessIdService;
import commonsos.service.crypto.CryptoService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageUploadService;
import commonsos.session.AdminSession;
import commonsos.util.AdminUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.admin.AdminListView;

@Singleton
public class AdminService {

  @Inject private AdminRepository adminRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private CryptoService cryptoService;
  @Inject private AccessIdService accessIdService;
  @Inject private ImageUploadService imageService;
  @Inject private EmailService emailService;
  
  public Admin getAdmin(Long id) {
    return adminRepository.findStrictById(id);
  }

  public Admin getAdmin(Admin admin, Long id) {
    Admin target = adminRepository.findStrictById(id);
    if (!AdminUtil.isSeeable(admin, target)) throw new ForbiddenException(String.format("[targetAdminId=%d]", id));
    
    return target;
  }

  public AdminListView searchAdmin(Admin admin, Long communityId, Long roleId, PaginationCommand pagination) {
    // validate role
    if (!AdminUtil.isSeeable(admin, communityId, roleId)) throw new ForbiddenException();
    
    ResultList<Admin> result = adminRepository.findByCommunityIdAndRoleId(communityId, roleId, pagination);

    AdminListView listView = new AdminListView();
    listView.setAdminList(result.getList().stream().map(AdminUtil::toView).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public void createAdminTemporary(Admin admin, CreateAdminTemporaryCommand command) {
    if (!AdminUtil.isCreatable(admin, command.getCommunityId(), command.getRoleId())) throw new ForbiddenException(String.format("[communityId=%d, roleId=%d]", command.getCommunityId(), command.getRoleId()));
    
    validate(command);
    if (adminRepository.isEmailAddressTaken(command.getEmailAddress())) throw new DisplayableException("error.emailAddressTaken");
    
    Community community = command.getCommunityId() == null ? null : communityRepository.findPublicStrictById(command.getCommunityId());
    Role role = Role.of(command.getRoleId());
    String photoUrl = command.getUploadPhotoCommand().getPhotoFile() == null ? null : imageService.create(command.getUploadPhotoCommand());

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

  public Admin updateLoggedinAt(Admin admin) {
    admin.setLoggedinAt(Instant.now());
    return adminRepository.update(admin);
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
}
