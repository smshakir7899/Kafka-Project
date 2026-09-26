package com.flipkart.delivery.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.flipkart.delivery.entity.DeliveryEntity;

@Repository
public interface DeliveryRepository extends CrudRepository<DeliveryEntity, Integer>
{

}
