package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.command.PaginationCommand;
import commonsos.command.admin.CreateRedistributionCommand;
import commonsos.command.admin.UpdateRedistributionCommand;
import commonsos.command.batch.CreateTokenTransactionForRedistributionCommand;
import commonsos.command.batch.RedistributionBatchCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.RedistributionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.util.PaginationUtil;
import commonsos.util.RedistributionUtil;
import commonsos.util.UserUtil;
import commonsos.view.RedistributionListView;

@Singleton
public class RedistributionService extends AbstractService {

  @Inject private RedistributionRepository repository;
  @Inject private CommunityRepository communityRepository;
  @Inject private UserRepository userRepository;
  @Inject private DeleteService deleteService;

  public Redistribution createRedistribution(Admin admin, CreateRedistributionCommand command) {
    // validate community
    Community community = communityRepository.findStrictById(command.getCommunityId());
    // validate role
    if (!RedistributionUtil.isEditable(admin, community.getId())) throw new ForbiddenException();
    
    // validate user
    User user = null;
    if (command.getUserId() != null) {
      user = userRepository.findStrictById(command.getUserId());
    }

    validateRedistribution(community, command.isAll(), user, command.getRedistributionRate(), null);
    
    // create redistribution
    Redistribution redistribution = new Redistribution()
        .setCommunity(community)
        .setAll(command.isAll())
        .setUser(command.isAll() ? null : user)
        .setRate(command.getRedistributionRate());
    
    return repository.create(redistribution);
  }
  
  public Redistribution updateRedistribution(Admin admin, UpdateRedistributionCommand command) {
    // validate role
    Community community = communityRepository.findStrictById(command.getCommunityId());
    if (!RedistributionUtil.isEditable(admin, community.getId())) throw new ForbiddenException();
    
    Redistribution redistribution = repository.findStrictById(command.getRedistributionId());
    if (!community.equals(redistribution.getCommunity())) throw new BadRequestException("community doesn't match");

    User user = null;
    if (command.getUserId() != null) {
      user = userRepository.findStrictById(command.getUserId());
    }
    
    validateRedistribution(community, command.isAll(), user, command.getRedistributionRate(), redistribution.getRate());
    
    // update redistribution
    repository.lockForUpdate(redistribution);
    redistribution
        .setAll(command.isAll())
        .setUser(command.isAll() ? null : user)
        .setRate(command.getRedistributionRate());
    repository.update(redistribution);
    
    return redistribution;
  }
  
  public void deleteRedistribution(Admin admin, Long communityId, Long redistributionId) {
    // validate role
    Community community = communityRepository.findStrictById(communityId);
    if (!RedistributionUtil.isEditable(admin, community.getId())) throw new ForbiddenException();
    
    Redistribution redistribution = repository.findStrictById(redistributionId);
    if (!community.equals(redistribution.getCommunity())) throw new BadRequestException("community doesn't match");
    
    deleteService.deleteRedistribution(redistribution);
  }
  
  public Redistribution getRedistribution(Admin admin, Long redistributionId, Long communityId) {
    // validate role
    if (!RedistributionUtil.isEditable(admin, communityId)) throw new ForbiddenException();
    
    Redistribution redistribution = repository.findStrictById(redistributionId);
    if (!redistribution.getCommunity().getId().equals(communityId)) throw new BadRequestException(String.format("redistribution is not of community [redistributionId=%d, communityId=%d]", redistributionId, communityId));
    
    return redistribution;
  }
  
  public RedistributionListView searchRedistribution(Admin admin, Long communityId, PaginationCommand pagination) {
    // validate role
    if (!RedistributionUtil.isEditable(admin, communityId)) throw new ForbiddenException();
    
    ResultList<Redistribution> result = repository.searchByCommunityId(communityId, pagination);

    RedistributionListView listView = new RedistributionListView();
    listView.setRedistributionList(result.getList().stream().map(RedistributionUtil::toView).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
  
  public RedistributionBatchCommand createRedistributionCommand() {
    Map<Community, List<CreateTokenTransactionForRedistributionCommand>> commandMap = new HashMap<>();
    
    List<Community> communityList = communityRepository.searchPublic(null).getList();
    communityList.forEach(c -> {
      List<CreateTokenTransactionForRedistributionCommand> commandList = new ArrayList<>();
      
      List<User> userList = userRepository.search(c.getId(), null, null).getList();
      userList.forEach(u -> {
        CreateTokenTransactionForRedistributionCommand command = new CreateTokenTransactionForRedistributionCommand()
            .setCommunity(c)
            .setUser(u)
            .setRate(BigDecimal.ZERO);
        commandList.add(command);
      });
      
      List<Redistribution> redistributionList = repository.searchByCommunityId(c.getId(), null).getList();
      redistributionList.forEach(r -> {
        commandList.forEach(command -> {
          BigDecimal currentRate = command.getRate();
          BigDecimal addRate = BigDecimal.ZERO;
          if (r.isAll()) {
            addRate = r.getRate().divide(BigDecimal.valueOf(userList.size()), BlockchainService.NUMBER_OF_DECIMALS, BigDecimal.ROUND_DOWN);
          } else if (command.getUser().getId().equals(r.getUser().getId())) {
            addRate = r.getRate();
          }
          command.setRate(currentRate.add(addRate));
        });
      });
      
      commandMap.put(c, commandList);
    });
    
    RedistributionBatchCommand command = new RedistributionBatchCommand()
        .setCommandMap(commandMap);
    return command;
  }
  
  private void validateRedistribution(Community community, boolean isAll, User user, BigDecimal newRate, BigDecimal currentRate) {
    // validate user
    if (user == null && !isAll) throw DisplayableException.getRequiredException("userId");
    if (user != null && !UserUtil.isMember(user, community)) throw DisplayableException.getNotMemberOfCommunity("user");
    // validate rate
    if (newRate == null) throw DisplayableException.getInvalidException("rate");
    
    BigDecimal totalRate = repository.sumByCommunityId(community.getId());
    if (currentRate != null) totalRate = totalRate.subtract(currentRate);
    
    if (newRate.compareTo(BigDecimal.ZERO) <= 0) throw DisplayableException.getInvalidException("rate");
    if (totalRate.add(newRate).compareTo(BigDecimal.valueOf(100L)) > 0) throw DisplayableException.getInvalidException("rate");
  }
}
