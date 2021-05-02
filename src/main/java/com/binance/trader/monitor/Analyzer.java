package com.binance.trader.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.DepositHistory;
import com.binance.api.client.domain.account.ProfitLoss;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.market.AggTrade;
import com.binance.api.client.exception.BinanceApiException;

public class Analyzer {

	private BinanceApiRestClient client;

	public Analyzer(String apiKey, String secretKey) {
		BinanceApiClientFactory factory = BinanceApiClientFactory
				.newInstance(apiKey, secretKey);
		client = factory.newRestClient();
	}

	public double getMarketPrice(String symbol) {
		List<AggTrade> aggTrades = client.getAggTrades(symbol.toUpperCase());
		return Double
				.parseDouble(aggTrades.get(aggTrades.size() - 1).getPrice());
	}

	public List<Trade> getTrades(String symbol) {
		return client.getMyTrades(symbol.toUpperCase());
	}

	public DepositHistory getDepositHistory(String asset) {
		return client.getDepositHistory(asset.toUpperCase());
	}

	public List<ProfitLoss> getAllPl(String[] symbols) {

		List<ProfitLoss> allPl = new ArrayList<>();

		for (String symbol : symbols) {
			List<Trade> myTrades = getTrades(symbol);
			ProfitLoss pl = getPL(symbol, myTrades);
			allPl.add(pl);
		}

		return allPl;
	}

	public List<ProfitLoss> getAllPL(String[] buySymbols, String[] sellSymbols)
			throws InterruptedException {

		List<ProfitLoss> allPL = new ArrayList<>();

		int requests = 0;
		for (String buySymbol : buySymbols) {
			for (String sellSymbol : sellSymbols) {
				String symbol = StringUtils.join(buySymbol, sellSymbol);
				try {
					List<Trade> myTrades = getTrades(symbol);
					requests++;
					ProfitLoss pl = getPL(buySymbol, sellSymbol, myTrades);
					if (pl.getQty() > 0 || pl.getRealizedPl() > 0) {
						allPL.add(pl);
					}
				} catch (BinanceApiException e) {
					// Invalid symbol exception check
					if (e.getError() != null
							&& e.getError().getCode() == -1121) {
						continue;
					} else {
						throw e;
					}
				}
				if (requests == 80) {
					Thread.sleep(60 * 1000);
					requests = 0;
				}
			}
		}

		return allPL;
	}

	private Map<String, Double> getBaseConversionRate(String[] sellSymbols,
			String baseSymbol) {
		Map<String, Double> baseConversionRate = new HashMap<>();
		for (String sellSymbol : sellSymbols) {
			if (sellSymbol.equals(baseSymbol)) {
				baseConversionRate.put(sellSymbol, 1.0);
				continue;
			}
			try {
				baseConversionRate.put(sellSymbol, getMarketPrice(
						StringUtils.join(sellSymbol, baseSymbol)));
			} catch (BinanceApiException e) {
				// Invalid symbol exception check
				if (e.getError() != null
						&& e.getError().getCode() == -1121) {
					try {
						baseConversionRate.put(sellSymbol, 1 / getMarketPrice(
								StringUtils.join(baseSymbol, sellSymbol)));
					} catch (BinanceApiException ex) {
						// Invalid symbol exception check
						if (ex.getError() != null
								&& ex.getError().getCode() == -1121) {
						} else {
							throw ex;
						}
					}
				} else {
					throw e;
				}
			}
		}
		return baseConversionRate;
	}

	public ProfitLoss getPL(String symbol, List<Trade> trades) {
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
					realizedPl += Double.parseDouble(trade.getPrice()) * qty
							- currentCost;
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
		double unrealizedPlRatio = currentQty == 0
				? 0
				: (marketPrice - avgPrice) / avgPrice * 100;

		return ProfitLoss.builder()
				.symbol(symbol)
				.marketPrice(marketPrice)
				.avgPrice(avgPrice)
				.cost(currentCost)
				.qty(currentQty)
				.realizedPl(realizedPl)
				.unrealizedPl(unrealizedPl)
				.unrealizedPlRatio(unrealizedPlRatio)
				.build();
	}

	public ProfitLoss getPL(String buySymbol, String sellSymbol,
			List<Trade> trades) {
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
					realizedPl += Double.parseDouble(trade.getPrice()) * qty
							- currentCost;
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

		double marketPrice = getMarketPrice(
				StringUtils.join(buySymbol, sellSymbol));
		double avgPrice = currentQty == 0 ? 0 : currentCost / currentQty;
		double unrealizedPl = currentQty == 0
				? 0
				: (marketPrice - avgPrice) * currentQty;
		double unrealizedPlRatio = currentQty == 0
				? 0
				: (marketPrice - avgPrice) / avgPrice * 100;

		return ProfitLoss.builder()
				.symbol(buySymbol)
				.sellCurrency(sellSymbol)
				.marketPrice(marketPrice)
				.avgPrice(avgPrice)
				.cost(currentCost)
				.qty(currentQty)
				.realizedPl(realizedPl)
				.unrealizedPl(unrealizedPl)
				.unrealizedPlRatio(unrealizedPlRatio)
				.build();
	}

	public List<ProfitLoss> aggregateByBuySymbols(List<ProfitLoss> allPL,
			String[] sellSymbols, String baseSymbol) {
		List<ProfitLoss> result = new ArrayList<>();
		Map<String, Double> baseConversionRate = getBaseConversionRate(
				sellSymbols, baseSymbol);
		Map<String, List<ProfitLoss>> grouped = allPL
				.stream().map(pl -> ProfitLoss.builder()
						.symbol(pl.getSymbol())
						.sellCurrency(pl.getSellCurrency())
						.qty(pl.getQty())
						.unrealizedPlRatio(pl.getUnrealizedPlRatio())
						.avgPrice(pl.getAvgPrice()
								* baseConversionRate.get(pl.getSellCurrency()))
						.marketPrice(pl.getMarketPrice()
								* baseConversionRate.get(pl.getSellCurrency()))
						.cost(pl.getCost()
								* baseConversionRate.get(pl.getSellCurrency()))
						.realizedPl(pl.getRealizedPl()
								* baseConversionRate.get(pl.getSellCurrency()))
						.unrealizedPl(pl.getUnrealizedPl()
								* baseConversionRate.get(pl.getSellCurrency()))
						.build())
				.collect(Collectors.groupingBy(ProfitLoss::getSymbol));
		grouped.values().stream().forEach(group -> {
			ProfitLoss pl = ProfitLoss.builder()
					.symbol(group.get(0).getSymbol())
					.sellCurrency(baseSymbol)
					.marketPrice(group.get(0).getMarketPrice())
					.build();
			for (ProfitLoss entry : group) {
				pl.setCost(pl.getCost() + entry.getCost());
				pl.setQty(pl.getQty() + entry.getQty());
				pl.setRealizedPl(pl.getRealizedPl() + entry.getRealizedPl());
				pl.setUnrealizedPl(
						pl.getUnrealizedPl() + entry.getUnrealizedPl());
			}
			pl.setAvgPrice(pl.getCost() / pl.getQty());
			pl.setUnrealizedPlRatio(pl.getQty() == 0
					? 0
					: pl.getUnrealizedPl() / pl.getCost() * 100);
			result.add(pl);
		});
		return result;
	}
}
