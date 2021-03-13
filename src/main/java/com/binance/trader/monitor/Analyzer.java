package com.binance.trader.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.DepositHistory;
import com.binance.api.client.domain.account.ProfitLoss;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.market.AggTrade;

public class Analyzer {

	private BinanceApiRestClient client;

	public Analyzer() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
				"<<your API key>>",		
				"<<your secret key>>");
		client = factory.newRestClient();
	}

	public static void main(String[] args) throws Exception {	
		String[] symbols = {"BNBBUSD","BTCBUSD","ETHBUSD","ADABUSD", "UNIBUSD","LINKBUSD","CHZUSDT","CHZBUSD","DOGEBUSD","BAKEBUSD","ETCBUSD","CREAMBUSD","BATBUSD","LTCBUSD", 
				"COMPBUSD","VETBUSD","NEARBUSD"};
		Analyzer a = new Analyzer();
		while (true) {		
			List<ProfitLoss> allPl = a.getAllPl(symbols);
			Map<String, Double> statement = a.analyze(allPl);
			a.printPlStatement(allPl, statement);
		    Thread.sleep(900000); //15 mins				
	    }
	}

	public double getMarketPrice(String symbol) {
		List<AggTrade> aggTrades = client.getAggTrades(symbol.toUpperCase());
		return Double.parseDouble(
				aggTrades.get(aggTrades.size() - 1).getPrice());
	}

	public List<Trade> getTrades(String symbol) {
		return client.getMyTrades(symbol.toUpperCase());
	}
	
	public DepositHistory getDepositHistory(String asset) {
		return client.getDepositHistory(asset.toUpperCase());
	}

	public  Map<String, Double> analyze(List<ProfitLoss> allPl) {

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
	
	public List<ProfitLoss> getAllPl(String[] symbols ) {
		
		List<ProfitLoss> allPl = new ArrayList<>();

		for (String symbol : symbols) {
			List<Trade> myTrades = getTrades(symbol);
			ProfitLoss pl = getPl(symbol, myTrades);
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
				if (currentQty < qty) {				
					realizedPl += Double.parseDouble(trade.getPrice()) * qty - currentCost;
					currentCost = 0;
					currentQty = 0;
				} else {
					currentCost -= tmpAvgPrice * qty;
					realizedPl += (Double.parseDouble(trade.getPrice())
							- tmpAvgPrice) * qty;
					currentQty -= qty;
				}
			}
		}
		
		double marketPrice = getMarketPrice(symbol);
		double avgPrice = currentQty == 0 ? 0 : currentCost / currentQty;
		double unrealizedPl = currentQty == 0
				? 0
				: (marketPrice - avgPrice) * currentQty;
		double unrealizedPlRatio = currentQty == 0 ? 0 : (marketPrice - avgPrice) / avgPrice * 100;
		
		pl.setSymbol(symbol);
		pl.setMarketPrice(marketPrice);
		pl.setAvgPrice(avgPrice);
		pl.setCost(currentCost);
		pl.setQty(currentQty);
		pl.setRealizedPl(realizedPl);
		pl.setUnrealizedPl(unrealizedPl);
		pl.setUnrealizedPlRatio(unrealizedPlRatio);

		return pl;
	}
	
	private void printPlStatement(List<ProfitLoss> allPl , Map<String, Double> statement){
		
		System.out.println(LocalDateTime.now());	
		System.out.printf("%-10s%12s%12s%12s%12s%12s%15s%15s\n",
				"Symbol",
				"MarketPrice",
				"AvgPrice",
				"Cost",
				"Quantity",
				"RealizedPL",
				"UnrealizedPL",
				"UnrealizedPL%");

		for (ProfitLoss pl : allPl) {
			System.out.printf("%-10s%12.4f%12.4f%12.4f%12.4f%12.2f%15.2f%14.2f%-10s\n",
					pl.getSymbol(),
					pl.getMarketPrice(),
					pl.getAvgPrice(),
					pl.getCost(),
					pl.getQty(),
					pl.getRealizedPl(),
					pl.getUnrealizedPl(),
					pl.getUnrealizedPlRatio(),
					"%");
		}
		
		System.out.println();
		System.out.printf("Total Realized P/L: %.4f\n", statement.get("TotalRealizedProfitLoss"));
		System.out.printf("Total Cost: %.4f\n", statement.get("TotalCost"));
		System.out.printf("Total Unrealized P/L: %.4f\n", statement.get("TotalUnrealizedProfitLoss"));
		System.out.printf("Total Capital Value: %.4f\n", statement.get("TotalCapitalValue"));
		System.out.println();
	}
}
