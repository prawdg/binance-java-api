package com.binance.trader.monitor;

import java.time.LocalDateTime;
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

	public static void main(String[] args) throws Exception {	
		String[] symbols = {"BNBBUSD","BTCBUSD", "ETHBUSD", "ADABUSD","ETCBUSD", "UNIBUSD", "DOGEBUSD", "LINKBUSD", 
				"COMPBUSD", "VETBUSD",	"NEARBUSD",	"BATBUSD", "BAKEBUSD", "CREAMBUSD","LTCBUSD"};
		List<ProfitLoss> allPl = getAllPl(symbols);
		Map<String, Double> statement = analyze(allPl);
		while (true) {			   
			printPlStatement(allPl, statement);
		    Thread.sleep(900000);				
	    }
	}

	public double getMarketPrice(String symbol) {
		return Double.parseDouble(
				client.getAggTrades(symbol.toUpperCase()).get(0).getPrice());
	}

	public List<Trade> getTrades(String symbol) {
		return client.getMyTrades(symbol.toUpperCase());
	}

	public static Map<String, Double> analyze(List<ProfitLoss> allPl) {

		Map<String, Double> result = new HashMap<>();
		result.put("TotalRealizedProfitLoss", allPl.stream()
				.map(s -> s.getRealizedPl()).reduce(0.0, Double::sum));
		result.put("TotalCost", allPl.stream()
				.map(s -> s.getCost()).reduce(0.0, Double::sum));
		result.put("TotalUnrealizedProfitLoss",allPl.stream()
				.map(s -> s.getUnrealizedPl()).reduce(0.0, Double::sum));
		result.put("TotalCapitalValue",result.get("TotalCost") + result.get("TotalUnrealizedProfitLoss"));
		return result;
	}
	
	public static List<ProfitLoss> getAllPl(String[] symbols ) {
		
		List<ProfitLoss> allPl = new ArrayList<>();
		Analyzer a = new Analyzer();

		for (String symbol : symbols) {
			List<Trade> myTrades = a.getTrades(symbol);
			ProfitLoss pl = a.getPl(symbol, myTrades);
			allPl.add(pl);}
		
		return allPl;
	}

	public ProfitLoss getPl(String symbol, List<Trade> trades) {
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

		double avgPrice = currentQty == 0 ? 0 : currentCost / currentQty;
		double unrealizedPl = currentQty == 0
				? 0
				: (getMarketPrice(symbol) - avgPrice) * currentQty;
		
		pl.setSymbol(symbol);
		pl.setMarketPrice(getMarketPrice(symbol));
		pl.setAvgPrice(avgPrice);
		pl.setQty(currentQty);
		pl.setCost(currentCost);
		pl.setUnrealizedPl(unrealizedPl);
		pl.setRealizedPl(realizedPl);
		return pl;
	}
	
	private static void printPlStatement(List<ProfitLoss> allPl , Map<String, Double> statement){
		
		System.out.println(LocalDateTime.now());	
		System.out.printf("%-10s%12s%12s%12s%12s%12s%15s\n",
				"Symbol",
				"MarketPrice",
				"AvgPrice",
				"Cost",
				"Qty",
				"RealizedPL",
				"UnrealizedPL");
		
		List<Map<String, Double>> allStats = new ArrayList<>();
		Analyzer a = new Analyzer();

		for (ProfitLoss pl : allPl) {
			System.out.printf("%-10s%12.4f%12.4f%12.4f%12.4f%12.2f%15.2f\n",
					pl.getSymbol(),
					pl.getMarketPrice(),
					pl.getAvgPrice(),
					pl.getCost(),
					pl.getQty(),
					pl.getRealizedPl(),
					pl.getUnrealizedPl());
		}
		System.out.println();
		System.out.printf("Total Realized P/L: %.4f\n", statement.get("TotalRealizedProfitLoss"));
		System.out.printf("Total Cost: %.4f\n", statement.get("TotalCost"));
		System.out.printf("Total Unrealized P/L: %.4f\n", statement.get("TotalUnrealizedProfitLoss"));
		System.out.printf("Total Capital Value: %.4f\n", statement.get("TotalCapitalValue"));
		System.out.println();
	}
}
