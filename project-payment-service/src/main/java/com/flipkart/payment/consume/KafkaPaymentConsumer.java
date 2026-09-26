package com.flipkart.payment.consume;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flipkart.payment.response.kafka.KafkaOrderResponse;
import com.flipkart.payment.service.PaymentService;

import tools.jackson.databind.ObjectMapper;

@Service
public class KafkaPaymentConsumer
{
	@Autowired
	PaymentService paymentService;

	@KafkaListener(topics = "order-created", groupId = "payment-service-group")
	public void consumeMessage(ConsumerRecord<String, String> record)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		KafkaOrderResponse response = objectMapper.readValue(record.value(), KafkaOrderResponse.class);

		paymentService.processPayment(response);
	}

}
