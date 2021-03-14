package com.binance.trader.examples;

import java.util.List;
import java.util.Map;

import com.binance.api.client.domain.account.ProfitLoss;
import com.binance.trader.monitor.Analyzer;

public class AnalyzerExample {

	public static void main(String[] args) throws Exception {
		String[] symbols = { "BNBBUSD", "BTCBUSD", "ETHBUSD", "ADABUSD", "UNIBUSD", "LINKBUSD", "CHZUSDT", "CHZBUSD",
				"DOGEBUSD", "BAKEBUSD", "ETCBUSD", "CREAMBUSD", "BATBUSD", "LTCBUSD", "COMPBUSD", "VETBUSD",
				"NEARBUSD" };
		Analyzer a = new Analyzer("<<your API key>>", "<<your secret key>>");
		while (true) {
			List<ProfitLoss> allPl = a.getAllPl(symbols);
			Map<String, Double> statement = a.analyze(allPl);
			a.printPlStatement(allPl, statement);
			Thread.sleep(900000); // 15 mins
		}
	}
}
