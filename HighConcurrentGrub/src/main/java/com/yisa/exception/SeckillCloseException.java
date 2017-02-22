package com.yisa.exception;

import com.yisa.exception.SeckillException;

/**
 * Created by Yisa on 2017/2/22.
 */
public class SeckillCloseException extends SeckillException {
    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
