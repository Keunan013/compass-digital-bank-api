package com.compass.digitalbank.adapter.in.web;

import com.compass.digitalbank.adapter.in.web.dto.AccountResponse;
import com.compass.digitalbank.adapter.in.web.dto.CreateAccountRequest;
import com.compass.digitalbank.adapter.in.web.dto.PageResponse;
import com.compass.digitalbank.adapter.in.web.security.AppUserPrincipal;
import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.pagination.PageQuery;
import com.compass.digitalbank.domain.pagination.PageResult;
import com.compass.digitalbank.domain.port.in.AccountUseCase;
import com.compass.digitalbank.domain.port.in.AccountUseCase.CreateAccountCommand;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.ACCOUNTS)
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management")
public class AccountController {

    private final AccountUseCase accountUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a new account owned by the authenticated user")
    public AccountResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @Valid @RequestBody CreateAccountRequest request) {
        Account account = accountUseCase.create(
                new CreateAccountCommand(principal.id(), request.name(), request.initialBalance()));
        return AccountResponse.from(account);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get an account owned by the authenticated user")
    public AccountResponse getById(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @PathVariable UUID accountId) {
        return AccountResponse.from(accountUseCase.getById(accountId, principal.toRequester()));
    }

    @GetMapping
    @Operation(summary = "List accounts owned by the authenticated user")
    public PageResponse<AccountResponse> list(@AuthenticationPrincipal AppUserPrincipal principal,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size) {
        PageResult<Account> result = accountUseCase.listOwnedBy(principal.id(), PageQuery.of(page, size));
        return PageResponse.from(result.map(AccountResponse::from));
    }
}
