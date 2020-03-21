package com.tian.tblong.prop;

import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Copyright: Copyright (c) 2019 xxx有限公司
 * @ClassName: RedisProperties.java
 * @Description: Redis属性配置类
 * @version: v1.0.0
 * @author: tblong
 * @date: 2019年10月10日 上午9:23:40 
 *
 * Modification History:
 * Date           Author          Version            Description
 *---------------------------------------------------------*
 * 2019年10月10日     tblong           v1.0.0               新建
 */
@Component
@Data
@Slf4j
public class RedisProperties {

    private static ResourceBundle bundle;

    static {
        bundle = ResourceBundle.getBundle("redis");
        log.debug("load redis.properties and init...");
    }

    /**
     * 重新加载配置文件
     */
    public void reloadProperties(){
    	bundle = ResourceBundle.getBundle("redis");
        log.debug("reload redis.properties ...");
    }
    
    
    /**
     * 获取String类型配置参数
     * @param key
     * @return
     */
    public static String getString(String key) {
    	if(bundle.containsKey(key)){
    		return bundle.getString(key);
    	}
        return null;
    }
    
    /**
     * 获取Int类型配置参数
     * @param key
     * @return
     */
    public static Integer getInt(String key) {
    	String value = getString(key);
    	if(value != null && !"".equals(value.trim())){
    		return Integer.parseInt(value.trim());
    	}
        return null;
    }
    
    /**
     * 获取Boolean类型配置参数
     * @param key
     * @return
     */
    public static boolean getBoolean(String key) {
    	String value = getString(key);
    	if(value != null && !"".equals(value.trim())){
    		return Boolean.parseBoolean(value.trim());
    	}
        return false;
    }
}