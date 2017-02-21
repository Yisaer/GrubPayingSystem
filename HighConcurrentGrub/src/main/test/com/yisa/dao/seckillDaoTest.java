package com.yisa.dao;

import com.yisa.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Yisa on 2017/2/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class seckillDaoTest {
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void reduceNumber() throws Exception {
        long seckill = 1000;
        Date date = new Date();
        int updateCount = seckillDao.reduceNumber(seckill,date);
        System.out.println(updateCount);
    }

    @Test
    public void queryById() throws Exception {
        long seckillID =1000;
        Seckill seckill = seckillDao.queryById(seckillID);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() throws Exception {
        List<Seckill> seckills = seckillDao.queryAll(0,100);
        for(Seckill s : seckills){
            System.out.println(s);
        }
    }

}