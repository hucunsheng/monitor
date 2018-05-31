package com.ai.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by hucunsheng on 2018/4/29.
 */
public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.info(r.toString() + " is rejected");

    }
}
