package com.tian.tblong.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.tian.tblong.vo.RedisInfoVo;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.DefaultLettucePool;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Copyright: Copyright (c) 2019 xxx有限公司
 * @ClassName: RedisConfig.java
 * @Description: Redis换成初始化配置
 * @version: v1.0.0
 * @author: tblong
 * @date: 2019年10月10日 下午4:28:50 
 *
 * Modification History:
 * Date           Author          Version            Description
 *---------------------------------------------------------*
 * 2019年10月10日     tblong           v1.0.0               新建
 */
@Configuration
@EnableCaching // 开启缓存支持
public class RedisConfig extends CachingConfigurerSupport {

	//redis参数配置信息
	private RedisInfoVo redisVo = new RedisInfoVo();
	
	@Override
	@Bean
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuffer sb = new StringBuffer();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					//由于参数可能不同, 缓存的key也需要不一样
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};
	}
	
	/**
	 * 缓存管理器
	 */
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
		//设置缓存过期时间 
		rcm.setUsePrefix(false);
		rcm.setDefaultExpiration(600);  //默认策略 ，10分钟
		//配置不同缓存区域的过期时间，应用使用@Cacheable注解，设置value属性为以下指定的key，缓存的过期时间即为以下key值的过期时间
		Map<String, Long> expires = new HashMap<String, Long>();
		expires.put("business_data", 1800L);      //指定key策略，30分钟
		expires.put("system_data", 1800L);        //指定key策略，30分钟
		expires.put("common_data", 1800L);        //指定key策略，30分钟
		expires.put("test_data", 60L);            //指定key策略，30分钟
		
		rcm.setExpires(expires);
		redisTemplate.getClientList();
		return rcm;
	}
	
	/**
	 * RedisTemplate配置
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
		// 设置序列化
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(
				Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	    om.enableDefaultTyping(DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		// 配置redisTemplate
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(lettuceConnectionFactory);
		RedisSerializer<?> stringSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringSerializer);// key序列化
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);// value序列化
		redisTemplate.setHashKeySerializer(stringSerializer);// Hash key序列化
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);// Hash value序列化
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	/**
	 * @Function: RedisConfig.java
	 * @Description: 连接工厂配置
	 * @param:描述1描述
	 * @return：返回结果描述
	 * @throws：异常描述
	 *
	 * @version: v1.0.0
	 * @author: tblong
	 * @date: 2019年10月10日 上午10:40:54 
	 * Modification History:
	 * Date           Author          Version            Description
	 *---------------------------------------------------------*
	 * 2019年10月10日     tblong           v1.0.0               新建
	 */
	@Bean    
	public LettuceConnectionFactory lettuceConnectionFactory(DefaultLettucePool defaultLettucePool) {
		LettuceConnectionFactory factory = new LettuceConnectionFactory(defaultLettucePool);
		factory.setValidateConnection(true);
		return factory;
	}

	
	/**
	 * @Function: RedisConfig.java
	 * @Description: 连接池配置
	 * @param:描述1描述
	 * @return：返回结果描述
	 * @throws：异常描述
	 *
	 * @version: v1.0.0
	 * @author: tblong
	 * @date: 2019年10月10日 下午3:19:17 
	 * Modification History:
	 * Date           Author          Version            Description
	 *---------------------------------------------------------*
	 * 2019年10月10日     tblong           v1.0.0               新建
	 */
	@Bean
    public DefaultLettucePool getDefaultLettucePool(RedisSentinelConfiguration redisSentinelConfiguration, GenericObjectPoolConfig poolConfig) {
		if("single".equalsIgnoreCase(redisVo.getDeployType())){
			//redis单机模式配置
			DefaultLettucePool defaultLettucePool = new DefaultLettucePool(redisVo.getHost(), redisVo.getPort(), poolConfig);
			if(redisVo.getPassword() != null && !"".equals(redisVo.getPassword())){
				defaultLettucePool.setPassword(redisVo.getPassword());
			}
			return defaultLettucePool;
		}else if("sentinel".equalsIgnoreCase(redisVo.getDeployType())){
			////redis主从哨兵模式配置
			DefaultLettucePool defaultLettucePool = new DefaultLettucePool(redisSentinelConfiguration);
			defaultLettucePool.setPoolConfig(poolConfig);
	        defaultLettucePool.afterPropertiesSet();
	        return defaultLettucePool;
		}else{
			throw new RuntimeException("redis deploy type config error !!!, please check redis.properties.", new Throwable());
		}
		
    }
	
	/**
	 * @Function: RedisConfig.java
	 * @Description: 配置哨兵集群信息 master和host:ip
	 * @param:描述1描述
	 * @return：返回结果描述
	 * @throws：异常描述
	 *
	 * @version: v1.0.0
	 * @author: tblong
	 * @date: 2019年10月10日 上午10:25:56 
	 * Modification History:
	 * Date           Author          Version            Description
	 *---------------------------------------------------------*
	 * 2019年10月10日     tblong           v1.0.0               新建
	 */
	@Bean
	public RedisSentinelConfiguration redisSentinelConfiguration() {
		return new RedisSentinelConfiguration(redisVo.getMaster(), redisVo.getHosts());
	}

	/**
	 * @Function: RedisConfig.java
	 * @Description: 线程池配置参数设置
	 * @param:描述1描述
	 * @return：返回结果描述
	 * @throws：异常描述
	 *
	 * @version: v1.0.0
	 * @author: tblong
	 * @date: 2019年10月10日 上午10:40:22 
	 * Modification History:
	 * Date           Author          Version            Description
	 *---------------------------------------------------------*
	 * 2019年10月10日     tblong           v1.0.0               新建
	 */
	@Bean
	public GenericObjectPoolConfig genericObjectPoolConfig() {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxIdle(redisVo.getMaxIdle());
		poolConfig.setMinIdle(redisVo.getMinIdle());
		poolConfig.setMaxTotal(redisVo.getMaxTotal());
		//todo 其他配置        
		return poolConfig;    
	}

}