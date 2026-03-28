package it.unipi.applicazione;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetailsController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label symbolLabel;
    @FXML
    private Label rankLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label marketCapLabel;
    @FXML
    private Label athLabel;
    @FXML
    private Label athDateLabel;
    @FXML
    private Label high24Label;
    @FXML
    private Label low24Label;
    @FXML
    private Label p24Label;
    @FXML
    private Label p7Label;
    @FXML
    private Label p14Label;
    @FXML
    private Label p30Label;
    @FXML
    private Label p60Label;
    @FXML
    private Label p200Label;
    @FXML
    private Label p1Label;

    @FXML
    private Hyperlink homepageLink;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private LineChart<Number, Number> priceChart;

    @FXML
    private RadioButton month1Button;
    @FXML
    private RadioButton month3Button;
    @FXML
    private RadioButton month6Button;
    @FXML
    private RadioButton month12Button;
    @FXML
    private ToggleGroup months;

    private String currentCryptoId;

    private static final Logger logger = LogManager.getLogger(DetailsController.class);

    public void loadDetails(String id) {
        currentCryptoId = id;
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Caricamento dei dettagli in corso...");

                    URL url = new URL("http://localhost:8080/crypto/" + id + "/details");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int status = connection.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        logger.error("Caricamento dei dettagli fallito. Codice di errore " + status);
                        return null;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                    in.close();

                    Gson gson = new Gson();
                    JsonObject root = gson.fromJson(content.toString(), JsonObject.class);
                    JsonObject marketData = root.getAsJsonObject("market_data");

                    String name = root.has("name") ? root.get("name").getAsString() : null;
                    String symbol = root.has("symbol") ? root.get("symbol").getAsString() : null;
                    Integer rank = root.has("market_cap_rank") ? root.get("market_cap_rank").getAsInt() : null;
                    String description = (root.has("description") && root.getAsJsonObject("description").has("en")) ? root.getAsJsonObject("description").get("en").getAsString() : null;
                    String homepage = (root.has("links") && root.getAsJsonObject("links").has("homepage") && root.getAsJsonObject("links").getAsJsonArray("homepage").size() > 0) ? root.getAsJsonObject("links").getAsJsonArray("homepage").get(0).getAsString() : null;

                    Double price = (marketData.has("current_price") && marketData.getAsJsonObject("current_price").has("eur")) ? marketData.getAsJsonObject("current_price").get("eur").getAsDouble() : null;
                    Double marketCap = (marketData.has("market_cap") && marketData.getAsJsonObject("market_cap").has("eur")) ? marketData.getAsJsonObject("market_cap").get("eur").getAsDouble() : null;
                    Double ath = (marketData.has("ath") && marketData.getAsJsonObject("ath").has("eur")) ? marketData.getAsJsonObject("ath").get("eur").getAsDouble() : null;
                    String athDate = (marketData.has("ath_date") && marketData.getAsJsonObject("ath_date").has("eur")) ? marketData.getAsJsonObject("ath_date").get("eur").getAsString() : null;
                    Double high24 = (marketData.has("high_24h") && marketData.getAsJsonObject("high_24h").has("eur")) ? marketData.getAsJsonObject("high_24h").get("eur").getAsDouble() : null;
                    Double low24 = (marketData.has("low_24h") && marketData.getAsJsonObject("low_24h").has("eur")) ? marketData.getAsJsonObject("low_24h").get("eur").getAsDouble() : null;

                    Double p24 = (marketData.has("price_change_percentage_24h")) ? marketData.get("price_change_percentage_24h").getAsDouble() : null;
                    Double p7 = (marketData.has("price_change_percentage_7d")) ? marketData.get("price_change_percentage_7d").getAsDouble() : null;
                    Double p14 = (marketData.has("price_change_percentage_14d")) ? marketData.get("price_change_percentage_14d").getAsDouble() : null;
                    Double p30 = (marketData.has("price_change_percentage_30d")) ? marketData.get("price_change_percentage_30d").getAsDouble() : null;
                    Double p60 = (marketData.has("price_change_percentage_60d")) ? marketData.get("price_change_percentage_60d").getAsDouble() : null;
                    Double p200 = (marketData.has("price_change_percentage_200d")) ? marketData.get("price_change_percentage_200d").getAsDouble() : null;
                    Double p1 = (marketData.has("price_change_percentage_1y")) ? marketData.get("price_change_percentage_1y").getAsDouble() : null;

                    logger.info("Caricamento dei dettagli completato con successo");

                    Platform.runLater(() -> {
                        nameLabel.setText(name != null ? name : "");
                        symbolLabel.setText(symbol != null ? symbol.toUpperCase() : "");
                        rankLabel.setText(rank != null ? String.valueOf(rank) : "");
                        descriptionArea.setText(description != null ? description : "");
                        homepageLink.setText(homepage != null ? homepage : "");
                        priceLabel.setText(price != null ? price + " €" : "");
                        marketCapLabel.setText(marketCap != null ? marketCap + " €" : "");
                        athLabel.setText(ath != null ? ath + " €" : "");
                        athDateLabel.setText(athDate != null ? OffsetDateTime.parse(athDate).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "");
                        high24Label.setText(high24 != null ? high24 + " €" : "");
                        low24Label.setText(low24 != null ? low24 + " €" : "");
                        p24Label.setText(p24 != null ? String.format(Locale.US, "%.3f %%", p24) : "");
                        p7Label.setText(p7 != null ? String.format(Locale.US, "%.3f %%", p7) : "");
                        p14Label.setText(p14 != null ? String.format(Locale.US, "%.3f %%", p14) : "");
                        p30Label.setText(p30 != null ? String.format(Locale.US, "%.3f %%", p30) : "");
                        p60Label.setText(p60 != null ? String.format(Locale.US, "%.3f %%", p60) : "");
                        p200Label.setText(p200 != null ? String.format(Locale.US, "%.3f %%", p200) : "");
                        p1Label.setText(p1 != null ? String.format(Locale.US, "%.3f %%", p1) : "");
                    });

                } catch (IOException e) {
                    logger.error("Errore durante il caricamento dei dettagli: ", e);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void loadChart(long days) {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Caricamento del grafico in corso...");

                    URL url = new URL("http://localhost:8080/crypto/" + currentCryptoId + "/chart?days=" + days);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int status = connection.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        logger.error("Caricamento del grafico fallito. Codice di errore ", status);
                        return null;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                    in.close();

                    Gson gson = new Gson();
                    JsonObject obj = gson.fromJson(content.toString(), JsonObject.class);
                    JsonArray prices = obj.getAsJsonArray("prices");

                    XYChart.Series<Number, Number> chart = new XYChart.Series<>();

                    long now = System.currentTimeMillis();
                    long past = now - days * 24 * 60 * 60 * 1000;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

                    double minPrice = Double.MAX_VALUE;
                    double maxPrice = Double.MIN_VALUE;

                    for (int i = 0; i < prices.size(); i++) {
                        JsonArray jsonArray = prices.get(i).getAsJsonArray();
                        long timestamp = jsonArray.get(0).getAsLong();
                        double price = jsonArray.get(1).getAsDouble();

                        minPrice = Math.min(minPrice, price);
                        maxPrice = Math.max(maxPrice, price);

                        chart.getData().add(new XYChart.Data<>(timestamp, price));

                    }

                    logger.info("Caricamento del grafico completato con successo");

                    double range = maxPrice - minPrice;
                    double padding = range / 10;
                    double lowerBound = minPrice - padding;
                    double upperBound = maxPrice + padding;

                    Platform.runLater(() -> {
                        NumberAxis xAxis = (NumberAxis) priceChart.getXAxis();
                        xAxis.setAutoRanging(false);
                        xAxis.setLowerBound(past);
                        xAxis.setUpperBound(now);
                        xAxis.setTickUnit(days / 10 * 24 * 60 * 60 * 1000);
                        xAxis.setTickLabelFormatter(new StringConverter<>() {
                            @Override
                            public String toString(Number object) {
                                return formatter.format(Instant.ofEpochMilli(object.longValue()));
                            }

                            @Override
                            public Number fromString(String string) {
                                return null;
                            }
                        });

                        NumberAxis yAxis = (NumberAxis) priceChart.getYAxis();
                        yAxis.setAutoRanging(false);
                        yAxis.setLowerBound(lowerBound);
                        yAxis.setUpperBound(upperBound);
                        yAxis.setPrefWidth(90);
                        yAxis.setTickUnit(range / 10);

                        priceChart.getData().clear();
                        priceChart.getData().add(chart);
                    });

                } catch (IOException e) {
                    logger.error("Errore durante la creazione del grafico: ", e);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void reloadChart() {
        RadioButton selected = (RadioButton) months.getSelectedToggle();
        int days = 30;
        if (selected == month1Button) {
            days = 30;
        } else if (selected == month3Button) {
            days = 90;
        } else if (selected == month6Button) {
            days = 180;
        } else if (selected == month12Button) {
            days = 365;
        }
        loadChart(days);
    }

    @FXML
    public void openInBrowser() {
        try {
            Desktop.getDesktop().browse(new URI(homepageLink.getText()));
        } catch (IOException | URISyntaxException e) {
            logger.error("Errore durante l'apertura della pagine nel browser: " + e);
        }
    }

    @FXML
    public void initialize() {
        priceChart.setCreateSymbols(false);
        priceChart.setLegendVisible(false);
    }

}
