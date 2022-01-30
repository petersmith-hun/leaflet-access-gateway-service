package hu.psprog.leaflet.lags.core.persistence.repository.impl;

import hu.psprog.leaflet.lags.core.domain.OngoingAuthorization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link InMemoryOngoingAuthorizationRepository}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class InMemoryOngoingAuthorizationRepositoryTest {

    private static final String AUTHORIZATION_CODE = "code-1";
    private static final OngoingAuthorization ONGOING_AUTHORIZATION = OngoingAuthorization.builder()
            .authorizationCode(AUTHORIZATION_CODE)
            .build();

    @InjectMocks
    private InMemoryOngoingAuthorizationRepository inMemoryOngoingAuthorizationRepository;

    @Test
    public void shouldGetOngoingAuthorizationByCodeReturnRequestItem() throws IllegalAccessException {

        // given
        Map<String, OngoingAuthorization> repository = getRepository();
        assertThat(repository.isEmpty(), is(true));
        repository.put(AUTHORIZATION_CODE, ONGOING_AUTHORIZATION);

        // when
        Optional<OngoingAuthorization> result = inMemoryOngoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(ONGOING_AUTHORIZATION));
    }

    @Test
    public void shouldSaveOnGoingAuthorizationStoreItem() throws IllegalAccessException {

        // given
        Map<String, OngoingAuthorization> repository = getRepository();
        assertThat(repository.isEmpty(), is(true));

        // when
        inMemoryOngoingAuthorizationRepository.saveOngoingAuthorization(ONGOING_AUTHORIZATION);

        // then
        assertThat(repository.get(AUTHORIZATION_CODE), equalTo(ONGOING_AUTHORIZATION));
    }

    @Test
    public void shouldDeleteOngoingAuthorizationRemoveItem() throws IllegalAccessException {

        // given
        Map<String, OngoingAuthorization> repository = getRepository();
        assertThat(repository.isEmpty(), is(true));
        repository.put(AUTHORIZATION_CODE, ONGOING_AUTHORIZATION);
        assertThat(inMemoryOngoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE).isPresent(), is(true));

        // when
        inMemoryOngoingAuthorizationRepository.deleteOngoingAuthorization(AUTHORIZATION_CODE);

        // then
        assertThat(repository.isEmpty(), is(true));
        assertThat(inMemoryOngoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE).isPresent(), is(false));
    }

    private Map<String, OngoingAuthorization> getRepository() throws IllegalAccessException {

        Field repositoryField = ReflectionUtils.findField(InMemoryOngoingAuthorizationRepository.class, "ongoingAuthorizationStorage");
        repositoryField.setAccessible(true);

        return (Map<String, OngoingAuthorization>) repositoryField.get(inMemoryOngoingAuthorizationRepository);
    }
}
