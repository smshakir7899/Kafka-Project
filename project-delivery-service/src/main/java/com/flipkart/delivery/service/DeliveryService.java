package com.flipkart.delivery.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.flipkart.delivery.entity.DeliveryEntity;
import com.flipkart.delivery.repository.DeliveryRepository;
import com.flipkart.delivery.response.DeliveryResponse;
import com.flipkart.delivery.response.kafka.KafkaPaymentResponse;
import com.flipkart.delivery.service.kafka.KafkaService;

import tools.jackson.databind.ObjectMapper;

@Service
public class DeliveryService
{

	@Autowired
	KafkaService kafkaService;

	@Autowired
	DeliveryRepository deliveryRepository;

	@Value("${app.kafka.topic.delivery-created}")
	private String deliveryCreatedTopic;

	public DeliveryResponse createDelivery(KafkaPaymentResponse response)
	{

		DeliveryResponse deliveryResponse = new DeliveryResponse();

		Random random = new Random();

		int id = 10000 + random.nextInt(90000);
		deliveryResponse.setEventId("DEL-" + id);
		deliveryResponse.setEventType("DELIVERY_CREATED");
		deliveryResponse.setOrderId(response.getOrderId());
		deliveryResponse.setCustomerId(response.getCustomerId());
		deliveryResponse.setEventTime(LocalDateTime.now().toString());
		int num = 10000 + random.nextInt(90000);
		deliveryResponse.setTrackingNumber("TRK-" + num);

		deliveryResponse.setDeliveryAddress(response.getDeliveryAddress());

		if (num % 2 == 0)
		{
			deliveryResponse.setDeliveryStatus("CREATED");
		} else if (num % 3 == 0)
		{
			deliveryResponse.setDeliveryStatus("iN_TRANSIT");
		} else if (num % 4 == 0)
		{
			deliveryResponse.setDeliveryStatus("OUT_FOR_DELIVERY");
		} else if (num % 5 == 0)
		{
			deliveryResponse.setDeliveryStatus("DELIVERED");
		} else
		{
			deliveryResponse.setDeliveryStatus("CANCELLED");
		}

		DeliveryEntity deliveryEntity = new DeliveryEntity();

		deliveryEntity.setEventId(deliveryResponse.getEventId());
		deliveryEntity.setOrderId(deliveryResponse.getOrderId());
		deliveryEntity.setCustomerId(deliveryResponse.getCustomerId());
		deliveryEntity.setTrackingNumber(deliveryResponse.getTrackingNumber());
		deliveryEntity.setDeliveryAddress(deliveryResponse.getDeliveryAddress());
		deliveryEntity.setDeliveryStatus(deliveryResponse.getDeliveryStatus());

		deliveryRepository.save(deliveryEntity);

		String data = objToJson(deliveryResponse);
		String key = String.valueOf(deliveryResponse.getOrderId());
		kafkaService.storingDeliveryMessage(deliveryCreatedTopic, key, data);

		return deliveryResponse;
	}

	private String objToJson(DeliveryResponse successResponses)
	{
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(successResponses);
		return json;
	}
}
