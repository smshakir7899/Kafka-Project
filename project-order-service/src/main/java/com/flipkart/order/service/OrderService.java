package com.flipkart.order.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.flipkart.order.entity.OrderEntity;
import com.flipkart.order.repository.OrderRepository;
import com.flipkart.order.request.OrderRequest;
import com.flipkart.order.response.OrderResponse;
import com.flipkart.order.response.kafka.KafkaResponse;
import com.flipkart.order.service.kafka.KafkaService;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderService
{
	@Autowired
	OrderRepository orderRepository;

	@Autowired
	KafkaService kafkaService;

	@Value("${app.kafka.topic.order-created}")
	private String orderCreatedTopic;
	// topic name comes from app properties not hard coding

	public OrderResponse createOrder(OrderRequest orderRequest)
	{
		OrderEntity orderEntity = new OrderEntity();

		System.out.println("OrderService.createOrder()::::::::::::::::::::::START");
		orderEntity.setCustomerId(orderRequest.getCustomerId());
		orderEntity.setCustomerName(orderRequest.getCustomerName());
		orderEntity.setProductId(orderRequest.getProductId());
		orderEntity.setProductName(orderRequest.getProductName());
		orderEntity.setQuantity(orderRequest.getQuantity());
		orderEntity.setStatus("CREATED");
		orderEntity.setAmount(orderRequest.getAmount());
		orderEntity.setDeliveryAddress(orderRequest.getDeliveryAddress());
//		orderEntity.setOrderId(generateOrderId());

		OrderEntity responseEntity = orderRepository.save(orderEntity);

		OrderResponse orderResponse = new OrderResponse();

		orderResponse.setOrderId(responseEntity.getOrderId());
		orderResponse.setStatus(responseEntity.getStatus());

		KafkaResponse kafkaResponse = new KafkaResponse();
		Random random = new Random();

		int eventId = 10000 + random.nextInt(90000);
		kafkaResponse.setEventId("EVT-" + eventId);
//		kafkaResponse.setEventId("EVT-" + 83838); This is using for idempotency
		kafkaResponse.setEventType("ORDER_CREATED");
		kafkaResponse.setOrderId(responseEntity.getOrderId());
		kafkaResponse.setCustomerId(responseEntity.getCustomerId());
		kafkaResponse.setAmount(responseEntity.getAmount());
		kafkaResponse.setDeliveryAddress(responseEntity.getDeliveryAddress());
		kafkaResponse.setEventTime(LocalDateTime.now().toString());

		if (orderRequest.getCustomerId() > 0)
		{
			String data = objToJson(kafkaResponse);
			String key = String.valueOf(responseEntity.getOrderId());
			kafkaService.storingOrderMessage(orderCreatedTopic, key, data);
		}
		System.out.println("OrderService.createOrder()::::::::::::::::::::::END");

		return orderResponse;
	}

//	public int generateOrderId()
//	{
//		Random random = new Random();
//		int id = 1000 + random.nextInt(9000);
//		return id;
//	}

	private String objToJson(KafkaResponse response)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(response);
		return json;
	}
}
