package org.example.redis;

import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class RedissonOrderDelayQueue {



      @Autowired
      RedissonClient redisson;

    public RDelayedQueue<String> initQueue() {

        RBlockingDeque<String> blockingDeque = redisson.getBlockingDeque("orderQueue");
        RDelayedQueue<String> delayedQueue = redisson.getDelayedQueue(blockingDeque);

        return delayedQueue;
    }

    public  void addTaskToDelayQueue(String orderId, RDelayedQueue<String> delayedQueue,long seconds) {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + orderId+seconds+"秒，添加任务到延时队列里面");
        delayedQueue.offer(orderId, seconds, TimeUnit.SECONDS);
    }


    public String getOrderFromDelayQueue() throws InterruptedException {
            RBlockingDeque<String> blockingDeque = redisson.getBlockingDeque("orderQueue");
            RDelayedQueue<String> delayedQueue = redisson.getDelayedQueue(blockingDeque);
            String orderId = blockingDeque.take();
            return orderId;
      }
    @PostConstruct
    public void listenDelayQueue() {
        RDelayedQueue<String> delayedQueue = initQueue();

        new Thread(() -> {
            while (true) {
                try {
                    String orderId = getOrderFromDelayQueue();
                    cancelOrder(orderId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void cancelOrder(String orderId) {
        // Logic to cancel the order
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"Canceling order: " + orderId);
    }

}

