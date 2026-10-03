package org.example.accountservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.AccountResponse;
import org.example.accountservice.dto.CreateAccountRequest;
import org.example.accountservice.dto.UpdateBalanceRequest;
import org.example.accountservice.entity.Account;
import org.example.accountservice.enums.AccountStatus;
import org.example.accountservice.enums.OperationType;
import org.example.accountservice.exception.BaseException;
import org.example.accountservice.exception.MessageType;
import org.example.accountservice.mapper.AccountMapper;
import org.example.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest accountRequest){
        log.info("Create account for customer: {}",accountRequest.customerId());

        String iban = generateIban();

        Account account = accountMapper.toEntity(accountRequest);
        Account savedAccount = accountRepository.save(account);

        log.info("Account created successfully with IBAN: {}",savedAccount.getIban());

        return accountMapper.toResponse(savedAccount);
    }

    public AccountResponse getAccountByIban(String iban){
        log.info("Get account by iban: {}", iban);
        Account account = accountRepository.findByIban(iban).orElseThrow(()->new BaseException(MessageType.ACCOUNT_NOT_FOUND));

        return accountMapper.toResponse(account);
    }

    public List<AccountResponse> getAccountByCustomerId(Long customerId){
        log.info("Get account by customerId: {} ",customerId);
        return accountMapper.toDtoList(accountRepository.findByCustomerId(customerId));

    }


    @Transactional
    public AccountResponse updateBalance(String iban, UpdateBalanceRequest request){
        log.info("Update balance for Iban: {}, Operation: {}", iban, request.operationTyp());

        Account account = accountRepository.findByIban(iban).orElseThrow(()->new BaseException(MessageType.ACCOUNT_NOT_FOUND));

        if (account.getStatus() != AccountStatus.ACTIVE){
            throw new BaseException(MessageType.ACCOUNT_NOT_ACTIVE);
        }

        if(request.operationTyp() == OperationType.DEPOSIT){
            account.setBalance(account.getBalance().add(request.amount()));
        }else {
            if(account.getBalance().compareTo(request.amount()) < 0){
                throw new BaseException(MessageType.INSUFFICIENT_BALANCE);
            }
            account.setBalance(account.getBalance().subtract(request.amount()));
        }

        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated successfully with new balance: {}", updatedAccount.getBalance());

        return accountMapper.toResponse(updatedAccount);

    }

    private  String generateIban(){
        String countryCode = "TR";
        String checkDigits = String.format("%82d",(new Random()).nextInt(100));
        String bankCode = "00001";
        String reserved = "0";
        String accountNumber = String.format("%016d", new Random().nextLong() & Long.MAX_VALUE).substring(0,16);

        return countryCode + checkDigits + bankCode + reserved + accountNumber;
    }
}
