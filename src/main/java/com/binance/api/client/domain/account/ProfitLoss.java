package com.binance.api.client.domain.account;

public class ProfitLoss {
	
	  private String symbol;
	  
	  private double qty;
	  
	  private double currentCost;
	  
	  private double currentAvgPrice;
	  
	  private double realizedPl;
	  
	  private double unrealizedPl;

	  public void setSymbol(String symbol) {
		    this.symbol = symbol;
		  }
	  
	  public void setQty(double qty) {
		    this.qty = qty;
		  }
	  
	  public void setCurrentCost(double currentCost) {
		    this.currentCost = currentCost;
		  }
	  
	  public void setCurrentAvgPrice(double currentAvgPrice) {
		    this.currentAvgPrice = currentAvgPrice;
		  }
	  
	  public void setRealizedPl(double realizedPl) {
		    this.realizedPl = realizedPl;
		  }

	  public void setUnrealizedPl(double unrealizedPl) {
		    this.unrealizedPl = unrealizedPl;
		  }
	  
	  
	  
	  public String getSymbol() {
		    return symbol;
		  }
	  
	  public double getQty() {
		    return qty;
		  }
	  
	  public double getCurrentCost() {
		    return currentCost;
		  }
	  
	  public double getCurrentAvgPrice() {
		    return currentAvgPrice;
		  }
	  
	  public double getRealizedPl() {
		    return realizedPl;
		  }
	  
	  public double getUnealizedPl() {
		    return realizedPl;
		  }
}
