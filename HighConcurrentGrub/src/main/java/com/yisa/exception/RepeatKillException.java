package com.yisa.exception;

import com.yisa.exception.SeckillException;

/**
 * Created by Yisa on 2017/2/22.
 */
public class RepeatKillException extends SeckillException {
    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
