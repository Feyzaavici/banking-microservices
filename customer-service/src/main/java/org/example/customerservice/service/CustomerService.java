package org.example.customerservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CreateCustomerRequest;
import org.example.customerservice.dto.CustomerResponse;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.enums.CustomerStatus;
import org.example.customerservice.exception.BaseException;
import org.example.customerservice.exception.MessageType;
import org.example.customerservice.mapper.CustomerMapper;
import org.example.customerservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.customerservice.dto.AuthenticateRequest;
import org.example.customerservice.dto.CustomerAuthResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request){
        if(customerRepository.existsByTcNo(request.tcNo())){
            throw new BaseException(MessageType.TC_NO_ALREADY_EXISTS);
        }
        if(customerRepository.existsByEmail(request.email())){
            throw  new BaseException(MessageType.EMAIl_ALREADY_EXISTS);
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.password()));
        customer.setStatus(CustomerStatus.ACTIVE);
        Customer savedCustomer = customerRepository.save(customer);

         return customerMapper.toResponse(savedCustomer);
    }

    public CustomerResponse getCustomer(String tcNo, Long requesterCustomerId){
        log.info("Get customer with tcNo: {}", tcNo);
        Customer customer = customerRepository.findByTcNo(tcNo).orElseThrow(()->new BaseException(MessageType.CUSTOMER_NOT_FOUND));

        requireOwner(customer.getId(), requesterCustomerId);

        return customerMapper.toResponse(customer);
    }

    public CustomerResponse getCustomer(Long id, Long requesterCustomerId){
        requireOwner(id, requesterCustomerId);
        log.info("Get customer with id: {}", id);

        Customer customer = customerRepository.findById(id).orElseThrow(()-> new BaseException(MessageType.CUSTOMER_NOT_FOUND));

        return customerMapper.toResponse(customer);
    }

    public CustomerAuthResponse authenticate(AuthenticateRequest request) {
        Customer customer = customerRepository.findByTcNo(request.tcNo())
                .orElseThrow(() -> new BaseException(MessageType.INVALID_CREDENTIALS));
        if (customer.getPassword() == null
                || !passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw new BaseException(MessageType.INVALID_CREDENTIALS);
        }
        return new CustomerAuthResponse(customer.getId(), customer.getTcNo());
    }

    private void requireOwner(Long owner, Long requester) {
        if (requester == null || !requester.equals(owner)) {
            throw new BaseException(MessageType.ACCESS_DENIED);
        }
    }


}
