package com.binance.trader.monitor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.binance.api.client.domain.account.ProfitLoss;

public class StatementGenerator {

	public Map<String, Double> aggregate(List<ProfitLoss> allPl) {

		Map<String, Double> result = new HashMap<>();
		result.put("TotalRealizedProfitLoss", allPl.stream()
				.map(s -> s.getRealizedPl()).reduce(0.0, Double::sum));
		result.put("TotalCost",
				allPl.stream().map(s -> s.getCost()).reduce(0.0, Double::sum));
		result.put("TotalUnrealizedProfitLoss", allPl.stream()
				.map(s -> s.getUnrealizedPl()).reduce(0.0, Double::sum));
		result.put("TotalCapitalValue", result.get("TotalCost")
				+ result.get("TotalUnrealizedProfitLoss"));
		return result;
	}

	public void generateStatement(List<ProfitLoss> allPL,
			double smallAssetCostThreshold, boolean separateZeroQuantity) {

		System.out.printf("As of: %s\n", LocalDateTime.now().toString());
		allPL.sort((pl1,
				pl2) -> (int) (pl2.getUnrealizedPl() - pl1.getUnrealizedPl()));
		List<ProfitLoss> statementAssets = allPL;
		List<ProfitLoss> zeroQuantityAssets = null;
		if (separateZeroQuantity) {
			zeroQuantityAssets = allPL.stream()
					.filter(pl -> pl.getQty() <= 0)
					.collect(Collectors.toList());
			statementAssets = allPL.stream().filter(pl -> pl.getQty() > 0)
					.collect(Collectors.toList());
		}
		List<ProfitLoss> smallAssets = statementAssets.stream()
				.filter(pl -> pl.getCost() <= smallAssetCostThreshold)
				.collect(Collectors.toList());
		List<ProfitLoss> largeAssets = statementAssets.stream()
				.filter(pl -> pl.getCost() > smallAssetCostThreshold)
				.collect(Collectors.toList());
		
		if (largeAssets.size() > 0) {
			System.out.printf("LARGE ASSETS (Cost > %.2f)\n", smallAssetCostThreshold);
			printStatement(largeAssets);
		}
		if (smallAssets.size() > 0) {
			System.out.printf("SMALL ASSETS (Cost <= %.2f)\n", smallAssetCostThreshold);
			printStatement(smallAssets);
		}
		if (zeroQuantityAssets != null && zeroQuantityAssets.size() > 0) {
			System.out.printf("PAST ASSETS\n");
			printStatement(zeroQuantityAssets);
		}
	}

	private void printStatement(List<ProfitLoss> allPL) {
		System.out.printf("%-10s%14s%14s%14s%14s%12s%15s%15s\n", "Symbol",
				"MarketPrice", "AvgPrice", "Cost",
				"Quantity", "RealizedPL", "UnrealizedPL", "UnrealizedPL%");

		for (ProfitLoss pl : allPL) {
			if (pl.getRealizedPl() != 0 || pl.getQty() > 0) {
				System.out.printf(
						"%-10s%14.5f%14.5f%14.4f%14.4f%12.2f%15.2f%14.2f%%\n",
						pl.getSymbol(),
						pl.getMarketPrice(), pl.getAvgPrice(), pl.getCost(),
						pl.getQty(), pl.getRealizedPl(),
						pl.getUnrealizedPl(), pl.getUnrealizedPlRatio());
			}
		}

		Map<String, Double> statement = aggregate(allPL);

		System.out.println();
		System.out.printf("Total Realized P/L: %.4f\n",
				statement.get("TotalRealizedProfitLoss"));
		System.out.printf("Total Cost: %.4f\n", statement.get("TotalCost"));
		System.out.printf("Total Unrealized P/L: %.4f\n",
				statement.get("TotalUnrealizedProfitLoss"));
		System.out.printf("Total Capital Value: %.4f\n",
				statement.get("TotalCapitalValue"));
		System.out.println();
	}

	public void generateStatement(List<ProfitLoss> allPL) {
		generateStatement(allPL, -1, false);
	}
}