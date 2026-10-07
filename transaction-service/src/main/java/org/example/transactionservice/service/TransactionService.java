package org.example.transactionservice.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transactionservice.client.AccountClientProxy;
import org.example.transactionservice.exception.AccountServiceUnavailableException;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;
import org.example.transactionservice.dto.*;
import org.example.transactionservice.entity.Transaction;
import org.example.transactionservice.enums.OperationType;
import org.example.transactionservice.enums.TransactionStatus;
import org.example.transactionservice.exception.BaseException;
import org.example.transactionservice.exception.MessageType;
import org.example.transactionservice.mapper.TransactionMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClientProxy accountClientProxy;
    private final TransactionMapper transactionMapper;

    @Transactional(noRollbackFor = BaseException.class)
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction from {} to {}, amount: {}",
                request.senderIban(), request.receiverIban(), request.amount());
        if (request.senderIban().equals(request.receiverIban())) {
            throw new BaseException(MessageType.SAME_ACCOUNT);
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new BaseException(MessageType.INVALID_AMOUNT);
        }

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setStatus(TransactionStatus.PENDING);
        Transaction savedTransaction = transactionRepository.save(transaction);

        try {
            AccountResponse sender = accountClientProxy.getAccountByIban(request.senderIban());
            AccountResponse receiver = accountClientProxy.getAccountByIban(request.receiverIban());
            if (!"ACTIVE".equals(sender.status()) || !"ACTIVE".equals(receiver.status())) {
                throw new BaseException(MessageType.ACCOUNT_NOT_ACTIVE);
            }
            if (!sender.currency().equals(request.currency())
                    || !receiver.currency().equals(request.currency())) {
                throw new BaseException(MessageType.CURRENCY_MISMATCH);
            }
            if (sender.balance().compareTo(request.amount()) < 0) {
                throw new BaseException(MessageType.INSUFFICIENT_BALANCE);
            }

            // Remote balance changes are not rolled back by this local transaction.
            accountClientProxy.updateBalance(request.senderIban(),
                    new UpdateBalanceRequest(request.amount(), OperationType.WITHDRAW));
            accountClientProxy.updateBalance(request.receiverIban(),
                    new UpdateBalanceRequest(request.amount(), OperationType.DEPOSIT));
        } catch (BaseException exception) {
            markFailed(savedTransaction, exception);
            throw exception;
        } catch (FeignException exception) {
            markFailed(savedTransaction, exception);
            throw new BaseException(exception.status() == 404
                    ? MessageType.ACCOUNT_NOT_FOUND : MessageType.ACCOUNT_SERVICE_ERROR, exception);
        } catch (CallNotPermittedException exception) {
            markFailed(savedTransaction, exception);
            throw new AccountServiceUnavailableException(exception);
        } catch (Exception exception) {
            markFailed(savedTransaction, exception);
            throw new BaseException(MessageType.TRANSACTION_FAILED, exception);
        }

        savedTransaction.setStatus(TransactionStatus.COMPLETED);
        Transaction completed = transactionRepository.save(savedTransaction);
        log.info("Transaction completed: {}", completed.getId());
        return transactionMapper.toResponse(completed);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByIban(String iban) {
        return transactionMapper.toDtoList(
                transactionRepository.findBySenderIbanOrReceiverIban(iban, iban));
    }

    private void markFailed(Transaction transaction, Exception exception) {
        log.error("Transaction failed: {}", transaction.getId(), exception);
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);
    }
}
