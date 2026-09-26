package com.flipkart.payment.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
//import com.flipkart.payment.consume.KafkaPaymentConsumer;
import com.flipkart.payment.entity.PaymentEntity;
import com.flipkart.payment.repository.PaymentRepository;
import com.flipkart.payment.response.PaymentResponse;
import com.flipkart.payment.response.kafka.KafkaOrderResponse;
import com.flipkart.payment.service.kafka.KafkaService;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentService
{

//	@Autowired
//	private KafkaPaymentConsumer kafkaPaymentConsumer;

	@Autowired
	KafkaService kafkaService;

	@Autowired
	PaymentRepository paymentRepository;

//	@Autowired
//	PaymentEntity paymentEntity;

	@Value("${app.kafka.topic.payment-success}")
	private String paymentSuccessTopic;

	@Value("${app.kafka.topic.payment-failed}")
	private String paymentFailedTopic;

//	PaymentService(KafkaPaymentConsumer kafkaPaymentConsumer)
//	{
//		this.kafkaPaymentConsumer = kafkaPaymentConsumer;
//	}

	public PaymentResponse processPayment(KafkaOrderResponse response)
	{
		if (paymentRepository.existsBySourceId(response.getEventId()))
		{
			System.out.println("Duplicate event recieved" + response.getEventId());
			return null;
		}
//		paymentEntity.setSourceId(response.getEventId());
		PaymentResponse paymentResponse = new PaymentResponse();

		Random random = new Random();

		int id = 10000 + random.nextInt(90000);
		paymentResponse.setEventId("PAY-" + id);

		paymentResponse.setOrderId(response.getOrderId());
		paymentResponse.setCustomerId(response.getCustomerId());
		paymentResponse.setAmount(response.getAmount());

		int payId = 10000 + random.nextInt(90000);
		paymentResponse.setPaymentId("TXN-" + payId);

		
		// Generates a random number from 0 to 99.
		// Values below 80 represent successful payments, while 80–99 represent failures.
		boolean isSuccess = random.nextInt(100) < 80;
		String topicToUse;

		if (isSuccess)
		{
			paymentResponse.setEventType("PAYMENT_SUCCESS");
			paymentResponse.setPaymentMethod("UPI");
			paymentResponse.setPaymentStatus("SUCCESS");

			topicToUse = paymentSuccessTopic;

		} else
		{
			paymentResponse.setEventType("PAYMENT_FAILED");
			paymentResponse.setPaymentStatus("FAILED");
			paymentResponse.setReason("INSUFFICIENT_FUNDS");

			topicToUse = paymentFailedTopic;

		}

		// we have to store the result of payment service

		PaymentEntity paymentEntity = new PaymentEntity();

		paymentEntity.setEventId(paymentResponse.getEventId());
		paymentEntity.setSourceId(response.getEventId());
		paymentEntity.setOrderId(paymentResponse.getOrderId());
		paymentEntity.setCustomerId(paymentResponse.getCustomerId());
		paymentEntity.setAmount(paymentResponse.getAmount());
		paymentEntity.setPaymentId(paymentResponse.getPaymentId());
		paymentEntity.setPaymentMethod(paymentResponse.getPaymentMethod());
		paymentEntity.setPaymentStatus(paymentResponse.getPaymentStatus());
		paymentEntity.setReason(paymentResponse.getReason());
		System.out.println("sms :::::::" + paymentEntity.getSourceId());
		paymentRepository.save(paymentEntity);

		String data = objToJson(paymentResponse);
		String key = String.valueOf(paymentResponse.getOrderId());

		kafkaService.storingPaymentMessage(topicToUse, key, data);

		return paymentResponse;
	}

	private String objToJson(PaymentResponse paymentResponse)
	{
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(paymentResponse);
		return json;
	}
}
