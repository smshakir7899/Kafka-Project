package com.flipkart.payment.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.flipkart.payment.entity.PaymentEntity;

@Repository
public interface PaymentRepository extends CrudRepository<PaymentEntity, Integer>
{
	
	boolean existsBySourceId(String sourceId);

}
