package org.example.customerservice.repository;

import org.example.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Boolean existsByTcNo(String tcNo);
    Boolean existsByEmail(String email);

    Optional<Customer> findByTcNo(String tcNo);
    Optional<Customer> findByEmail(String email);
}
