package com.flipkart.order.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService
{
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	public void storingOrderMessage(String topic, String key, String message)
	{
		System.out.println("KafkaService.storingOrderMessage():::::::::::::::;START");
//		String key = "ORD01";
		// key is now passed in by the caller instead of being a fixed constant.
		// We always pass the orderId as the key so that:
		// 1) all events for the same order go to the same partition (keeps order)
		// 2) we can actually see partitioning/ordering behavior when the topic has more
		// than 1 partition

		System.out.println(message);
		kafkaTemplate.send(topic, key, message);
		System.out.println("KafkaService.storingOrderMessage():::::::::::::::;END");

	}
}
