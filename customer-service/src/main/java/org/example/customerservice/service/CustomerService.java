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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request){
        if(customerRepository.existsByTcNo(request.tcNo())){
            throw new BaseException(MessageType.TC_NO_ALREADY_EXISTS);
        }
        if(customerRepository.existsByEmail(request.email())){
            throw  new BaseException(MessageType.EMAIl_ALREADY_EXISTS);
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setStatus(CustomerStatus.ACTIVE);
        Customer savedCustomer = customerRepository.save(customer);

         return customerMapper.toResponse(savedCustomer);
    }

    public CustomerResponse getCustomer(String tcNo){
        log.info("Get customer with tcNo: {}", tcNo);
        Customer customer = customerRepository.findByTcNo(tcNo).orElseThrow(()->new BaseException(MessageType.CUSTOMER_NOT_FOUND));

        return customerMapper.toResponse(customer);
    }

    public CustomerResponse getCustomerById(Long id){
        log.info("Get customer with id: {}", id);

        Customer customer = customerRepository.findById(id).orElseThrow(()-> new BaseException(MessageType.CUSTOMER_NOT_FOUND));

        return customerMapper.toResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers(){
        log.info("Get all customers");

        List<Customer> customers = customerRepository.findAll();

        return customerMapper.toDtoList(customers);
    }


}
