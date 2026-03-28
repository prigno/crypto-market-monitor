package it.unipi.Servizio;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/crypto")
public class CryptoController {

    private final CryptoRepository cryptoRepository;
    private final CryptoDetailsRepository cryptoDetailsRepository;
    private final CryptoChartRepository cryptoChartRepository;

    private static final Logger logger = LogManager.getLogger(CryptoController.class);

    public CryptoController(CryptoRepository cryptoRepository, CryptoDetailsRepository cryptoDetailsRepository, CryptoChartRepository cryptoChartRepository) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoDetailsRepository = cryptoDetailsRepository;
        this.cryptoChartRepository = cryptoChartRepository;
    }

    @PostMapping("/inizializza")
    public @ResponseBody void inizializza() {
        try {
            logger.info("Inizializzazione del database in corso...");

            cryptoRepository.deleteAll();
            cryptoDetailsRepository.deleteAll();
            cryptoChartRepository.deleteAll();

            URL url = new URL("https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&per_page=100&page=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != 200) {
                logger.error("Risposta del servizio esterno " + status);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();

            Gson gson = new Gson();
            JsonArray array = gson.fromJson(content.toString(), JsonArray.class);

            for (JsonElement e : array) {
                JsonObject obj = e.getAsJsonObject();
                Crypto c = new Crypto(obj.get("id").getAsString(), obj.get("name").getAsString(), obj.get("current_price").getAsDouble(), obj.get("market_cap_rank").getAsInt(), (!obj.get("price_change_percentage_24h").isJsonNull() ? obj.get("price_change_percentage_24h").getAsDouble() : 0.0), false);
                cryptoRepository.save(c);
            }

            logger.info("Inizializzazione completata con successo");

        } catch (JsonSyntaxException e) {
            logger.error("Errore nel parsing JSON: " + e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        } catch (IOException e) {
            logger.error("Servizio esterno non raggiungibile: " + e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping("/search")
    public @ResponseBody Iterable<Crypto> searchCrypto(@RequestParam(required = false) Double minPrice, @RequestParam(required = false) Double maxPrice, @RequestParam boolean positiveChange) {
        return cryptoRepository.search(minPrice, maxPrice, positiveChange);
    }

    @PutMapping("/{id}/favorite")
    public @ResponseBody void setFavorite(@PathVariable String id, @RequestParam boolean value) {
        Crypto crypto = cryptoRepository.findById(id).orElseThrow();
        crypto.setFavorite(value);
        cryptoRepository.save(crypto);
    }

    @GetMapping("/favorites")
    public @ResponseBody Iterable<Crypto> favorites() {
        return cryptoRepository.findByFavoriteTrueOrderByMarketCapRankAsc();
    }

    @PutMapping("/clear")
    public @ResponseBody void removeFavorites() {
        Iterable<Crypto> ic = cryptoRepository.findAll();
        for (Crypto c : ic) {
            c.setFavorite(false);
            cryptoRepository.save(c);
        }
    }

    @PutMapping("/update")
    public @ResponseBody void update() {
        try {
            logger.info("Aggiornamento delle crypto in corso...");
            
            URL url = new URL("https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&per_page=100&page=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int status = connection.getResponseCode();
            if (status != 200) {
                logger.error("Risposta del servizio esterno " + status);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();
            
            Set<String> ss = cryptoRepository.findFavoriteCryptoId();
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(content.toString(), JsonArray.class);
            for (JsonElement e : array) {
                JsonObject obj = e.getAsJsonObject();
                boolean favorite = false;
                if (ss.contains(obj.get("id").getAsString())) {
                    favorite = true;
                }
                Crypto c = new Crypto(obj.get("id").getAsString(), obj.get("name").getAsString(), obj.get("current_price").getAsDouble(), obj.get("market_cap_rank").getAsInt(), (!obj.get("price_change_percentage_24h").isJsonNull() ? obj.get("price_change_percentage_24h").getAsDouble() : 0.0), favorite);
                cryptoRepository.save(c);
            }
            
            logger.info("Aggiornamento delle crypto completato con successo");

            logger.info("Aggiornamento dei dettagli in corso...");
            
            Iterable<CryptoDetails> icd = cryptoDetailsRepository.findAll();
            int i = 1;
            long count = cryptoDetailsRepository.count();
            for (CryptoDetails c : icd) {
                Thread.sleep(12000);
                
                logger.info("[" + i + " di " + count + "] Aggiornamento dettagli di " + c.getId());
                i++;
                
                url = new URL("https://api.coingecko.com/api/v3/coins/" + c.getId());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                status = connection.getResponseCode();
                if (status != 200) {
                    logger.error("Risposta del servizio esterno " + status);
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
                }
                
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                content = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();
                
                c.setJson(content.toString());
                cryptoDetailsRepository.save(c);
            }
            
            logger.info("Aggiornamento dei dettagli completato con successo");

            logger.info("Aggiornamento dei valori dei grafici in corso...");
            
            Iterable<CryptoChart> icc = cryptoChartRepository.findAll();
            i = 1;
            count = cryptoChartRepository.count();
            
            for (CryptoChart c : icc) {
                Thread.sleep(12000);
                
                int idx = c.getId().lastIndexOf('-');
                String id = c.getId().substring(0, idx);
                String days = c.getId().substring(idx + 1);
                logger.info("[" + i + " di " + count + "] Aggiornamento dei valori del grafico di " + id + " da " + days + " giorni");
                i++;
                
                url = new URL("https://api.coingecko.com/api/v3/coins/" + id + "/market_chart?vs_currency=eur&days=" + days);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                status = connection.getResponseCode();
                if (status != 200) {
                    logger.error("Risposta del servizio esterno " + status);
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
                }
                
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                content = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();
                
                c.setJson(content.toString());
                cryptoChartRepository.save(c);
            }
            
            logger.info("Aggiornamento dei valori dei grafici completato con successo");

        } catch (JsonSyntaxException e) {
            logger.error("Errore nel parsing JSON: " + e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        } catch (IOException e) {
            logger.error("Servizio esterno non raggiungibile: " + e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (InterruptedException e) {
            logger.error("Aggiornamento fallito: " + e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/details")
    public @ResponseBody String getCryptoDetails(@PathVariable String id) {
        try {
            Optional<CryptoDetails> cached = cryptoDetailsRepository.findById(id);
            if (cached.isPresent()) {
                logger.info("Dettagli della crypto " + id + " presenti nel database");
                return cached.get().getJson();
            }

            URL url = new URL("https://api.coingecko.com/api/v3/coins/" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != 200) {
                logger.error("Risposta del servizio esterno " + status);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();

            String json = content.toString();

            CryptoDetails details = new CryptoDetails(id, json);
            cryptoDetailsRepository.save(details);

            logger.info("Dettagli della crypto " + id + " salvati nel database");

            return json;

        } catch (IOException e) {
            logger.error("Servizio esterno non raggiungibile: " + e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping("/{id}/chart")
    public @ResponseBody String getCryptoChart(@PathVariable String id, @RequestParam int days) {
        try {
            Optional<CryptoChart> cached = cryptoChartRepository.findById(id + "-" + days);
            if (cached.isPresent()) {
                logger.info("Valori del grafico della crypto " + id + " presenti nel database");
                return cached.get().getJson();
            }

            URL url = new URL("https://api.coingecko.com/api/v3/coins/" + id + "/market_chart?vs_currency=eur&days=" + days);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != 200) {
                logger.error("Risposta del servizio esterno " + status);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();

            String json = content.toString();

            CryptoChart chart = new CryptoChart(id + "-" + days, json);
            cryptoChartRepository.save(chart);

            logger.info("Valori del grafico della crypto " + id + " salvati nel database");

            return json;
            
        } catch (IOException e) {
            logger.error("Servizio esterno non raggiungibile: " + e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

}
