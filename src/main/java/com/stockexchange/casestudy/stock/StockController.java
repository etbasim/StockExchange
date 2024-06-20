package com.stockexchange.casestudy.stock;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        return ResponseEntity.ok(stockService.createStock(stock));
    }

    @PutMapping
    public ResponseEntity<Stock> updateStockPrice(@RequestBody Stock stock) {
        return ResponseEntity.ok(stockService.updateStockPrice(stock));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteStock(@RequestBody Stock stock) {
        stockService.deleteStock(stock);
        return ResponseEntity.noContent().build();
    }

}
