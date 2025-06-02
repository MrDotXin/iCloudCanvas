package com.mrdotxin.icloudcanvas;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mrdotxin.icloudcanvas.model.entity.Picture;
import com.mrdotxin.icloudcanvas.service.PictureService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ICloudCanvasApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(ICloudCanvasApplicationTests.class);
    @Resource
    private PictureService pictureService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private Cache<String, Object> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    @Test
    void contextLoads() {
    }

    @Test
    void testRedis() {
        Picture picture = pictureService.getById(1);
        for (int i = 0; i < 500; i++) {
            String key = String.format("icc:picture:%d:%d", picture.getId(), i);

            String json = JSONUtil.toJsonStr(picture);
            redisTemplate.opsForValue().set(key, json);
        }
    }

    void loadCaffeine() {
        Picture picture = pictureService.getById(1);
        for (int i = 0; i < 500; i++) {
            String key = String.format("icc:picture:%d:%d", picture.getId(), i);
            LOCAL_CACHE.put(key, picture);
        }

        ConcurrentMap<String, Object> map = LOCAL_CACHE.asMap();
        log.info("local caches {} in total", map.size());
    }

    @Test
    void testDataBaseInsert() {
        Picture picture = pictureService.getById(1);
        picture.setId(null);
        for (int i = 0; i < 485; i++) {
            pictureService.save(picture);
            picture.setId(null);
        }
    }

    @Test
    void testReading() {

        loadCaffeine();

        StopWatch stopWatch = new StopWatch();

        // 读redis
        stopWatch.start();
        for (int i = 0; i < 50000; i++) {
            int random = (int) (Math.random() * 500);
            String key = String.format("icc:picture:%d:%d", 1, random);
            redisTemplate.opsForValue().get(key);
        }
        stopWatch.stop();

        Long redisMs = stopWatch.getLastTaskTimeMillis();

        stopWatch.start();
        for (int i = 0; i < 50000; i++) {
            int random = (int) (Math.random() * 500);
            String key = String.format("icc:picture:%d:%d", 1, i);
            Object object = LOCAL_CACHE.getIfPresent(key);
        }
        stopWatch.stop();
        Long localMs = stopWatch.getLastTaskTimeMillis();

        stopWatch.start();
        for (int i = 0; i < 50000; i++) {
            int random = (int) (Math.random() * 500);
            Picture picture = pictureService.getById(random);
        }
        stopWatch.stop();

        log.info("with local cache {}ms", localMs);
        log.info("with redis {}ms", redisMs);
        log.info("with databases {}ms", stopWatch.getLastTaskTimeMillis());
    }

}
