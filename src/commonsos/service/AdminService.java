package commonsos.service;

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.AuthenticationException;
import commonsos.repository.AdminRepository;
import commonsos.repository.entity.Admin;
import commonsos.service.command.AdminLoginCommand;
import commonsos.service.crypto.CryptoService;
import commonsos.session.AdminSession;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class AdminService {

  @Inject private AdminRepository adminRepository;
  @Inject private CryptoService cryptoService;
  
  public Admin getAdmin(Long id) {
    return adminRepository.findStrictById(id);
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

}
