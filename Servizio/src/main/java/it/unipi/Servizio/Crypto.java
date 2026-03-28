package it.unipi.Servizio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crypto")
public class Crypto {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "current_price")
    private double currentPrice;

    @Column(name = "market_cap_rank")
    private int marketCapRank;

    @Column(name = "price_change_percentage_24h")
    private double priceChangePercentage24h;

    @Column(name = "favorite")
    private boolean favorite;

    public Crypto() {
    }

    public Crypto(String id, String name, double currentPrice, int marketCapRank, double priceChangePercentage24h, boolean favorite) {
        this.id = id;
        this.name = name;
        this.currentPrice = currentPrice;
        this.marketCapRank = marketCapRank;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getMarketCapRank() {
        return marketCapRank;
    }

    public void setMarketCapRank(int marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}
