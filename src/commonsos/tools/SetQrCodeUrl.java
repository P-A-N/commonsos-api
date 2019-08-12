package commonsos.tools;

import java.util.List;

import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import commonsos.di.Web3jProvider;
import commonsos.repository.EntityManagerService;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.User;
import commonsos.service.UserService;

public class SetQrCodeUrl {

  private static EntityManagerService emService;
  private static UserService userService;
  private static UserRepository userRepository;


  public static void main(String[] args) throws Exception {
    prepareInstances();

    System.out.println("Setting qrCodeUrl to users...");
    
    List<User> userList = emService.get().createQuery("FROM User WHERE deleted IS FALSE", User.class).getResultList();
    userList.forEach(u -> {
      String url = userService.getQrCodeUrl(u, null);
      emService.runInTransaction(() -> userRepository.update(u.setQrCodeUrl(url)));
      System.out.println(String.format("Set qrCodeUrl done. [username=%s]", u.getUsername()));
    });
    
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
    userService = injector.getInstance(UserService.class);
    userRepository = injector.getInstance(UserRepository.class);
  }

}
