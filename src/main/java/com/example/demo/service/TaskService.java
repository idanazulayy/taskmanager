package com.example.demo.service;

import com.example.demo.model.Process;
import com.example.demo.model.ProcessStatus;
import com.example.demo.model.TaskProgressDTO;
import com.example.demo.model.TasksDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int port;

    private final RedisTemplate<String, Object> redisTemplate;

    public UUID createTask(TasksDTO tasksDTO) {
        UUID taskId = UUID.randomUUID();
        List<Process> subProcesses = tasksDTO.getSubProcess();

        // עבור כל תהליך ניצור אותו באופן אסינכרוני
        for (Process process : subProcesses) {
            process.setProcessId(UUID.randomUUID());

            // TTL אקראי עבור כל תהליך
            int ttl = (int) (Math.random() * 30) + 10;
            process.setTimeOfLiveInSeconds(ttl);

            // שמירה ב-Redis
            String redisKey = getRedisKey(taskId, process.getProcessId());
            log.info("redisKey: " + redisKey);
            redisTemplate.opsForValue().set(redisKey, process, ttl, TimeUnit.SECONDS);

            // הפעלת התהליך אסינכרונית
            runAsyncProcess(taskId, process);
        }

        return taskId;
    }

    @Async(value = "taskExecutor")
    public CompletableFuture<Void> runAsyncProcess(UUID taskId, Process process) {
        log.info("🔄 Starting async process [{}] for task [{}] (TTL={}s)", process.getProcessName(), taskId, process.getTimeOfLiveInSeconds());
        return CompletableFuture.completedFuture(null);
    }

    private String getRedisKey(UUID taskId, UUID processId) {
        return "task" + taskId + "process" + processId;
    }

    public TaskProgressDTO getTaskProgress(UUID taskId) {
        // Connect to Redis
        Jedis jedis = new Jedis(redisHost, port); // Modify with your Redis server details

        // Retrieve keys that match the pattern "task*"
        Set<String> keys = new HashSet<>();
        Set<byte[]> rawKeys = jedis.keys(("*" + taskId + "*").getBytes(StandardCharsets.UTF_8));

        for (byte[] keyBytes : rawKeys) {
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            keys.add(key.substring(7,key.length()));
        }

        // Close the connection
        jedis.close();


//    Set<String> keys = redisTemplate.keys("task:" + taskId + ":process*");
        if (keys == null || keys.isEmpty()) {
            log.error("No processes found for task ID: {}", taskId);
            return null;
        }

        List<ProcessStatus> statuses = new ArrayList<>();
        double totalPercent = 0;

        for (String key : keys) {
            Process process = (Process) redisTemplate.opsForValue().get(key);
            if (process == null) continue;

            // קבלת TTL הנותר לכל תהליך
            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds == null || ttlSeconds < 0) ttlSeconds = 0L;

            // חישוב אחוז ההשלמה של התהליך
            int percentComplete = (int) (100 - ((double) ttlSeconds / process.getTimeOfLiveInSeconds()) * 100);
            percentComplete = Math.min(percentComplete, 100);

            statuses.add(new ProcessStatus(process.getProcessName(), ttlSeconds.intValue(), percentComplete));
            totalPercent += percentComplete;
        }

        double averageCompletion = totalPercent / statuses.size();

        return new TaskProgressDTO(statuses, averageCompletion);
    }
}
