package com.binance.api.client.domain.account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfitLoss {

	private String symbol;

	private String sellCurrency;

	private double marketPrice;

	private double qty;

	private double cost;

	private double avgPrice;

	private double realizedPl;

	private double unrealizedPl;

	private double unrealizedPlRatio;
}
