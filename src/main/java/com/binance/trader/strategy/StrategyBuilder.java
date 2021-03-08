package com.binance.trader.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface StrategyBuilder {

	/**
	 * @param series
	 *            the bar series
	 * @return the moving momentum strategy
	 */
	Strategy buildStrategy(BarSeries series);

}