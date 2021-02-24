package com.binance.trader.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.ProfitLoss;
import com.binance.api.client.domain.account.Trade;

public class Analyzer {

	private BinanceApiRestClient client;

	public Analyzer() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
				"<<your API key>>",
				"<<your secret key>>");
		client = factory.newRestClient();
	}

	public static void main(String[] args) {

		Analyzer a = new Analyzer();
		String[] symbols = {"BNBBUSD", "BNBUSDT", "BTCBUSD", "BTCUSDT",
				"ETHBUSD", "LTCBUSD", "ETCBUSD", "UNIBUSD", "DOGEBUSD",
				"LINKBUSD", "SFPBUSD", "RSRBUSD", "ADABUSD", "COMPBUSD",
				"AAVEBUSD", "VETBUSD", "DOTBUSD", "DATABUSD", "ATOMBUSD",
				"NEARBUSD", "BATBUSD", "BAKEBUSD", "CREAMBUSD", "FRONTBUSD",
				"BTTBUSD", "CAKEBUSD", "ACMBUSD", "LITBUSD", "DODOBUSD"};
		System.out.printf("%-10s%12s%12s%12s%12s%12s%15s\n",
				"Symbol",
				"MarketPrice",
				"AvgPrice",
				"CurrentCost",
				"CurrentQty",
				"RealizedPL",
				"UnrealizedPL");
		List<Map<String, Double>> allStats = new ArrayList<>();
		for (String symbol : symbols) {
			List<Trade> myTrades = a.getTrades(symbol);
			Map<String, Double> stats = a.analyze(symbol, myTrades);
			allStats.add(stats);
			System.out.printf("%-10s%12.4f%12.4f%12.4f%12.2f%12.2f%15.2f\n",
					symbol,
					stats.get("MarketPrice"),
					stats.get("SmartAveragePrice"),
					stats.get("CurrentCost"),
					stats.get("CurrentQuantity"),
					stats.get("RealizedProfitLoss"),
					stats.get("UnrealizedProfitLoss"));
		}
		System.out.println();
		System.out.printf("Total Cost: %.4f\n", allStats.stream()
				.map(s -> s.get("CurrentCost")).reduce(0.0, Double::sum));
		System.out.printf("Total Realized P/L: %.4f\n", allStats.stream()
				.map(s -> s.get("RealizedProfitLoss"))
				.reduce(0.0, Double::sum));
		System.out.printf("Total Unrealized P/L: %.4f\n", allStats.stream()
				.map(s -> s.get("UnrealizedProfitLoss"))
				.reduce(0.0, Double::sum));
	}

	public double getMarketPrice(String symbol) {
		return Double.parseDouble(
				client.getAggTrades(symbol.toUpperCase()).get(0).getPrice());
	}

	public List<Trade> getTrades(String symbol) {
		return client.getMyTrades(symbol.toUpperCase());
	}

	public Map<String, Double> analyze(String symbol, List<Trade> trades) {

		ProfitLoss pl = getPl(symbol, trades);
		Map<String, Double> result = new HashMap<>();
		result.put("CurrentCost", pl.getCurrentCost());
		result.put("CurrentQuantity", pl.getQty());
		result.put("SmartAveragePrice", pl.getCurrentAvgPrice());
		result.put("MarketPrice", getMarketPrice(symbol));
		result.put("RealizedProfitLoss", pl.getRealizedPl());
		result.put("UnrealizedProfitLoss",
				(getMarketPrice(symbol) - pl.getCurrentAvgPrice())
						* pl.getQty());
		return result;
	}

	private ProfitLoss getPl(String symbol, List<Trade> trades) {
		ProfitLoss pl = new ProfitLoss();
		double currentCost = 0;
		double currentQty = 0;
		double realizedPl = 0;

		for (Trade trade : trades) {
			Double qty = Double.parseDouble(trade.getQty());
			if (trade.isBuyer()) {
				currentCost += Double.parseDouble(trade.getPrice()) * qty;
				currentQty += qty;
			} else {
				double tmpAvgPrice = currentQty == 0
						? 0
						: currentCost / currentQty;
				currentCost -= tmpAvgPrice * qty;
				realizedPl += (Double.parseDouble(trade.getPrice())
						- tmpAvgPrice) * qty;
				currentQty -= qty;
			}
		}

		double currentAvgPrice = currentQty == 0 ? 0 : currentCost / currentQty;
		double unrealizedPl = currentQty == 0
				? 0
				: (getMarketPrice(symbol) - currentAvgPrice) * currentQty;
		// currentCost = currentQty == 0 ? 0 : currentCost;

		pl.setQty(currentQty);
		pl.setCurrentCost(currentCost);
		pl.setCurrentAvgPrice(currentAvgPrice);
		pl.setRealizedPl(realizedPl);
		pl.setUnrealizedPl(unrealizedPl);
		return pl;
	}
}
