package it.unipi.applicazione;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainController {

    @FXML
    private Button initButton;
    @FXML
    private Button updateButton;
    @FXML
    private Label statusLabel;

    private static final Logger logger = LogManager.getLogger(MainController.class);

    @FXML
    public void inizializza() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Invio richiesta di inizializzazione al servizio");
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("Inizializzazione in corso...");
                        initButton.setDisable(true);
                    });
                    
                    URL url = new URL("http://localhost:8080/crypto/inizializza");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    
                    int status = connection.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        logger.error("Inizializzazione fallita. Codice di errore " + status);
                        Platform.runLater(() -> {
                            statusLabel.setText("Errore durante l'inizializzazione");
                            initButton.setDisable(false);
                        });
                        return null;
                    }
                    
                    logger.info("Inizializzazione completata con successo");
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("Inizializzazione completata con successo");
                        initButton.setDisable(false);
                    });
                    
                } catch (IOException e) {
                    logger.error("Errore durante l'inizializzazione: " + e);
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("Errore durante l'inizializzazione");
                        initButton.setDisable(false);
                    });
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void update() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    logger.info("Invio richiesta di aggiornamento al servizio");
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("            Aggiornamento dei dati in corso...\nL'operazione potrebbe richiedere qualche minuto");
                        updateButton.setDisable(true);
                    });
                    
                    URL url = new URL("http://localhost:8080/crypto/update");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    
                    int status = connection.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        logger.error("Aggiornamento fallito. Codice di errore " + status);
                        Platform.runLater(() -> {
                            statusLabel.setText("Errore durante l'aggiornamento dei dati");
                            updateButton.setDisable(false);
                        });
                        return null;
                        }
                    
                    logger.info("Aggiornamento completato con successo");
                    Platform.runLater(() -> {
                        statusLabel.setText("Aggiornamento completato con successo");
                        updateButton.setDisable(false);
                    });

                } catch (IOException e) {
                    logger.error("Errore durante l'aggiornamento dei dati: " + e);
                    Platform.runLater(() -> {
                        statusLabel.setText("Errore durante l'aggiornamento dei dati");
                        updateButton.setDisable(false);
                    });
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void showFavorites() throws IOException {
        App.setRoot("favorites");
    }

    @FXML
    public void showSearch() throws IOException {
        App.setRoot("search");
    }

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }

}
