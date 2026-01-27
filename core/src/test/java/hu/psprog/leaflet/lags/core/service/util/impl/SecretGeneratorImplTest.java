package hu.psprog.leaflet.lags.core.service.util.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit tests for {@link SecretGeneratorImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class SecretGeneratorImplTest {

    @InjectMocks
    private SecretGeneratorImpl secretGenerator;

    @Test
    public void shouldGenerateSecret() {

        // when
        var result = secretGenerator.generateSecret();

        // then
        var decoded = Base64.getDecoder().decode(result);
        assertThat(decoded.length, equalTo(48));
    }
}
