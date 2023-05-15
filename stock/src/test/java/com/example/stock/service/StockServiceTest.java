package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class StockServiceTests {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        Stock stock = new Stock(1L,100L);

        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void stock_decrease(){
        stockService.decrease(1L,1L);

        // 100 - 1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();

        Assertions.assertEquals(99,stock.getQuantity());

    }

    /**
     * 레이스 컨디션 문제점 발생
     * Race Condition이란
     * 두 개 이상의 cocurrent한 프로세스(혹은 스레드)들이 하나의 자원(리소스)에 접근하기 위해 경쟁하는 상태
     * @throws InterruptedException
     */
    @Test
    public void 동시에_100개_요청() throws InterruptedException {
        int threadCnt = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCnt);

        for(int i=0; i < threadCnt; i++ ){
            executorService.submit(() -> {
                try{
                    stockService.decrease(1L,1L);
                }finally {
                    latch.countDown();
                }

            });
        }
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100 - (1*100) = 0
        Assertions.assertEquals(0L,stock.getQuantity());
    }
}
