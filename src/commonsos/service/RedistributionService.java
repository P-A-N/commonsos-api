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
import commonsos.command.batch.CreateTokenTransactionForRedistributionCommand;
import commonsos.command.batch.RedistributionBatchCommand;
import commonsos.exception.BadRequestException;
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
    if (user == null && !command.isAll()) throw new BadRequestException("user is not specified");
    if (user != null && !UserUtil.isMember(user, community)) throw new BadRequestException("user is not a member of community");
    // validate rate
    if (command.getRedistributionRate() == null) throw new BadRequestException("redistritution rate is required");
    BigDecimal rate = command.getRedistributionRate();
    BigDecimal currentRate = repository.sumByCommunityId(command.getCommunityId());
    if (rate.compareTo(BigDecimal.ZERO) <= 0) throw new BadRequestException(String.format("Rate is less or equal to 0 [rate=%f]", rate));
    if (currentRate.add(rate).compareTo(BigDecimal.valueOf(100L)) > 0) throw new BadRequestException(String.format("Sum of rate is begger than 100 [rate=%f]", rate));
    
    // create redistribution
    Redistribution redistribution = new Redistribution()
        .setCommunity(community)
        .setAll(command.isAll())
        .setUser(command.isAll() ? null : user)
        .setRate(rate);
    
    return repository.create(redistribution);
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
    
    ResultList<Redistribution> result = repository.findByCommunityId(communityId, pagination);

    RedistributionListView listView = new RedistributionListView();
    listView.setRedistributionList(result.getList().stream().map(RedistributionUtil::toView).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
  
  public RedistributionBatchCommand createRedistributionCommand() {
    Map<Community, List<CreateTokenTransactionForRedistributionCommand>> commandMap = new HashMap<>();
    
    List<Community> communityList = communityRepository.listPublic(null).getList();
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
      
      List<Redistribution> redistributionList = repository.findByCommunityId(c.getId(), null).getList();
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
}
