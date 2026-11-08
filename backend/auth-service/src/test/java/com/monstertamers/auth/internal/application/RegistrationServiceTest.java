package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import com.monstertamers.auth.internal.web.dto.request.ConfirmationRequest;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import com.monstertamers.auth.error.ApiException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationServiceTest {

    private static final String EMAIL = "tamer@example.com";
    private static final RegistrationRequest REQUEST =
            new RegistrationRequest(EMAIL, "secret123", "secret123");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RegistrationAttemptRepository registrationAttemptRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AccountService accountService;

    @Mock
    private RegistrationAttemptCounterService registrationAttemptCounterService;

    @InjectMocks
    private RegistrationService registrationService;

    @Captor
    private ArgumentCaptor<RegistrationAttempt> savedAttempt;

    @Nested
    class Register {

        @BeforeEach
        void setUp() {
            when(codeGenerator.confirmationCode()).thenReturn("123456");
            when(passwordEncoder.encode("secret123")).thenReturn("password-hash");
            when(passwordEncoder.encode("123456")).thenReturn("code-hash");
        }

        @Test
        void takenEmail_rejected() {
            // given
            when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when
            Throwable thrown = catchThrowable(() -> registrationService.register(REQUEST));

            // then
            assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.email_taken");
            });
            verifyNoInteractions(registrationAttemptRepository, eventPublisher);
        }

        @Test
        void freeEmail_succeeds() {
            // given
            RegistrationAttempt saved = new RegistrationAttempt();
            saved.setId(UUID.randomUUID());
            when(registrationAttemptRepository.save(any())).thenReturn(saved);

            // when
            RegistrationResponse response = registrationService.register(REQUEST);

            // then
            assertThat(response.registrationId()).isEqualTo(saved.getId());
            InOrder order = inOrder(registrationAttemptRepository);
            order.verify(registrationAttemptRepository).deleteAllByEmail(EMAIL);
            order.verify(registrationAttemptRepository).save(savedAttempt.capture());
            RegistrationAttempt attempt = savedAttempt.getValue();
            assertThat(attempt.getId()).isNull();
            assertThat(attempt.getEmail()).isEqualTo(EMAIL);
            assertThat(attempt.getPassword()).isEqualTo("password-hash");
            assertThat(attempt.getCode()).isEqualTo("code-hash");
            verify(eventPublisher).publishEvent(new ConfirmationCodeIssuedEvent(EMAIL, saved.getId(), "123456"));
        }
    }

    @Nested
    class Confirm {

        private RegistrationAttempt attempt() {
            RegistrationAttempt attempt = new RegistrationAttempt();
            attempt.setId(UUID.randomUUID());
            attempt.setEmail(EMAIL);
            attempt.setPassword("password-hash");
            attempt.setCode("code-hash");
            attempt.setCreatedAt(Instant.now());
            attempt.setFailedAttempts(0);
            return attempt;
        }

        @Test
        void unknownRegistration_rejected() {
            // given
            UUID registrationId = UUID.randomUUID();
            ConfirmationRequest request = new ConfirmationRequest("123456");
            when(registrationAttemptRepository.findById(registrationId)).thenReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(() -> registrationService.confirm(registrationId, request));

            // then
            assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.confirmation_code_invalid");
            });
        }

        @Test
        void wrongCode_rejected_and_counter_incremented() {
            // given
            RegistrationAttempt attempt = attempt();
            ConfirmationRequest request = new ConfirmationRequest("000000");
            
            when(registrationAttemptRepository.findById(attempt.getId())).thenReturn(Optional.of(attempt));
            when(passwordEncoder.matches("000000", "code-hash")).thenReturn(false);

            // when
            Throwable thrown = catchThrowable(() -> registrationService.confirm(attempt.getId(), request));

            // then
            assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.confirmation_code_invalid");
            });

            verify(registrationAttemptCounterService).incrementFailedAttempts(attempt);
        }

        @Test
        void takenEmail_rejected() {
            // given
            RegistrationAttempt attempt = attempt();

            ConfirmationRequest request = new ConfirmationRequest("123456");
            when(registrationAttemptRepository.findById(attempt.getId())).thenReturn(Optional.of(attempt));
            when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);
            when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when
            Throwable thrown = catchThrowable(() -> registrationService.confirm(attempt.getId(), request));

            // then
            assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.email_taken");
            });
            verifyNoInteractions(accountService);
        }

        @Test
        void validCode_succeeds() {
            // given
            RegistrationAttempt attempt = attempt();

            ConfirmationRequest request = new ConfirmationRequest("123456");
            when(registrationAttemptRepository.findById(attempt.getId())).thenReturn(Optional.of(attempt));
            when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);

            // when
            registrationService.confirm(attempt.getId(), request);

            // then
            verify(accountService).create(EMAIL, "password-hash");
            verify(registrationAttemptRepository).delete(attempt);
        }

        @Test
        void expiredCode_rejected() {
            // given
            RegistrationAttempt attempt = attempt();
            attempt.setCreatedAt(Instant.now().minus(16, ChronoUnit.MINUTES));

            when(registrationAttemptRepository.findById(attempt.getId()))
                    .thenReturn(Optional.of(attempt));
            
            //when
            Throwable thrown = catchThrowable(() ->
                    registrationService.confirm(
                            attempt.getId(),
                            new ConfirmationRequest("123456")));

            //then
            assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(ex.getBody().getProperties())
                        .containsEntry("code", "auth.confirmation_code_expired");
            });

            verifyNoInteractions(passwordEncoder, accountService);
        }

        @Test
        void five_failed_attempts_correctCode_rejected() {
            //given
            RegistrationAttempt attempt = attempt();
            attempt.setFailedAttempts(5);

            when(registrationAttemptRepository.findById(attempt.getId()))
                    .thenReturn(Optional.of(attempt));
            
            //when
            Throwable thrown = catchThrowable(() ->
                    registrationService.confirm(
                            attempt.getId(),
                            new ConfirmationRequest("123456")));

            //then
            assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                assertThat(ex.getBody().getProperties())
                        .containsEntry(
                                "code",
                                "auth.confirmation_attempts_exceeded");
            });

            verifyNoInteractions(passwordEncoder, accountService);
        }

        @Test 
        void correctCode_after_five_failed_attempts_rejected(){
            //given
            RegistrationAttempt attempt = attempt();
            attempt.setFailedAttempts(5);

            //when
            when(registrationAttemptRepository.findById(attempt.getId()))
                    .thenReturn(Optional.of(attempt));

            Throwable thrown = catchThrowable(() ->
                    registrationService.confirm(
                            attempt.getId(),
                            new ConfirmationRequest("123456")));
            
            //then
             assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                assertThat(ex.getBody().getProperties())
                        .containsEntry(
                                "code",
                                "auth.confirmation_attempts_exceeded");
            });

            verifyNoInteractions(passwordEncoder, accountService);
        }
    }
}
