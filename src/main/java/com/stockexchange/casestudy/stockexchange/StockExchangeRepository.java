package com.stockexchange.casestudy.stockexchange;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockExchangeRepository extends JpaRepository<StockExchange, Long> {

    Optional<StockExchange> findByName(String name);
}
