package com.binance.trader.util;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.util.DateTimeUtil;

public class BinanceBarLoader {

	public static Map<CandlestickInterval, Duration> period;

	static {
		period = new HashMap<>();
		period.put(CandlestickInterval.FIFTEEN_MINUTES, Duration.ofMinutes(15));
		period.put(CandlestickInterval.HALF_HOURLY, Duration.ofMinutes(30));
		period.put(CandlestickInterval.HOURLY, Duration.ofHours(1));
		period.put(CandlestickInterval.FOUR_HORLY, Duration.ofHours(4));
		period.put(CandlestickInterval.DAILY, Duration.ofDays(1));
	}

	public static BarSeries loadBars(String symbol,
			CandlestickInterval interval) {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiRestClient client = factory.newRestClient();
		List<Candlestick> candlestickBars = client
				.getCandlestickBars(symbol.toUpperCase(), interval);
		System.out.printf("Found %d candles\n", candlestickBars.size());
		BaseBarSeries series = new BaseBarSeries(symbol,
				candlestickBars
						.stream()
						.map(candle -> new BaseBar(period.get(interval),
								DateTimeUtil
										.toZonedDateTime(candle.getCloseTime()).withZoneSameInstant(ZoneOffset.ofHours(8)),
								candle.getOpen(),
								candle.getHigh(), candle.getLow(),
								candle.getClose(), candle.getVolume()))
						.collect(Collectors.toList()));
		return series;
	}

}
