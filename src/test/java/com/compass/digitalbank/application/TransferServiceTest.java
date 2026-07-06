package com.compass.digitalbank.application;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;
import com.compass.digitalbank.domain.exception.ForbiddenOperationException;
import com.compass.digitalbank.domain.exception.InactiveAccountException;
import com.compass.digitalbank.domain.exception.InsufficientBalanceException;
import com.compass.digitalbank.domain.exception.InvalidTransferException;
import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.model.TransferStatus;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.in.TransferUseCase.TransferCommand;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import com.compass.digitalbank.domain.port.out.DomainEventPublisher;
import com.compass.digitalbank.domain.port.out.TransferRepository;
import com.compass.digitalbank.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID OTHER_OWNER_ID = UUID.randomUUID();
    private static final UUID SOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DESTINATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private TransferService transferService;

    private Account source;
    private Account destination;

    @BeforeEach
    void setUp() {
        source = Account.restore(SOURCE_ID, OWNER_ID, "Source", new BigDecimal("100.00"), true, Instant.now());
        destination = Account.restore(DESTINATION_ID, OTHER_OWNER_ID, "Destination",
                new BigDecimal("20.00"), true, Instant.now());
    }

    private TransferCommand command(BigDecimal amount, String idempotencyKey) {
        return new TransferCommand(new Requester(OWNER_ID, false), SOURCE_ID, DESTINATION_ID, amount, idempotencyKey);
    }

    private void stubAccountsForTransfer() {
        when(accountRepository.findByIdForUpdate(SOURCE_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DESTINATION_ID)).thenReturn(Optional.of(destination));
        lenient().when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void completesTransferAndMovesFunds() {
        stubAccountsForTransfer();
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(
                User.restore(OWNER_ID, "Alice", "alice@example.com", "hash", Role.USER, Instant.now())));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer toPersist = invocation.getArgument(0);
            return Transfer.restore(UUID.randomUUID(), toPersist.getSourceAccountId(),
                    toPersist.getDestinationAccountId(), toPersist.getAmount(), toPersist.getStatus(),
                    toPersist.getIdempotencyKey(), Instant.now());
        });

        Transfer result = transferService.transfer(command(new BigDecimal("30.00"), null));

        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(source.getBalance()).isEqualByComparingTo("70.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("50.00");
        verify(eventPublisher).publish(any(TransferCompletedEvent.class));
    }

    @Test
    void rejectsTransferWhenBalanceIsInsufficient() {
        stubAccountsForTransfer();

        assertThatThrownBy(() -> transferService.transfer(command(new BigDecimal("150.00"), null)))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void rejectsTransferToSameAccount() {
        TransferCommand sameAccount = new TransferCommand(
                new Requester(OWNER_ID, false), SOURCE_ID, SOURCE_ID, new BigDecimal("10.00"), null);

        assertThatThrownBy(() -> transferService.transfer(sameAccount))
                .isInstanceOf(InvalidTransferException.class);
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> transferService.transfer(command(new BigDecimal("0.00"), null)))
                .isInstanceOf(InvalidTransferException.class);
    }

    @Test
    void rejectsOversizedIdempotencyKey() {
        String oversizedKey = "k".repeat(81);

        assertThatThrownBy(() -> transferService.transfer(command(new BigDecimal("10.00"), oversizedKey)))
                .isInstanceOf(InvalidTransferException.class);
    }

    @Test
    void rejectsTransferWhenRequesterDoesNotOwnSource() {
        Account foreignSource = Account.restore(SOURCE_ID, OTHER_OWNER_ID, "Source",
                new BigDecimal("100.00"), true, Instant.now());
        when(accountRepository.findByIdForUpdate(SOURCE_ID)).thenReturn(Optional.of(foreignSource));
        when(accountRepository.findByIdForUpdate(DESTINATION_ID)).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> transferService.transfer(command(new BigDecimal("10.00"), null)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void rejectsTransferWhenAccountIsInactive() {
        destination = Account.restore(DESTINATION_ID, OTHER_OWNER_ID, "Destination",
                new BigDecimal("20.00"), false, Instant.now());
        when(accountRepository.findByIdForUpdate(SOURCE_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DESTINATION_ID)).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> transferService.transfer(command(new BigDecimal("10.00"), null)))
                .isInstanceOf(InactiveAccountException.class);
    }

    @Test
    void returnsExistingTransferOnIdempotentReplay() {
        Transfer existing = Transfer.restore(UUID.randomUUID(), SOURCE_ID, DESTINATION_ID,
                new BigDecimal("30.00"), TransferStatus.COMPLETED, "key-1", Instant.now());
        lenient().when(transferRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

        Transfer result = transferService.transfer(command(new BigDecimal("30.00"), "key-1"));

        assertThat(result).isSameAs(existing);
        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(eventPublisher, never()).publish(any());
    }
}
