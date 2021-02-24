package com.binance.trader.strategy;

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
import com.binance.trader.util.BinanceBarLoader;

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
		Rule entryRule = new OverIndicatorRule(shortEma, longEma)
				// .and(new CrossedDownIndicatorRule(stochasticOscillK, 20))
				.and(new OverIndicatorRule(macd, emaMacd));

		// Exit rule
		Rule exitRule = new UnderIndicatorRule(shortEma, longEma)
				// .and(new CrossedUpIndicatorRule(stochasticOscillK, 20))
				.and(new UnderIndicatorRule(macd, emaMacd));

		return new BaseStrategy(entryRule, exitRule);
	}

	public static void main(String[] args) {

		// Getting the bar series
		BarSeries series = BinanceBarLoader.loadBars("COMPBUSD",
				CandlestickInterval.HOURLY);

		// Building the trading strategy
		Strategy strategy = buildStrategy(series);

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
