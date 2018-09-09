package com.imooc.miaosha.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Auther: cmy
 * @Date: 2018/9/1 20:11
 * @Description:
 */
//@Component
@Slf4j
public class RedisLock {
    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 加锁
     * @param key
     * @param value 当前时间+超时时间
     * @return
     */

    public boolean lock(String key,String value){
        if(redisTemplate.opsForValue().setIfAbsent(key,value)){
            return true;
        }
        //currentValue=A  两个线程value都是B  其中一个线程拿到锁
        String currentValue=redisTemplate.opsForValue().get(key);
        //如果锁过期
        if (!StringUtils.isEmpty(currentValue)&&Long.parseLong(currentValue)<System.currentTimeMillis()){
            //获取上一个锁的时间（第一个线程拿到，oldvalue=A 第二个则拿到oldvalue=B）
            String oldValue=redisTemplate.opsForValue().getAndSet(key,value);
            if (!StringUtils.isEmpty(oldValue)&&oldValue.equals(currentValue)){
                return  true;
            }
        }
        return false;
    }


    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key,String value){
        try {
            String currentValue=redisTemplate.opsForValue().get(key);
            if(!StringUtils.isEmpty(currentValue)&&currentValue.equals(value)){
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("【redis分布式锁】 解锁异常，{}",e);
        }

    }



}
