package com.prreviewer.service;

import com.prreviewer.dto.RepositoryResponse;
import com.prreviewer.exception.RepositoryOwnershipException;
import com.prreviewer.github.GitHubRepoDto;
import com.prreviewer.github.GitHubService;
import com.prreviewer.model.Repository;
import com.prreviewer.model.User;
import com.prreviewer.repository.RepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ownership validation in {@link RepositoryService#selectRepository}.
 *
 * <p>Verifies the two acceptance cases:
 * <ul>
 *   <li>Case 1: valid githubRepoId → 200 OK, repo persisted</li>
 *   <li>Case 2: unknown githubRepoId → 403 Forbidden, nothing persisted</li>
 * </ul>
 *
 * <p>GitHub API and DB calls are mocked — this test is purely about the
 * ownership decision logic, not I/O. No Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RepositoryService — ownership validation")
class RepositoryServiceOwnershipTest {

    @Mock GitHubService        githubService;
    @Mock RepositoryRepository repositoryRepository;

    @InjectMocks
    RepositoryService repositoryService;

    // ─── Test fixtures ────────────────────────────────────────────────

    /** A user whose access token will be used for all GitHub calls. */
    private User testUser;

    /** A repo that this user can access (simulates "Repo A"). */
    private static final long ACCESSIBLE_REPO_ID = 123456789L;

    /** A repo ID that is NOT in the user's accessible list (simulates the 999999999 case). */
    private static final long INACCESSIBLE_REPO_ID = 999999999L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .githubId("42")
                .username("octocat")
                .email("octocat@github.com")
                .accessToken("gho_test_token")
                .build();
        // Simulate a persisted user with a DB id
        // (field is set by JPA normally; we use reflection-free approach: mock the getId())
    }

    // ─── Helper: build a realistic GitHubRepoDto ─────────────────────

    private GitHubRepoDto makeRepoDto(long id, String ownerLogin, String repoName) {
        // DTO getters are consumed by persistNewRepository(). In the idempotency test
        // the code short-circuits to the existing DB record before reading them, so
        // Mockito strict mode would flag them as unnecessary. lenient() silences that
        // without weakening any other assertion in the test.
        GitHubRepoDto dto = mock(GitHubRepoDto.class);
        lenient().when(dto.getId()).thenReturn(id);
        lenient().when(dto.getOwnerLogin()).thenReturn(ownerLogin);
        lenient().when(dto.getName()).thenReturn(repoName);
        lenient().when(dto.getFullName()).thenReturn(ownerLogin + "/" + repoName);
        return dto;
    }

    // ─── Case 1: POST { githubRepoId: ACCESSIBLE_REPO_ID } → 200 OK ─

    @Test
    @DisplayName("Case 1: accessible repo → persisted and returned")
    void selectRepository_whenRepoIsAccessible_persistsAndReturns() {
        // GitHub confirms the repo exists for this user
        GitHubRepoDto accessibleRepo = makeRepoDto(ACCESSIBLE_REPO_ID, "octocat", "hello-world");
        when(githubService.fetchRepositoryById(testUser.getAccessToken(), ACCESSIBLE_REPO_ID))
                .thenReturn(Optional.of(accessibleRepo));

        // No existing record — first time selecting this repo
        when(repositoryRepository.findByGithubRepoIdAndUserId(ACCESSIBLE_REPO_ID, testUser.getId()))
                .thenReturn(Optional.empty());

        // Simulate save() returning the entity with an ID
        Repository savedEntity = Repository.builder()
                .githubRepoId(ACCESSIBLE_REPO_ID)
                .owner("octocat")
                .name("hello-world")
                .fullName("octocat/hello-world")
                .webhookEnabled(false)
                .user(testUser)
                .build();
        when(repositoryRepository.save(any(Repository.class))).thenReturn(savedEntity);

        // ── Act ──
        RepositoryResponse response = repositoryService.selectRepository(testUser, ACCESSIBLE_REPO_ID);

        // ── Assert: response is correct ──
        assertThat(response).isNotNull();
        assertThat(response.getGithubRepoId()).isEqualTo(ACCESSIBLE_REPO_ID);
        assertThat(response.getOwner()).isEqualTo("octocat");
        assertThat(response.getName()).isEqualTo("hello-world");
        assertThat(response.getFullName()).isEqualTo("octocat/hello-world");
        assertThat(response.isSelected()).isTrue();
        assertThat(response.isWebhookEnabled()).isFalse();

        // ── Assert: entity was actually persisted ──
        ArgumentCaptor<Repository> savedCaptor = ArgumentCaptor.forClass(Repository.class);
        verify(repositoryRepository).save(savedCaptor.capture());
        Repository entityToPersist = savedCaptor.getValue();
        assertThat(entityToPersist.getGithubRepoId()).isEqualTo(ACCESSIBLE_REPO_ID);
        assertThat(entityToPersist.getFullName()).isEqualTo("octocat/hello-world");
        assertThat(entityToPersist.getUser()).isEqualTo(testUser);
    }

    // ─── Case 2: POST { githubRepoId: 999999999 } → 403, nothing saved ──

    @Test
    @DisplayName("Case 2: inaccessible repo ID → 403, nothing inserted into DB")
    void selectRepository_whenRepoNotAccessible_throws403AndNothingPersisted() {
        // GitHub does NOT return this repo ID for this user's token
        when(githubService.fetchRepositoryById(testUser.getAccessToken(), INACCESSIBLE_REPO_ID))
                .thenReturn(Optional.empty());

        // ── Act & Assert: 403 exception is thrown ──
        assertThatThrownBy(() ->
                repositoryService.selectRepository(testUser, INACCESSIBLE_REPO_ID))
                .isInstanceOf(RepositoryOwnershipException.class)
                .hasMessageContaining(String.valueOf(INACCESSIBLE_REPO_ID));

        // ── Assert: NOTHING was written to the DB ──
        verify(repositoryRepository, never()).save(any());
        verify(repositoryRepository, never()).findByGithubRepoIdAndUserId(anyLong(), anyLong());
    }

    // ─── Bonus: idempotency — re-selecting an already-persisted repo ──

    @Test
    @DisplayName("Idempotency: re-selecting an already-monitored repo returns existing record, no duplicate insert")
    void selectRepository_whenAlreadyPersisted_returnsExistingWithoutInsert() {
        GitHubRepoDto accessibleRepo = makeRepoDto(ACCESSIBLE_REPO_ID, "octocat", "hello-world");
        when(githubService.fetchRepositoryById(testUser.getAccessToken(), ACCESSIBLE_REPO_ID))
                .thenReturn(Optional.of(accessibleRepo));

        Repository existingEntity = Repository.builder()
                .githubRepoId(ACCESSIBLE_REPO_ID)
                .owner("octocat")
                .name("hello-world")
                .fullName("octocat/hello-world")
                .webhookEnabled(false)
                .user(testUser)
                .build();

        // Already exists in DB for this user
        when(repositoryRepository.findByGithubRepoIdAndUserId(ACCESSIBLE_REPO_ID, testUser.getId()))
                .thenReturn(Optional.of(existingEntity));

        // ── Act ──
        RepositoryResponse response = repositoryService.selectRepository(testUser, ACCESSIBLE_REPO_ID);

        // ── Assert: correct response returned ──
        assertThat(response.getGithubRepoId()).isEqualTo(ACCESSIBLE_REPO_ID);
        assertThat(response.isSelected()).isTrue();

        // ── Assert: no duplicate insert ──
        verify(repositoryRepository, never()).save(any());
    }
}
