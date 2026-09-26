package com.flipkart.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flipkart.order.request.OrderRequest;
import com.flipkart.order.response.OrderResponse;
import com.flipkart.order.service.OrderService;

@RestController
//@RequestMapping("order/") change to match the project requirement i.e. POST/ orders
public class OrderController
{
	@Autowired
	OrderService orderService;

	@PostMapping("/orders")
	public OrderResponse placeOrder(@RequestBody OrderRequest orderRequest)
	{
		System.out.println("OrderController.placeOrder()::::::::::::STARTED");
		OrderResponse response = orderService.createOrder(orderRequest);
		System.out.println("OrderController.placeOrder()::::::::::::ENDED");

		return response;
	}
}
