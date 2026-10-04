package org.example.transactionservice.mapper;

import org.example.transactionservice.dto.CreateTransactionRequest;
import org.example.transactionservice.dto.TransactionResponse;
import org.example.transactionservice.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntity(CreateTransactionRequest request);

    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toDtoList(List<Transaction> transactions);
}
