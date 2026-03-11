package com.urlshortener.repository;

import com.urlshortener.domain.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByUrlId(Long urlId);

    List<Click> findByUrlIdOrderByClickedAtDesc(Long urlId);

    // Click count grouped by date (for analytics chart)
    @Query("""
        SELECT DATE(c.clickedAt) as date, COUNT(c) as count
        FROM Click c
        WHERE c.url.id = :urlId AND c.clickedAt >= :since
        GROUP BY DATE(c.clickedAt)
        ORDER BY DATE(c.clickedAt)
    """)
    List<Object[]> countClicksByDaySince(@Param("urlId") Long urlId,
                                          @Param("since") Instant since);
}
