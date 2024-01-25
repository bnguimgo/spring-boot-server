package com.bnguimgo.springbootrestserver.cache;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {

    //1er type de Cache: Cache Spring ==> spring-boot-starter-cache
    @Bean
    public CacheManager cacheManager() {//This is the defaut CacheManager
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("userAuthCache"),
                new ConcurrentMapCache("userCache", true),
                new ConcurrentMapCache("otherCache")));
        return cacheManager;
    }

    //2ème type de Cache: Cache Redis ==> spring-boot-starter-data-redis
    //https://www.baeldung.com/spring-boot-redis-cache
    /**
     * This Redis cache manager builder only work for Springboot 2.4.3 or later
     * @return
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("userAuthCacheRedis",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues())
                .withCacheConfiguration("userCacheRedis",
                        RedisCacheConfiguration.defaultCacheConfig().
                            entryTtl(Duration.ofMinutes(5))
                            .disableCachingNullValues());
    }
}