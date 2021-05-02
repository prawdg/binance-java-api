package com.binance.trader.examples;

import java.util.List;
import java.util.Map;

import com.binance.api.client.domain.account.ProfitLoss;
import com.binance.trader.monitor.Analyzer;
import com.binance.trader.monitor.StatementGenerator;

public class AnalyzerExample {

	public static void main(String[] args) throws Exception {
		String[] buySymbols = {"BTC", "ETH", "BNB",
				"LTC", "ETC", "UNI", "LINK", "ADA", "DOT", "COMP",
				"AAVE", "XMR", "CAKE", "NEAR", "ATOM", "DOGE",
				"DODO", "BAKE", "VET", "XRP", "BAT", "RSR", "SFP",
				"LIT", "BTT", "CREAM", "DATA", "FRONT", "BTCST", "ACM",
				"CHZ", "CHR", "ALICE", "DEGO", "DOTUP", "FUN",
				"IQ", "WIN", "ETHUP", "MANA", "TKO", "ADAUP", "PSG",
				"TLM", "VTHO", "BTT", "BAR", "GVT", "ALGO", "THETA",
				"MATIC", "NEO"};
		String[] sellSymbols = {"BUSD", "USDT", "BTC"};
		String baseSymbol = "BUSD";

		Analyzer a = new Analyzer("<<your API key>>", "<<your secret key>>");

		StatementGenerator sg = new StatementGenerator();
		while (true) {
			List<ProfitLoss> allPL = a.getAllPL(buySymbols, sellSymbols);
			allPL = a.aggregateByBuySymbols(allPL, sellSymbols, baseSymbol);
			sg.generateStatement(allPL, 300, true);
			Thread.sleep(900000); // 15 mins
		}
	}
}
