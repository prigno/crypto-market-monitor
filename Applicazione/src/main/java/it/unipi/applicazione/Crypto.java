package it.unipi.applicazione;

public class Crypto {

    private String id;
    private String name;
    private double currentPrice;
    private int marketCapRank;
    private double priceChangePercentage24h;
    private boolean favorite;

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

    public String getName() {
        return name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public int getMarketCapRank() {
        return marketCapRank;
    }

    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}
