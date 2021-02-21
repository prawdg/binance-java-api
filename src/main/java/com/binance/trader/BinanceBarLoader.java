package com.binance.trader;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

public class BinanceBarLoader {

	public static Map<CandlestickInterval, Duration> period;

	static {
		period = new HashMap<>();
		period.put(CandlestickInterval.FIFTEEN_MINUTES, Duration.ofMinutes(15));
		period.put(CandlestickInterval.HALF_HOURLY, Duration.ofMinutes(30));
		period.put(CandlestickInterval.HOURLY, Duration.ofHours(1));
		period.put(CandlestickInterval.FOUR_HORLY, Duration.ofHours(4));
	}

	public static BarSeries loadBars(String symbol,
			CandlestickInterval interval) {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
				"wjZJjRsUVSdodJwTswXqfTgCMDIctZNK2G6BLj8zbkD6Qx2rSVKaKaksexOTPfgl",
				"e1UOvjUVdmKYPxuMhJs9DdIuFn15OesVo27pDIEc9WS9ty0kIkfuIlhtH5im7zAi");
		BinanceApiRestClient client = factory.newRestClient();
		List<Candlestick> candlestickBars = client
				.getCandlestickBars(symbol.toUpperCase(), interval);
		BaseBarSeries series = new BaseBarSeries(symbol,
				candlestickBars
						.stream()
						.map(candle -> new BaseBar(period.get(interval),
								toZonedDateTime(candle.getCloseTime()),
								candle.getOpen(),
								candle.getHigh(), candle.getLow(),
								candle.getClose(), candle.getVolume()))
						.collect(Collectors.toList()));
		return series;
	}

	private static ZonedDateTime toZonedDateTime(Long time) {
		return ZonedDateTime
				.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
	}

}
