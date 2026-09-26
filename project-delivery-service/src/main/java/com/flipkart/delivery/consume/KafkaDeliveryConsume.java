package com.flipkart.delivery.consume;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flipkart.delivery.response.kafka.KafkaPaymentResponse;
import com.flipkart.delivery.service.DeliveryService;

import tools.jackson.databind.ObjectMapper;

@Service
public class KafkaDeliveryConsume
{

	@Autowired
	DeliveryService deliveryService;

	@KafkaListener(topics = "payment-success", groupId = "delivery-service-group")
	public void consumeMessage(ConsumerRecord<String, String> record)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		KafkaPaymentResponse response = objectMapper.readValue(record.value(), KafkaPaymentResponse.class);

		deliveryService.createDelivery(response);
	}
}
