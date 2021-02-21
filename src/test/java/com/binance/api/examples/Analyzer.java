package com.binance.api.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Trade;

public class Analyzer {

	private BinanceApiRestClient client;

	public Analyzer() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
				"wjZJjRsUVSdodJwTswXqfTgCMDIctZNK2G6BLj8zbkD6Qx2rSVKaKaksexOTPfgl",
				"e1UOvjUVdmKYPxuMhJs9DdIuFn15OesVo27pDIEc9WS9ty0kIkfuIlhtH5im7zAi");
		client = factory.newRestClient();
	}

	public static void main(String[] args) {

		Analyzer a = new Analyzer();
		String[] symbols = {"BTCBUSD", "BTCUSDT", "ETHBUSD", "LTCBUSD",
				"ETCBUSD", "BNBBUSD", "UNIBUSD", "DOGEBUSD", "LINKBUSD",
				"ADABUSD", "COMPBUSD", "AAVEBUSD", "VETBUSD", "DOTBUSD",
				"ATOMBUSD",	"NEARBUSD",	"BATBUSD", "RSRBUSD", "SFPBUSD",
				"BTTBUSD"};
		for (String symbol : symbols) {
			List<Trade> myTrades = a.getTrades(symbol);
			System.out.println(symbol);
			System.out.println(a.analyze(symbol, myTrades));
		}
	}

	public double getCurrentPrice(String symbol) {
		return Double.parseDouble(
				client.getAggTrades(symbol.toUpperCase()).get(0).getPrice());
	}

	public List<Trade> getTrades(String symbol) {
		return client.getMyTrades(symbol.toUpperCase());
	}

	public Map<String, Double> analyze(String symbol, List<Trade> trades) {
		double avgPrice = 0;
		double totalCost = 0;
		double totalQty = 0;
		for (Trade trade : trades) {
			Double qty = trade.isBuyer()
					? Double.parseDouble(trade.getQty())
					: Double.parseDouble(trade.getQty()) * -1;
			totalQty += qty;
			totalCost += Double.parseDouble(trade.getPrice()) * qty;
		}

		avgPrice = totalQty == 0 ? 0 : totalCost / totalQty;
		Map<String, Double> result = new HashMap<>();
		result.put("AveragePrice", avgPrice);
		result.put("TotalCost", totalCost);
		result.put("TotalQuantity", totalQty);

		return result;
	}
}
