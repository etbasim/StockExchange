package com.stockexchange.casestudy.stockexchange;

import com.stockexchange.casestudy.stock.Stock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/stock-exchange")
public class StockExchangeController {

    private final StockExchangeService stockExchangeService;

    public StockExchangeController(StockExchangeService stockExchangeService) {
        this.stockExchangeService = stockExchangeService;
    }

    @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StockExchange> getStockExchange(@PathVariable String name) {
        return ResponseEntity.ok(stockExchangeService.getStockExchangeByName(name));
    }

    @PutMapping("/{name}")
    public ResponseEntity<StockExchange> addStockToStockExchange(@PathVariable String name, @RequestBody Stock stock) {
        return ResponseEntity.ok(stockExchangeService.addStockToStockExchange(name, stock));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<StockExchange> removeStockFromStockExchange(@PathVariable String name, @RequestBody Stock stock) {
        return ResponseEntity.ok(stockExchangeService.removeStockFromStockExchange(name, stock));
    }

}
