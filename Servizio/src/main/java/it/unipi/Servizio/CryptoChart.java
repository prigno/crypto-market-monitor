package it.unipi.Servizio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crypto_chart")
public class CryptoChart {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "json", columnDefinition = "MEDIUMTEXT")
    private String json;

    public CryptoChart() {
    }

    public CryptoChart(String id, String json) {
        this.id = id;
        this.json = json;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}
