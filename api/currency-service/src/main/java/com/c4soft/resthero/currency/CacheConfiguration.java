/**
 *
 */
package com.c4soft.resthero.currency;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
@EnableCaching
public class CacheConfiguration {
  @Bean
  CacheManager cacheManager() {
    return new CaffeineCacheManager();
  }
}
