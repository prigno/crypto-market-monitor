package it.unipi.applicazione;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchController {

    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private CheckBox positiveChangeButton;

    @FXML
    private TableView<Crypto> searchTable;
    private ObservableList<Crypto> ol;

    private static final Logger logger = LogManager.getLogger(SearchController.class);

    @FXML
    public void initialize() {
        TableColumn<Crypto, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Crypto, Integer> marketCapRankCol = new TableColumn<>("Market cap rank");
        marketCapRankCol.setCellValueFactory(new PropertyValueFactory<>("marketCapRank"));

        TableColumn<Crypto, Double> currentPriceCol = new TableColumn<>("Current price (€)");
        currentPriceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        TableColumn<Crypto, Double> priceChangePercentage24hCol = new TableColumn<>("Price change percentage 24h (%)");
        priceChangePercentage24hCol.setCellValueFactory(new PropertyValueFactory<>("priceChangePercentage24h"));

        TableColumn<Crypto, Boolean> favoriteCol = new TableColumn<>("★");
        favoriteCol.setCellValueFactory(new PropertyValueFactory<>("favorite"));

        searchTable.getColumns().clear();
        searchTable.getColumns().addAll(nameCol, marketCapRankCol, currentPriceCol, priceChangePercentage24hCol, favoriteCol);
        searchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ol = FXCollections.observableArrayList();
        searchTable.setItems(ol);

        searchCrypto();
    }

    @FXML
    public void searchCrypto() {
        String minText = minPriceField.getText().trim();
        String maxText = maxPriceField.getText().trim();
        boolean pos = positiveChangeButton.isSelected();
        
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                Double min;
                Double max;
                try {
                    if(minText.isEmpty()) {
                        min = null;
                    } else {
                        min = Double.parseDouble(minText);
                    }
                } catch (NumberFormatException e) {
                    min = null;
                }
                try {
                    if(maxText.isEmpty()) {
                        max = null;
                    } else {
                        max = Double.parseDouble(maxText);
                    }
                } catch (NumberFormatException e) {
                    max = null;
                }        
                try {                    
                    logger.info("Ricerca crypto con min=" + min + " max=" + max + " positive=" + pos + " in corso...");
                    
                    URL url = new URL("http://localhost:8080/crypto/search?" + (min != null ? "minPrice=" + min + "&" : "") + (max != null ? "maxPrice=" + max + "&" : "") + "positiveChange=" + pos);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                    in.close();

                    Gson gson = new Gson();
                    JsonArray array = gson.fromJson(content.toString(), JsonArray.class);
                    List<Crypto> cryptos = new ArrayList<>();
                    for (JsonElement e : array) {
                        JsonObject obj = e.getAsJsonObject();
                        Crypto c = new Crypto(obj.get("id").getAsString(), obj.get("name").getAsString(), obj.get("currentPrice").getAsDouble(), obj.get("marketCapRank").getAsInt(), obj.get("priceChangePercentage24h").getAsDouble(), obj.get("favorite").getAsBoolean());
                        cryptos.add(c);
                    }

                    logger.info("Ricerca completata");

                    Platform.runLater(() -> {
                        ol.clear();
                        ol.addAll(cryptos);
                    });

                } catch (IOException e) {
                    logger.error("Errore durante la ricerca: " + e);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void updateFavorites(boolean value) {
        Crypto selected = searchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.isFavorite() == value) {
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Aggiornamento del campo favorite a " + value + " della crypto " + selected.getId());

                    URL url = new URL("http://localhost:8080/crypto/" + selected.getId() + "/favorite?value=" + value);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.getResponseCode();
                    
                    logger.info("Aggiornamento completato con successo");

                    Platform.runLater(() -> {
                        selected.setFavorite(value);
                        searchTable.refresh();
                    });

                } catch (IOException e) {
                    logger.error("Errore durante l'aggiornamento: " + e);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void addToFavorites() {
        updateFavorites(true);
    }

    @FXML
    public void removeFromFavorites() {
        updateFavorites(false);
    }

    @FXML
    public void switchToMain() throws IOException {
        App.setRoot("main");
    }

}
