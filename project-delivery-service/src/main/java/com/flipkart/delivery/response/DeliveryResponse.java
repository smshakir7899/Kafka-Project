package com.flipkart.delivery.response;

public class DeliveryResponse
{

	private String eventId;
	private String eventType;
	private int orderId;
	private int customerId;
	private String trackingNumber;
	private String deliveryAddress;
	private String deliveryStatus;
	private String eventTime;

	public String getEventId()
	{
		return eventId;
	}

	public void setEventId(String eventId)
	{
		this.eventId = eventId;
	}

	public String getEventType()
	{
		return eventType;
	}

	public void setEventType(String eventType)
	{
		this.eventType = eventType;
	}

	public int getOrderId()
	{
		return orderId;
	}

	public void setOrderId(int orderId)
	{
		this.orderId = orderId;
	}

	public int getCustomerId()
	{
		return customerId;
	}

	public void setCustomerId(int customerId)
	{
		this.customerId = customerId;
	}

	public String getTrackingNumber()
	{
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber)
	{
		this.trackingNumber = trackingNumber;
	}

	public String getDeliveryAddress()
	{
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress)
	{
		this.deliveryAddress = deliveryAddress;
	}

	public String getDeliveryStatus()
	{
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus)
	{
		this.deliveryStatus = deliveryStatus;
	}

	public String getEventTime()
	{
		return eventTime;
	}

	public void setEventTime(String eventTime)
	{
		this.eventTime = eventTime;
	}

}
