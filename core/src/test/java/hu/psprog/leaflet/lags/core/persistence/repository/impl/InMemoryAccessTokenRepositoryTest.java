package hu.psprog.leaflet.lags.core.persistence.repository.impl;

import hu.psprog.leaflet.lags.core.domain.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.TokenStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link InMemoryAccessTokenRepository}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class InMemoryAccessTokenRepositoryTest {

    private static final String JTI_1 = "jti-1";
    private static final String JTI_2 = "jti-2";
    private static final AccessTokenInfo ACCESS_TOKEN_INFO_1 = new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().id(JTI_1).build());
    private static final AccessTokenInfo ACCESS_TOKEN_INFO_2 = new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().id(JTI_2).build());

    @InjectMocks
    private InMemoryAccessTokenRepository inMemoryAccessTokenRepository;

    @Test
    public void shouldSaveStoreAccessTokenInfoInStorageMap() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();

        // when
        inMemoryAccessTokenRepository.save(ACCESS_TOKEN_INFO_1);

        // then
        assertThat(repository.size(), equalTo(1));
        assertThat(repository.get(JTI_1), equalTo(ACCESS_TOKEN_INFO_1));
    }

    @Test
    public void shouldRetrieveByJTIReturnAccessTokenInfo() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();
        repository.put(JTI_1, ACCESS_TOKEN_INFO_1);

        // when
        Optional<AccessTokenInfo> result = inMemoryAccessTokenRepository.retrieveByJTI(JTI_1);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(ACCESS_TOKEN_INFO_1));
    }

    @Test
    public void shouldRetrieveByJTIReturnEmptyOptionalOnMissingEntry() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();

        // when
        Optional<AccessTokenInfo> result = inMemoryAccessTokenRepository.retrieveByJTI(JTI_1);

        // then
        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void shouldGetAllAccessTokenInfoReturnListOfEntries() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();
        repository.put(JTI_1, ACCESS_TOKEN_INFO_1);
        repository.put(JTI_2, ACCESS_TOKEN_INFO_2);

        // when
        List<AccessTokenInfo> result = inMemoryAccessTokenRepository.getAllAccessTokenInfo();

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, hasItems(ACCESS_TOKEN_INFO_1, ACCESS_TOKEN_INFO_2));
    }

    @Test
    public void shouldDeleteByJTIRemoveIdentifiedItem() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();
        repository.put(JTI_1, ACCESS_TOKEN_INFO_1);

        // when
        inMemoryAccessTokenRepository.deleteByJTI(JTI_1);

        // then
        assertThat(repository.isEmpty(), is(true));
    }

    @Test
    public void shouldDeleteByJTIFallThroughSilentlyIfItemIsNotPresent() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();
        assertThat(repository.isEmpty(), is(true));

        // when
        inMemoryAccessTokenRepository.deleteByJTI(JTI_1);

        // then
        assertThat(repository.isEmpty(), is(true));
    }

    @Test
    public void shouldUpdateStatusByJTIUpdateStatusAndRevocationDate() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();
        repository.put(JTI_1, new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().id(JTI_1).build()));
        assertThat(repository.get(JTI_1).getStatus(), equalTo(TokenStatus.ACTIVE));

        // when
        inMemoryAccessTokenRepository.updateStatusByJTI(JTI_1, TokenStatus.REVOKED);

        // then
        assertThat(repository.size(), equalTo(1));
        assertThat(repository.get(JTI_1).getStatus(), equalTo(TokenStatus.REVOKED));

        Date revokedAt = repository.get(JTI_1).getRevokedAt();
        assertThat(revokedAt, notNullValue());
        assertThat(System.currentTimeMillis() - revokedAt.getTime() < 100, is(true));
    }

    @Test
    public void shouldUpdateStatusByJTIFallThroughSilentlyIfItemIsNotPresent() throws IllegalAccessException {

        // given
        Map<String, AccessTokenInfo> repository = getRepository();
        assertThat(repository.isEmpty(), is(true));

        // when
        inMemoryAccessTokenRepository.updateStatusByJTI(JTI_1, TokenStatus.REVOKED);

        // then
        // silent fall-through expected
        assertThat(repository.isEmpty(), is(true));
    }

    private Map<String, AccessTokenInfo> getRepository() throws IllegalAccessException {

        Field repositoryField = ReflectionUtils.findField(InMemoryAccessTokenRepository.class, "accessTokenInfoStorage");
        repositoryField.setAccessible(true);

        return (Map<String, AccessTokenInfo>) repositoryField.get(inMemoryAccessTokenRepository);
    }
}
