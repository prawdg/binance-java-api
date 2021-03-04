package com.binance.trader.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

/**
 * MACD strategy.
 */
public class MACDStrategyBuilder {

	/**
	 * @param series
	 *            the bar series
	 * @return the moving momentum strategy
	 */
	public Strategy buildStrategy(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

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
}
