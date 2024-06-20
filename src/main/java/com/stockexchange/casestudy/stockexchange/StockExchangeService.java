package com.stockexchange.casestudy.stockexchange;

import com.stockexchange.casestudy.stock.Stock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockExchangeService {

    private final StockExchangeRepository stockExchangeRepository;

    public StockExchangeService(StockExchangeRepository stockExchangeRepository) {
        this.stockExchangeRepository = stockExchangeRepository;
    }

    /**
     * @param name Stock exchange name to find StockExchange
     * @return StockExchange
     * throws {@link IllegalArgumentException} if stock exchange can not be found
     */
    public StockExchange getStockExchangeByName(String name) {
        return this.stockExchangeRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Stock Exchange not found!!!"));
    }

    /**
     * @param name  Stock exchange name to add Stock
     * @param stock Stock to add into StockExchange
     * @return StockExchange updated after add Stock to it.
     */
    @Transactional
    public StockExchange addStockToStockExchange(String name, Stock stock) {
        StockExchange stockExchange = getStockExchangeByName(name);
        stockExchange.getStocks().add(stock);
        updateLiveInMarketStatus(stockExchange);
        return stockExchangeRepository.save(stockExchange);
    }

    /**
     * @param name  Stock Exchange name to remove Stock
     * @param stock Stock to be removed from Stock Exchange
     * @return StockExchange updated after remove stock
     */
    @Transactional
    public StockExchange removeStockFromStockExchange(String name, Stock stock) {
        StockExchange stockExchange = getStockExchangeByName(name);
        stockExchange.getStocks().removeIf(stockFromExchange -> stockFromExchange.getId().equals(stock.getId()));
        updateLiveInMarketStatus(stockExchange);
        return stockExchangeRepository.save(stockExchange);
    }

    /**
     * Update liveInMarket attribute to true if stock exchange has more then 5 stocks else set it to false
     *
     * @param stockExchange StockExchange to check number of stocks bind to StockExchange
     */
    private void updateLiveInMarketStatus(StockExchange stockExchange) {
        stockExchange.setLiveInMarket(stockExchange.getStocks().size() >= 5);
    }

}
