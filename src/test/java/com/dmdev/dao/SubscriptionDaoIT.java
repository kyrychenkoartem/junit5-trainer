package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        var subscription1 = subscriptionDao.insert(getSubscription(1, "name1"));
        var subscription2 = subscriptionDao.insert(getSubscription(2, "name2"));
        var subscription3 = subscriptionDao.insert(getSubscription(3, "name3"));

        var actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        var subscriptionIds = actualResult.stream()
                .map(Subscription::getId)
                .toList();
        assertThat(subscriptionIds).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());
    }

    @Test
    void findById() {
        var subscription = subscriptionDao.insert(getSubscription(1, "name1"));

        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(subscription);
    }

    @Test
    void deleteExistingEntity() {
        var subscription = subscriptionDao.insert(getSubscription(1, "name1"));

        var actualResult = subscriptionDao.delete(subscription.getId());

        assertTrue(actualResult);
    }

    @Test
    void deleteNotExistingEntity() {
        var subscription = subscriptionDao.insert(getSubscription(1, "name1"));

        var actualResult = subscriptionDao.delete(234);

        assertFalse(actualResult);
    }

    @Test
    void update() {
        var subscription = getSubscription(1, "name1");
        subscriptionDao.insert(subscription);
        subscription.setName("updatedName");
        subscription.setProvider(Provider.GOOGLE);

        subscriptionDao.update(subscription);

        var updatedSubscription = subscriptionDao.findById(subscription.getId()).get();
        assertThat(updatedSubscription).isEqualTo(subscription);
    }

    @Test
    void insert() {
        var subscription = getSubscription(1, "name1");

        var actualResult = subscriptionDao.insert(subscription);

        assertNotNull(actualResult.getId());
    }

    @Test
    void findByUserId() {
        var subscription = subscriptionDao.insert(getSubscription(1, "name1"));

        var actualResult = subscriptionDao.findByUserId(subscription.getUserId());

        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult.get(0)).isEqualTo(subscription);
    }

    @Test
    void shouldNotFindByUserIdIfUserDoesNotExist() {
        subscriptionDao.insert(getSubscription(1, "name1"));
        subscriptionDao.insert(getSubscription(2, "name2"));
        subscriptionDao.insert(getSubscription(3, "name3"));

        var actualResult = subscriptionDao.findByUserId(4);

        assertThat(actualResult).isEmpty();
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

}