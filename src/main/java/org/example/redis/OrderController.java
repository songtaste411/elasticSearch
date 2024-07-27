package org.example.redis;

import org.redisson.api.RDelayedQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private RedissonOrderDelayQueue redissonOrderDelayQueue;

    @PostMapping("/addOrder")
    public String addOrder(@RequestParam String orderId, @RequestParam long delaySeconds) {
        RDelayedQueue<String> delayedQueue = redissonOrderDelayQueue.initQueue();
        redissonOrderDelayQueue.addTaskToDelayQueue(orderId, delayedQueue, delaySeconds);
        return "Order added to delay queue";
    }
}