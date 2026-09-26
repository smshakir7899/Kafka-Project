package com.flipkart.notification.service;

import org.springframework.stereotype.Service;

import com.flipkart.notification.response.kafka.KafkaNotificationResponse;

@Service
public class NotificationService {

	public void sendNotification(KafkaNotificationResponse response) {
//		System.out.println("Notification received: " + response.getEventType());
//
//		if ("PAYMENT_SUCCESS".equals(response.getEventType())) {
//			throw new RuntimeException("Testing DLT");
//		}

		String message;

		switch (response.getEventType()) {
		case "ORDER_CREATED":
			message = "Your order #" + response.getOrderId() + " has been placed successfully.";
			break;
		case "PAYMENT_SUCCESS":
			message = "Your payment of Rs." + response.getAmount() + " for order #" + response.getOrderId()
					+ " was successful.";
			break;
		case "PAYMENT_FAILED":
			message = "Your payment for order #" + response.getOrderId() + " has failed. Reason: "
					+ response.getReason();
			break;
		case "DELIVERY_CREATED":
			message = "Your order #" + response.getOrderId() + " is out for delivery. Tracking number: "
					+ response.getTrackingNumber();
			break;
		default:
			message = "Update on order #" + response.getOrderId() + ": " + response.getEventType();
		}

		System.out.println("SMS SENT");
		System.out.println("Customer: " + response.getCustomerId());
		System.out.println("Order: " + response.getOrderId());
		System.out.println(message);

	}

}