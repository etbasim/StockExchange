package com.stockexchange.casestudy.stock;

import com.stockexchange.casestudy.stockexchange.StockExchange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StockControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void createStock() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        Stock stock = new Stock();
        stock.setName("AAPL");
        stock.setDescription("Apple Inc.");
        stock.setCurrentPrice(BigDecimal.valueOf(150));

        HttpEntity<Stock> request = new HttpEntity<>(stock, headers);

        ResponseEntity<Stock> response = restTemplate.postForEntity(getBaseUrl("stock"), request, Stock.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getName()).isEqualTo("AAPL");
    }

    @Test
    public void updateStockPrice() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // create stock AAPL
        Stock stock = new Stock();
        stock.setName("AAPL");
        stock.setDescription("Apple Inc.");
        stock.setCurrentPrice(BigDecimal.valueOf(150));

        HttpEntity<Stock> request = new HttpEntity<>(stock, headers);

        ResponseEntity<Stock> stockResponse = restTemplate.postForEntity(getBaseUrl("stock"), request, Stock.class);

        // add stock AAPL to NYSE
        restTemplate.exchange(getBaseUrl("stock-exchange/NYSE"), HttpMethod.PUT, new HttpEntity<>(stockResponse.getBody(), headers), StockExchange.class);

        // add stock AAPL to IMKB
        restTemplate.exchange(getBaseUrl("stock-exchange/IMKB"), HttpMethod.PUT, new HttpEntity<>(stockResponse.getBody(), headers), StockExchange.class);

        // update price of AAPL Stock
        Stock updatedStock = stockResponse.getBody();
        updatedStock.setCurrentPrice(BigDecimal.valueOf(180));
        restTemplate.exchange(getBaseUrl("stock"), HttpMethod.PUT, new HttpEntity<>(updatedStock, headers), Stock.class);

        ResponseEntity<StockExchange> nyse = restTemplate.exchange(getBaseUrl("stock-exchange/NYSE"), HttpMethod.GET, new HttpEntity<>(headers), StockExchange.class);
        List<Stock> nyseStock = nyse.getBody().getStocks().stream().filter(s -> s.getName().equals(stock.getName())).toList();

        // Verify stock price reflected the updated price for the NYSE
        assertThat(nyseStock.getFirst().getCurrentPrice()).isEqualByComparingTo(BigDecimal.valueOf(180));

        ResponseEntity<StockExchange> imkb = restTemplate.exchange(getBaseUrl("stock-exchange/IMKB"), HttpMethod.GET, new HttpEntity<>(headers), StockExchange.class);
        List<Stock> imkbStock = imkb.getBody().getStocks().stream().filter(s -> s.getName().equals(stock.getName())).toList();

        // Verify stock price reflected the updated price for the IMKB
        assertThat(imkbStock.getFirst().getCurrentPrice()).isEqualByComparingTo(BigDecimal.valueOf(180));

    }

    @Test
    public void deleteStock() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // create stock AAPL
        Stock stock = new Stock();
        stock.setName("AAPL");
        stock.setDescription("Apple Inc.");
        stock.setCurrentPrice(BigDecimal.valueOf(150));

        HttpEntity<Stock> request = new HttpEntity<>(stock, headers);

        ResponseEntity<Stock> stockResponse = restTemplate.postForEntity(getBaseUrl("stock"), request, Stock.class);

        // add stock AAPL to SSE
        restTemplate.exchange(getBaseUrl("stock-exchange/SSE"), HttpMethod.PUT, new HttpEntity<>(stockResponse.getBody(), headers), StockExchange.class);

        // add stock AAPL to LSE
        restTemplate.exchange(getBaseUrl("stock-exchange/LSE"), HttpMethod.PUT, new HttpEntity<>(stockResponse.getBody(), headers), StockExchange.class);

        // delete the stock AAPL
        restTemplate.exchange(getBaseUrl("stock"), HttpMethod.DELETE, new HttpEntity<>(stockResponse.getBody(), headers), Void.class);

        // Verify AAPL deleted from NYSE
        ResponseEntity<StockExchange> sse = restTemplate.exchange(getBaseUrl("stock-exchange/SSE"), HttpMethod.GET, new HttpEntity<>(headers), StockExchange.class);
        assertThat(sse.getBody().getStocks().size()).isEqualTo(0);

        // Verify AAPL deleted from IMKB
        ResponseEntity<StockExchange> lse = restTemplate.exchange(getBaseUrl("stock-exchange/LSE"), HttpMethod.GET, new HttpEntity<>(headers), StockExchange.class);
        assertThat(lse.getBody().getStocks().size()).isEqualByComparingTo(0);
    }

    @Test
    public void createInvalidStock() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // create stock with null name
        Stock stock = new Stock();
        stock.setName(null);
        stock.setDescription("Apple Inc.");
        stock.setCurrentPrice(BigDecimal.valueOf(150));

        HttpEntity<Stock> request = new HttpEntity<>(stock, headers);

        // Verify the response is HTTP 400 BAD_REQUEST
        ResponseEntity<Stock> stockResponse = restTemplate.postForEntity(getBaseUrl("stock"), request, Stock.class);
        assertThat(stockResponse.getStatusCode().value()).isEqualTo(400);
    }

    private String getBaseUrl(String url) {
        return "http://localhost:" + port + "/api/v1/" + url;
    }
}
