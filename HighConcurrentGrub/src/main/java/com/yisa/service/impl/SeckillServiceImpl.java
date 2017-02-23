package com.yisa.service.impl;

import com.yisa.dao.SeckillDao;
import com.yisa.dao.SuccessKilledDao;
import com.yisa.dto.Exposer;
import com.yisa.dto.SeckillExecution;
import com.yisa.entity.Seckill;
import com.yisa.entity.SuccessKilled;
import com.yisa.enums.SeckillStatEnum;
import com.yisa.exception.RepeatKillException;
import com.yisa.exception.SeckillCloseException;
import com.yisa.exception.SeckillException;
import com.yisa.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by Yisa on 2017/2/22.
 */
@Service
public class SeckillServiceImpl implements SeckillService {


    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //日志对象
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //加入一个混淆字符串(秒杀接口)的salt，
    private final String salt="jk~nkcasnc";

    //依赖注入
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    private String getMD5(long seckillId)
    {
        String base=seckillId+"/"+salt;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }


    public List<Seckill> getSeckillList() {

        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill=seckillDao.queryById(seckillId);
        if (seckill==null) //说明查不到这个秒杀产品的记录
        {
            return new Exposer(false,seckillId);
        }

        //若是秒杀未开启
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        //系统当前时间
        Date nowTime=new Date();
        if (startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime())
        {
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        //秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5==null||!md5.equals(getMD5(seckillId)))
        {
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime=new Date();
        try{
            //减库存
            int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
            if (updateCount<=0)
            {
                //没有更新库存记录，说明秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //否则更新了库存，秒杀成功,增加明细
                int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
                //看是否该明细被重复插入，即用户是否重复秒杀
                if (insertCount<=0)
                {
                    throw new RepeatKillException("seckill repeated");
                }else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }

        }catch (SeckillCloseException e1)
        {
            throw e1;
        }catch (RepeatKillException e2)
        {
            throw e2;
        }catch (Exception e)
        {
            logger.error(e.getMessage(),e);
            //编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }
    }
}
