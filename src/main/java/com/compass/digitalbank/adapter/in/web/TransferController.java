package com.compass.digitalbank.adapter.in.web;

import com.compass.digitalbank.adapter.in.web.dto.PageResponse;
import com.compass.digitalbank.adapter.in.web.dto.TransactionResponse;
import com.compass.digitalbank.adapter.in.web.dto.TransferRequest;
import com.compass.digitalbank.adapter.in.web.dto.TransferResponse;
import com.compass.digitalbank.adapter.in.web.security.AppUserPrincipal;
import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;
import com.compass.digitalbank.domain.port.in.TransferUseCase;
import com.compass.digitalbank.domain.port.in.TransferUseCase.TransferCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Fund transfers and account statements")
public class TransferController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final TransferUseCase transferUseCase;

    @PostMapping(ApiPaths.TRANSFERS)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer funds between two accounts")
    public TransferResponse transfer(@AuthenticationPrincipal AppUserPrincipal principal,
                                     @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
                                     @Valid @RequestBody TransferRequest request) {
        Transfer transfer = transferUseCase.transfer(new TransferCommand(
                principal.toRequester(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                idempotencyKey));
        return TransferResponse.from(transfer);
    }

    @GetMapping(ApiPaths.ACCOUNTS + "/{accountId}/transactions")
    @Operation(summary = "List the financial movements of an account")
    public PageResponse<TransactionResponse> listTransactions(@AuthenticationPrincipal AppUserPrincipal principal,
                                                              @PathVariable UUID accountId,
                                                              @RequestParam(required = false) Integer page,
                                                              @RequestParam(required = false) Integer size) {
        PageResult<Transfer> result = transferUseCase.listAccountTransactions(
                accountId, principal.toRequester(), PageQuery.of(page, size));
        return PageResponse.from(result.map(transfer -> TransactionResponse.from(transfer, accountId)));
    }
}
