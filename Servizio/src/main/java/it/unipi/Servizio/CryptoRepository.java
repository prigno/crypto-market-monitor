package it.unipi.Servizio;

import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CryptoRepository extends CrudRepository<Crypto, String> {

    Iterable<Crypto> findByFavoriteTrueOrderByMarketCapRankAsc();

    @Query("""
           SELECT c
           FROM Crypto c
           WHERE (:minPrice IS NULL OR c.currentPrice > :minPrice) AND
                 (:maxPrice IS NULL OR c.currentPrice < :maxPrice) AND
                 (:positiveChange = false OR c.priceChangePercentage24h > 0)
           ORDER BY c.marketCapRank ASC
           """)
    Iterable<Crypto> search(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, @Param("positiveChange") boolean positiveChange);

    @Query("""
           SELECT c.id 
           FROM Crypto c 
           WHERE c.favorite = true
           """)
    Set<String> findFavoriteCryptoId();
}
