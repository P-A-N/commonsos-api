package commonsos.service.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;
import commonsos.util.CommunityUtil;

@RunWith(MockitoJUnitRunner.class)
public class CommunityServiceTest {

  @Mock CommunityRepository repository;
  @Mock CommunityUtil communityUtil;
  @InjectMocks @Spy CommunityService service;

  @Test
  public void community_community_found() {
    when(repository.findById(any())).thenReturn(Optional.of(new Community()));
    when(communityUtil.view(any())).thenCallRealMethod();
    CommunityView view = service.view(1L);

    assertThat(view).isNotNull();
  }

  @Test(expected = BadRequestException.class)
  public void community_community_not_found() {
    when(repository.findById(any())).thenReturn(Optional.empty());
    service.view(1L);
  }
}