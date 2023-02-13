package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("testName")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS))
                .build();

        var actualResult = mapper.map(dto);
        var expectedResult = Subscription.builder()
                .userId(1)
                .name("testName")
                .provider(Provider.APPLE)
                .expirationDate((Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS)))
                .status(Status.ACTIVE)
                .build();

        Assertions.assertThat(actualResult).isEqualTo(expectedResult);

    }
}