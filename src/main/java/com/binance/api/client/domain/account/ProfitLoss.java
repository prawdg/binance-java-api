package com.binance.api.client.domain.account;

public class ProfitLoss {
	
	  private String symbol;
	  
	  private double marketPrice;
	  
	  private double qty;
	  
	  private double cost;
	  
	  private double avgPrice;
	  
	  private double realizedPl;
	  
	  private double unrealizedPl;

	  public void setSymbol(String symbol) {
		    this.symbol = symbol;
		  }
	  
	  public void setMarketPrice(double marketPrice) {
		    this.marketPrice = marketPrice;
		  }
	  
	  public void setQty(double qty) {
		    this.qty = qty;
		  }
	  
	  public void setCost(double cost) {
		    this.cost = cost;
		  }
	  
	  public void setAvgPrice(double avgPrice) {
		    this.avgPrice = avgPrice;
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
	  
	  public double getMarketPrice() {
		  	return marketPrice;
	  }
	  
	  public double getQty() {
		    return qty;
		  }
	  
	  public double getCost() {
		    return cost;
		  }
	  
	  public double getAvgPrice() {
		    return avgPrice;
		  }
	  
	  public double getRealizedPl() {
		    return realizedPl;
		  }
	  
	  public double getUnrealizedPl() {
		    return unrealizedPl;
		  }
}
