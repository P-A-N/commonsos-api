package commonsos.tools;

import java.util.Scanner;

import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import commonsos.di.Web3jProvider;
import commonsos.repository.AdminRepository;
import commonsos.repository.EntityManagerService;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Role;
import commonsos.service.crypto.CryptoService;

public class CreateNclAccount {

  private static EntityManagerService emService;
  private static AdminRepository adminRepository;
  private static CryptoService cryptoService;
  private static Scanner scanner = new Scanner(System.in);


  public static void main(String[] args) throws Exception {
    prepareInstances();

    System.out.println("Creating NCL account...");
    
    System.out.print("Please specify admin name: ");
    String adminname = scanner.nextLine();
    System.out.print("Please specify admin's email address: ");
    String emailAddress = scanner.nextLine();
    System.out.print("Please specify admin's password: ");
    String password = scanner.nextLine();
    
    Admin admin = new Admin()
        .setRole(Role.NCL)
        .setAdminname(adminname)
        .setEmailAddress(emailAddress)
        .setPasswordHash(cryptoService.encryptoPassword(password));
    emService.runInTransaction(() -> adminRepository.create(admin));

    System.out.print("Done.");
  }

  private static void prepareInstances() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override protected void configure() {
        bind(Web3j.class).toProvider(Web3jProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
      }
    });

    emService = injector.getInstance(EntityManagerService.class);
    adminRepository = injector.getInstance(AdminRepository.class);
    cryptoService = injector.getInstance(CryptoService.class);
  }

}
