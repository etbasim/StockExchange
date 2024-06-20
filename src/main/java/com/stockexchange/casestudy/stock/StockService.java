package com.stockexchange.casestudy.stock;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * @param id Stock id to find stock information
     * @return Stock for the given stock id
     * throws {@link IllegalArgumentException} if stock can not be found
     */
    public Stock getStockById(Long id) {
        return stockRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Stock not found!!!"));
    }

    /**
     * @param stock Stock to create
     * @return Stock created Stock object
     * throws {@link jakarta.validation.ConstraintViolationException} if the stock is not valid
     */
    public Stock createStock(@Valid Stock stock) {
        return stockRepository.save(stock);
    }

    /**
     * @param stock Stock to update currentPrice attribute
     * @return Stock updated Stock object
     * throws {@link jakarta.validation.ConstraintViolationException} if the stock is not valid
     */
    @Transactional
    public Stock updateStockPrice(@Valid Stock stock) {
        Stock stockToUpdate = getStockById(stock.getId());
        stockToUpdate.setCurrentPrice(stock.getCurrentPrice());
        return stockRepository.save(stockToUpdate);
    }

    /**
     * First, delete the stock relations with stock exchange and then delete the stock
     *
     * @param stock Stock to delete
     */
    @Transactional
    public void deleteStock(Stock stock) {
        Stock stockToDelete = getStockById(stock.getId());
        stockToDelete.getStockExchanges().forEach(stockExchange -> {
            stockExchange.getStocks().remove(stockToDelete);
        });
        stockRepository.deleteById(stock.getId());
    }
}