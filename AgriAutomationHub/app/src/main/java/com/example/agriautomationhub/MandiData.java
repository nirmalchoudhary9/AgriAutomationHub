package com.example.agriautomationhub;

public class MandiData {
    String district, market, commodity, minPrice, maxPrice, modalPrice, priceDate;

    public MandiData(String market, String commodity,
                     String minPrice, String maxPrice, String priceDate) {
        this.market = market;
        this.commodity = commodity;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceDate = priceDate;
    }
}
