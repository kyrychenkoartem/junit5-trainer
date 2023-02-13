package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidation() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("testName")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        var actualResult = validator.validate(dto);

        assertFalse(actualResult.hasErrors());
    }

    @Test
    void invalidUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("testName")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(100);
    }

    @Test
    void invalidName() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(101);
    }

    @Test
    void invalidProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("testName")
                .provider("provider")
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(102);
    }

    @Test
    void invalidExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("testName")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        var actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void invalidUserIdNameProviderExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("")
                .provider("provider")
                .expirationDate(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        var actualResult = validator.validate(dto);
        var errorCodes = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();

        assertThat(actualResult.getErrors()).hasSize(4);

        assertThat(errorCodes).contains(100, 101, 102, 103);
    }
}