package commonsos.service;

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
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;

@RunWith(MockitoJUnitRunner.class)
public class CommunityServiceTest {

  @Mock CommunityRepository repository;
  @InjectMocks @Spy CommunityService service;

  @Test
  public void community_community_found() {
    when(repository.findById(any())).thenReturn(Optional.of(new Community()));
    Community community = service.community(1L);

    assertThat(community).isNotNull();
  }

  @Test(expected = BadRequestException.class)
  public void community_community_not_found() {
    when(repository.findById(any())).thenReturn(Optional.empty());
    service.community(1L);
  }
}