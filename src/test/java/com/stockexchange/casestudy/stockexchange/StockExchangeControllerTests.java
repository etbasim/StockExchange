package com.stockexchange.casestudy.stockexchange;

import com.stockexchange.casestudy.stock.Stock;
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
class StockExchangeControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getStockExchange() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // Get the stock exchange NYSE
        String getUrl = getBaseUrl("stock-exchange") + "/NYSE";
        ResponseEntity<StockExchange> response = restTemplate.exchange(getUrl, HttpMethod.GET, new HttpEntity<>(headers), StockExchange.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getName()).isEqualTo("NYSE");
    }

    @Test
    public void getStockExchangeUnAuthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("user", "password");

        // Get the stock exchange NYSE with unauthorized user
        String getUrl = getBaseUrl("stock-exchange") + "/NYSE";
        ResponseEntity<StockExchange> response = restTemplate.exchange(getUrl, HttpMethod.GET, new HttpEntity<>(headers), StockExchange.class);

        // Verify response is HTTP 403
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    public void addStockToStockExchange() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // First, create the stock AAPL
        Stock stock = new Stock();
        stock.setName("AAPL");
        stock.setDescription("Apple Inc.");
        stock.setCurrentPrice(BigDecimal.valueOf(150));

        HttpEntity<Stock> stockRequest = new HttpEntity<>(stock, headers);
        restTemplate.postForEntity(getBaseUrl("stock"), stockRequest, Stock.class);

        // Add stock AAPL to stock exchange FSE
        String addUrl = getBaseUrl("stock-exchange") + "/FSE";
        HttpEntity<Stock> addRequest = new HttpEntity<>(stock, headers);
        ResponseEntity<StockExchange> response = restTemplate.exchange(addUrl, HttpMethod.PUT, addRequest, StockExchange.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStocks().size()).isEqualTo(1);
    }

    @Test
    public void addFiveStockToStockExchangeAndDeleteOne() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // First, create 5 stocks
        Stock stock = new Stock();
        stock.setName("Stock1");
        stock.setDescription("Stock1");
        stock.setCurrentPrice(BigDecimal.valueOf(150));

        Stock stock2 = new Stock();
        stock2.setName("Stock2");
        stock2.setDescription("Stock2");
        stock2.setCurrentPrice(BigDecimal.valueOf(200));

        Stock stock3 = new Stock();
        stock3.setName("Stock3");
        stock3.setDescription("Stock3");
        stock3.setCurrentPrice(BigDecimal.valueOf(100));

        Stock stock4 = new Stock();
        stock4.setName("Stock4");
        stock4.setDescription("Stock4");
        stock4.setCurrentPrice(BigDecimal.valueOf(104));

        Stock stock5 = new Stock();
        stock5.setName("Stock5");
        stock5.setDescription("Stock5");
        stock5.setCurrentPrice(BigDecimal.valueOf(105));

        ResponseEntity<Stock> stock1Response = restTemplate.postForEntity(getBaseUrl("stock"), new HttpEntity<>(stock, headers), Stock.class);
        ResponseEntity<Stock> stock2Response = restTemplate.postForEntity(getBaseUrl("stock"), new HttpEntity<>(stock2, headers), Stock.class);
        ResponseEntity<Stock> stock3Response = restTemplate.postForEntity(getBaseUrl("stock"), new HttpEntity<>(stock3, headers), Stock.class);
        ResponseEntity<Stock> stock4Response = restTemplate.postForEntity(getBaseUrl("stock"), new HttpEntity<>(stock4, headers), Stock.class);
        ResponseEntity<Stock> stock5Response = restTemplate.postForEntity(getBaseUrl("stock"), new HttpEntity<>(stock5, headers), Stock.class);

        // Add 5 created stocks to stock exchange NYSE
        String nyseUrl = getBaseUrl("stock-exchange") + "/NYSE";
        restTemplate.exchange(nyseUrl, HttpMethod.PUT, new HttpEntity<>(stock1Response.getBody(), headers), StockExchange.class);
        restTemplate.exchange(nyseUrl, HttpMethod.PUT, new HttpEntity<>(stock2Response.getBody(), headers), StockExchange.class);
        restTemplate.exchange(nyseUrl, HttpMethod.PUT, new HttpEntity<>(stock3Response.getBody(), headers), StockExchange.class);
        restTemplate.exchange(nyseUrl, HttpMethod.PUT, new HttpEntity<>(stock4Response.getBody(), headers), StockExchange.class);
        ResponseEntity<StockExchange> responseNYSE = restTemplate.exchange(nyseUrl, HttpMethod.PUT, new HttpEntity<>(stock5Response.getBody(), headers), StockExchange.class);

        // Verify NYSE has 5 stocks and liveInMarket is true
        assertThat(responseNYSE.getStatusCode().value()).isEqualTo(200);
        assertThat(responseNYSE.getBody().getStocks().size()).isEqualTo(5);
        assertThat(responseNYSE.getBody().isLiveInMarket()).isEqualTo(true);

        // Delete Stock1 from NYSE
        List<Stock> stockList = responseNYSE.getBody().getStocks().stream().filter(s -> s.getName().equals("Stock1")).toList();

        // Verify NYSE has 4 stocks and liveInMarket is false
        ResponseEntity<StockExchange> updatedResponseNYSE = restTemplate.exchange(nyseUrl, HttpMethod.DELETE, new HttpEntity<>(stockList.getFirst(), headers), StockExchange.class);
        assertThat(updatedResponseNYSE.getStatusCode().value()).isEqualTo(200);
        assertThat(updatedResponseNYSE.getBody().getStocks().size()).isEqualTo(4);
        assertThat(updatedResponseNYSE.getBody().isLiveInMarket()).isEqualTo(false);
    }

    private String getBaseUrl(String url) {
        return "http://localhost:" + port + "/api/v1/" + url;
    }

}
