package commonsos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.exception.BadRequestException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.entity.Community;

@ExtendWith(MockitoExtension.class)
public class CommunityServiceTest {

  @Mock CommunityRepository repository;
  @InjectMocks @Spy CommunityService service;

  @Test
  public void community_community_found() {
    when(repository.findPublicById(any())).thenReturn(Optional.of(new Community()));
    Community community = service.community(1L);

    assertThat(community).isNotNull();
  }

  @Test
  public void community_community_not_found() {
    when(repository.findPublicById(any())).thenReturn(Optional.empty());

    assertThrows(BadRequestException.class, () -> service.community(1L));
  }
}