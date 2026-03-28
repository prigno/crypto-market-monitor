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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FavoritesController {

    @FXML
    private TableView<Crypto> favoritesTable;
    private ObservableList<Crypto> ol;

    private static final Logger logger = LogManager.getLogger(FavoritesController.class);

    public void initialize() {
        TableColumn<Crypto, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Crypto, Integer> marketCapRankCol = new TableColumn<>("Market cap rank");
        marketCapRankCol.setCellValueFactory(new PropertyValueFactory<>("marketCapRank"));

        TableColumn<Crypto, Double> currentPriceCol = new TableColumn<>("Current price");
        currentPriceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        TableColumn<Crypto, Double> priceChangePercentage24hCol = new TableColumn<>("Price change percentage 24h");
        priceChangePercentage24hCol.setCellValueFactory(new PropertyValueFactory<>("priceChangePercentage24h"));

        favoritesTable.getColumns().clear();
        favoritesTable.getColumns().addAll(nameCol, marketCapRankCol, currentPriceCol, priceChangePercentage24hCol);
        favoritesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ol = FXCollections.observableArrayList();
        favoritesTable.setItems(ol);

        loadCrypto();
    }

    private void loadCrypto() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Caricamento delle crypto preferite in corso...");

                    URL url = new URL("http://localhost:8080/crypto/favorites");
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

                    logger.info("Caricamento delle crypto preferite completato con successo");

                    Platform.runLater(() -> {
                        ol.clear();
                        ol.addAll(cryptos);
                    });
                } catch (IOException e) {
                    logger.error("Errore durante il caricamento delle crypto preferite: " + e);
                }

                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void removeFromFavorites() {
        Crypto selected = favoritesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Rimozione dai preferiti della crypto " + selected.getId() + " in corso...");

                    URL url = new URL("http://localhost:8080/crypto/" + selected.getId() + "/favorite?value=false");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.getResponseCode();

                    logger.info("Rimozione dai preferiti completata con successo");

                    Platform.runLater(() -> {
                        selected.setFavorite(false);
                        favoritesTable.getItems().remove(selected);
                    });
                    
                } catch (IOException e) {
                    logger.error("Errore durante la rimozione dai preferiti: " + e);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void removeAllFavorites() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Ripristino delle crpyto preferite in corso...");

                    URL url = new URL("http://localhost:8080/crypto/clear");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.getResponseCode();

                    logger.info("Ripristino completato con successo");

                    Platform.runLater(() -> {
                        for (Crypto c : ol) {
                            c.setFavorite(false);
                        }
                        ol.clear();
                    });

                } catch (IOException e) {
                    logger.error("Errore durante il ripristino dei preferiti: " + e);
                }

                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void showDetails() {
        Crypto selected = favoritesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("details.fxml"));
            Parent root = loader.load();

            DetailsController controller = loader.getController();
            controller.loadDetails(selected.getId());
            controller.loadChart(90);

            Stage stage = new Stage();
            stage.setTitle("Dettagli " + selected.getId());
            stage.setScene(new Scene(root));

            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            logger.error("Errore durante il caricamento dei dettagli: " + e);
        }
    }

    public void switchToMain() throws IOException {
        App.setRoot("main");
    }

}
