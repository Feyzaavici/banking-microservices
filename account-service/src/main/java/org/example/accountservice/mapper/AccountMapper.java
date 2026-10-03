package org.example.accountservice.mapper;

import org.example.accountservice.dto.AccountResponse;
import org.example.accountservice.dto.CreateAccountRequest;
import org.example.accountservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper {

    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "iban", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toEntity(CreateAccountRequest request);

    AccountResponse toResponse(Account account);
    List<AccountResponse> toDtoList(List<Account>accounts);
}
