package com.stockexchange.casestudy;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SimultaneousAccessTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testConcurrentAccess() throws InterruptedException, ExecutionException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "password");

        // First, create the stock
        Stock stock = new Stock();
        stock.setName("Stock1");
        stock.setDescription("Stock1");
        stock.setCurrentPrice(BigDecimal.valueOf(150));
        ResponseEntity<Stock> stockResponse = restTemplate.postForEntity(getBaseUrl(), new HttpEntity<>(stock, headers), Stock.class);

        // Prepare tasks for concurrent execution
        Callable<ResponseEntity<Stock>> updateTask = () -> {
            //Thread.sleep(1000);
            return restTemplate.exchange(getBaseUrl(), HttpMethod.PUT, new HttpEntity<>(stockResponse.getBody(), headers), Stock.class);
        };

        Callable<ResponseEntity<Void>> deleteTask = () -> restTemplate.exchange(getBaseUrl(), HttpMethod.DELETE, new HttpEntity<>(stockResponse.getBody(), headers), Void.class);

        // Submit update and delete tasks
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            Future<ResponseEntity<Stock>> futureUpdate = executorService.submit(updateTask);
            Future<ResponseEntity<Void>> futureDelete = executorService.submit(deleteTask);

            // Get results from related Future
            ResponseEntity<Stock> updateResponse = futureUpdate.get();
            ResponseEntity<Void> deleteResponse = futureDelete.get();

            // Assert the results, depends on the process order updated response may return 400 bad request if first delete
            // stock has been executed
            assertThat(updateResponse.getStatusCode().value()).isIn(200, 400); // The update may succeed or fail depending on timing
            assertThat(deleteResponse.getStatusCode().value()).isEqualTo(204);
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/stock";
    }
}
