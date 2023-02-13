package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private Clock clock;
    @InjectMocks
    private SubscriptionService subscriptionService;


    @Test
    void upsertWhenSubstitutionIsExist() {
        var subscription = getSubscription();
        var createSubscriptionDto = getSubscriptionDto();
        doReturn(List.of(subscription)).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        doReturn(subscription).when(subscriptionDao).upsert(subscription);
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult).isEqualTo(subscription);
        verifyNoInteractions(createSubscriptionMapper);
        verify(subscriptionDao).upsert(subscription);
    }


    @Test
    void upsertWhenSubstitutionIsNotExist() {
        var subscription = getSubscription();
        var createSubscriptionDto = getSubscriptionDto();
        doReturn(emptyList()).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        doReturn(subscription).when(createSubscriptionMapper).map(createSubscriptionDto);
        doReturn(subscription).when(subscriptionDao).upsert(subscription);
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult).isEqualTo(subscription);
        verify(subscriptionDao).upsert(subscription);
    }

    @Test
    void shouldThrowExceptionIfDtoInvalid() {
        var createSubscriptionDto = getSubscriptionDto();
        var validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "userId is invalid"));
        doReturn(validationResult).when(createSubscriptionValidator).validate(createSubscriptionDto);

        assertThrows(ValidationException.class, () -> subscriptionService.upsert(createSubscriptionDto));
        verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
    }

    @Test
    void cancelSuccess() {
        ArgumentCaptor<Subscription> argumentCaptor = ArgumentCaptor.forClass(Subscription.class);
        var subscription = getSubscription();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        var expectedResult = getSubscription();
        expectedResult.setStatus(Status.CANCELED);
        doReturn(expectedResult).when(subscriptionDao).update(subscription);

        subscriptionService.cancel(anyInt());

        verify(subscriptionDao).update(argumentCaptor.capture());
        var actualResult = argumentCaptor.getValue();
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionIfIllegalArgument() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(anyInt()));
    }

    @Test
    void shouldThrowExceptionIfStatusIsNotActive() {
        var subscription = getSubscription();
        subscription.setStatus(Status.CANCELED);
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(2);

        var exception = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(2));
        assertEquals("Only active subscription 2 can be canceled", exception.getMessage());
    }

    @Test
    void expireSuccess() {
        ArgumentCaptor<Subscription> argumentCaptor = ArgumentCaptor.forClass(Subscription.class);
        var subscription = getSubscription();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        var expectedResult = getSubscription();
        expectedResult.setStatus(Status.EXPIRED);
        expectedResult.setExpirationDate(Instant.now(clock));

        subscriptionService.expire(anyInt());

        verify(subscriptionDao).update(argumentCaptor.capture());
        var actualResult = argumentCaptor.getValue();
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionIfStatusIsExpired() {
        var subscription = getSubscription();
        subscription.setStatus(Status.EXPIRED);
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(2);

        var exception = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(2));
        assertEquals("Subscription 2 has already expired", exception.getMessage());
    }


    private Subscription getSubscription() {
        return Subscription.builder()
                .userId(1)
                .name("testName")
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