package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    private Clock clock;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        clock = Clock.systemDefaultZone();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                clock);
    }

    @Test
    void upsertIfNotExist() {
        var subscriptionDto = getSubscriptionDto();

        var actualResult = subscriptionService.upsert(subscriptionDto);

        assertNotNull(actualResult.getId());
    }

    @Test
    void upsertIfExist() {
        var subscriptionDto = getSubscriptionDto();
        var expectedResult = subscriptionDao.upsert(getSubscription(1, "testName"));
        expectedResult.setStatus(Status.CANCELED);

        var actualResult = subscriptionService.upsert(subscriptionDto);

        assertThat(actualResult.getStatus()).isNotEqualTo(expectedResult.getStatus());
    }

    @Test
    void cancel() {
        var subscription = subscriptionDao.upsert(getSubscription(1, "testName"));

        subscriptionService.cancel(subscription.getId());
        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult.get().getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    void expire() {
        var subscription = subscriptionDao.upsert(getSubscription(1, "testName"));

        subscriptionService.expire(subscription.getId());
        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult.get().getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(actualResult.get().getExpirationDate().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(Instant.now(clock).truncatedTo(ChronoUnit.SECONDS));
    }

    private Subscription getSubscription(Integer userId, String name) {
        return Subscription.builder()
                .userId(userId)
                .name(name)
                .provider(Provider.APPLE)
                .expirationDate((Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS)))
                .status(Status.ACTIVE)
                .build();
    }

    private CreateSubscriptionDto getSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("testName")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}