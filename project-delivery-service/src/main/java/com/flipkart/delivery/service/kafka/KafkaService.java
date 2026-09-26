package com.flipkart.delivery.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService
{
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	public void storingDeliveryMessage(String topic, String key, String message)
	{

		kafkaTemplate.send(topic, key, message);
	}
}