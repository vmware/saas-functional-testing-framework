/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.redis;

import java.io.Serializable;
import java.time.Duration;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.vmware.test.functional.saas.LocalServiceEndpoint;

/**
 * Redis Template Factory.
 * Provides local {@link RedisTemplate}. To be used by Functional tests.
 */
public class RedisTemplateFactory implements FactoryBean<RedisTemplate<String, ? extends Serializable>> {

    private static final int REDIS_READ_TIMEOUT_MILLIS = 2000;
    private static final int REDIS_TIMEOUT_MILLIS = 2000;

    private final LocalServiceEndpoint redisEndpoint;

    public RedisTemplateFactory(final LocalServiceEndpoint redisEndpoint) {
        this.redisEndpoint = redisEndpoint;
    }

    @Override
    public RedisTemplate<String, ? extends Serializable> getObject() {
        final RedisTemplate<String, ? extends Serializable> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Override
    public Class<?> getObjectType() {
        return RedisTemplate.class;
    }

    private RedisConnectionFactory redisConnectionFactory() {
        final JedisClientConfiguration.JedisClientConfigurationBuilder jccBuilder = JedisClientConfiguration.builder()
                .readTimeout(Duration.ofMillis(REDIS_READ_TIMEOUT_MILLIS))
                .connectTimeout(Duration.ofMillis(REDIS_TIMEOUT_MILLIS));

        final RedisStandaloneConfiguration rsc = new RedisStandaloneConfiguration();
        rsc.setHostName(this.redisEndpoint.getHostName());
        rsc.setPort(this.redisEndpoint.getPort());

        final JedisConnectionFactory jcf = new JedisConnectionFactory(rsc, jccBuilder.build());
        jcf.afterPropertiesSet();
        return jcf;
    }
}
