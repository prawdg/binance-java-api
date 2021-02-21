package com.binance.api.examples;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Order.OrderType;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.trader.BinanceBarLoader;

/**
 * Moving momentum strategy.
 *
 * @see <a href=
 *      "http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum">
 *      http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum</a>
 */
public class MACDStrategy {

	/**
	 * @param series
	 *            the bar series
	 * @return the moving momentum strategy
	 */
	public static Strategy buildStrategy(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

		// The bias is bullish when the shorter-moving average moves above the
		// longer
		// moving average.
		// The bias is bearish when the shorter-moving average moves below the
		// longer
		// moving average.
		EMAIndicator shortEma = new EMAIndicator(closePrice, 12);
		EMAIndicator longEma = new EMAIndicator(closePrice, 26);

		StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(
				series, 14);

		MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
		EMAIndicator emaMacd = new EMAIndicator(macd, 9);

		// Entry rule
		Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
				// .and(new CrossedDownIndicatorRule(stochasticOscillK, 20)) //
				// Signal
				// // 1
				.and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

		// Exit rule
		Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
				// .and(new CrossedUpIndicatorRule(stochasticOscillK, 20)) //
				// Signal
				// // 1
				.and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2

		return new BaseStrategy(entryRule, exitRule);
	}

	public static void main(String[] args) {

		// Getting the bar series
		BarSeries series = BinanceBarLoader.loadBars("ICXBUSD",
				CandlestickInterval.HALF_HOURLY);

		// Building the trading strategy
		Strategy strategy = buildStrategy(series);

		// Running the strategy
		BarSeriesManager seriesManager = new BarSeriesManager(series);
		TradingRecord tradingRecord = seriesManager.run(strategy, OrderType.BUY,
				series.numOf(700));
		evaluateRecord(series, tradingRecord);
	}

	private static void evaluateRecord(BarSeries series,
			TradingRecord tradingRecord) {
		Num profit = series.numOf(0);
		for (int i = 0; i < tradingRecord.getTradeCount(); i++) {
			Trade trade = tradingRecord.getTrades().get(i);
			profit = profit.plus(trade.getProfit());
			System.out.println(trade.getProfit());
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
