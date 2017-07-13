package com.hundsun.hscar.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 订单表
 * 
 * @author zhangmm
 * @email phoenix122411@126.com
 * @date 2017-07-13
 */
public class CarpoolingOrdersEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long orderId; // 
	
	private Long routeId; // 
	
	private Integer orderType; // 1:即时订单、2:预约订单
	
	private Double price; // 价格
	
	private Double reward; // 奖励



	/**
	 * 设置：
	 */
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	/**
	 * 获取：
	 */
	public Long getOrderId() {
		return orderId;
	}

	/**
	 * 设置：
	 */
	public void setRouteId(Long routeId) {
		this.routeId = routeId;
	}
	/**
	 * 获取：
	 */
	public Long getRouteId() {
		return routeId;
	}

	/**
	 * 设置：1:即时订单、2:预约订单
	 */
	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}
	/**
	 * 获取：1:即时订单、2:预约订单
	 */
	public Integer getOrderType() {
		return orderType;
	}

	/**
	 * 设置：价格
	 */
	public void setPrice(Double price) {
		this.price = price;
	}
	/**
	 * 获取：价格
	 */
	public Double getPrice() {
		return price;
	}

	/**
	 * 设置：奖励
	 */
	public void setReward(Double reward) {
		this.reward = reward;
	}
	/**
	 * 获取：奖励
	 */
	public Double getReward() {
		return reward;
	}
}