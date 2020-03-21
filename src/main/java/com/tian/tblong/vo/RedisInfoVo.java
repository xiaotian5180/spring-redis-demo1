package com.tian.tblong.vo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tian.tblong.prop.RedisProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RedisInfoVo {

	public RedisInfoVo(){
		getConfigResource();
	}
	
	private String deployType;
	
	private String host;
	private int port;
	private String password;
	
	private int maxTotal;
	private int maxIdle;
	private int minIdle;
	
	private String master;
    private Set<String> hosts; 
	
	public void getConfigResource(){
		loadBaseConfigInfo();     //加载基础参数配置
		loadPropEvnConfigInfo();  //加载环境参数配置
	}
	/**
	 * 加载基础参数配置
	 */
	public void loadBaseConfigInfo(){
    	int maxTotal = RedisProperties.getInt("lettuce.pool.maxTotal");
    	int maxIdle = RedisProperties.getInt("lettuce.pool.maxIdle");
    	int minIdle = RedisProperties.getInt("lettuce.pool.minIdle");

    	this.maxTotal = maxTotal;
    	this.maxIdle = maxIdle;
    	this.minIdle = minIdle;
	}
	
	/**
	 * 加载环境参数配置
	 */
	public void loadPropEvnConfigInfo(){
		String deploy = RedisProperties.getString("redis.deploy");
    	String host = RedisProperties.getString("redis.host");
    	int port = RedisProperties.getInt("redis.port");
    	String password = RedisProperties.getString("redis.password");
    	
    	String master = RedisProperties.getString("lettuce.sentinel.master");
    	String sentinelNodes = RedisProperties.getString("lettuce.sentinel.nodes");

    	this.deployType = deploy;
    	this.host = host;
    	this.port = port;
    	this.password = password;
    	
    	this.master = master;
    	
    	if(sentinelNodes != null && !"".equals(sentinelNodes.trim())){
    		String[] sentinels = sentinelNodes.split(",");
    		hosts = new HashSet<>();
        	hosts.addAll(Arrays.asList(sentinels));
    	}
    	if("single".equals(deployType)){
			log.info("load redis single conn info from properties : host->"  + this.host + ", port->" + this.port + ", password->" + this.password);
		}else if("sentinel".equals(deployType)){
			log.info("load redis sentinel conn info from properties : master->"  + this.master + ", hosts->" + sentinelNodes);
		}
	}
	
}