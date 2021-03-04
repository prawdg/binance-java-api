package com.binance.trader.examples;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Order.OrderType;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.num.Num;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.trader.strategy.MACDStrategy;
import com.binance.trader.util.BinanceBarLoader;

public class StrategyExample {

	public static void main(String[] args) {

		// Getting the bar series
		BarSeries series = BinanceBarLoader.loadBars("COMPBUSD",
				CandlestickInterval.HOURLY);

		MACDStrategy macdStrategy = new MACDStrategy();

		// Building the trading strategy
		Strategy strategy = macdStrategy.buildStrategy(series);

		// Running the strategy
		BarSeriesManager seriesManager = new BarSeriesManager(series);
		TradingRecord tradingRecord = seriesManager.run(strategy, OrderType.BUY,
				series.numOf(1), 36, series.getEndIndex());
		evaluateRecord(series, tradingRecord);
	}

	private static void evaluateRecord(BarSeries series,
			TradingRecord tradingRecord) {
		Num profit = series.numOf(0);
		for (int i = 0; i < tradingRecord.getTradeCount(); i++) {
			Trade trade = tradingRecord.getTrades().get(i);
			profit = profit.plus(trade.getProfit());
			System.out.printf(
					"Buy@ %.2f [%s]\tSell@ %.2f [%s]\t Profit: %.2f\n",
					trade.getEntry().getNetPrice().doubleValue(),
					series.getBar(trade.getEntry().getIndex()).getDateName(),
					trade.getExit().getNetPrice().doubleValue(),
					series.getBar(trade.getExit().getIndex()).getDateName(),
					trade.getProfit().doubleValue());
		}
		System.out.println("Profit: " + profit);
		System.out.println("Number of trades for the strategy: "
				+ tradingRecord.getTradeCount());

		// Analysis
		System.out.println(
				"Total profit for the strategy: " + new TotalProfitCriterion()
						.calculate(series, tradingRecord));
	}
}
