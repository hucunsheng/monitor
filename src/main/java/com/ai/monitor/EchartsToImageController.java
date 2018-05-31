package com.ai.monitor;

import com.ai.base.MonitorCofParaDAO;
import com.ai.util.HtmlToImageMain;
import com.ai.util.PageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 通过获取监控配置表进行查询待处理任务，执行超过阀值的进行告警。
 */
@RestController
public class EchartsToImageController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private MonitorCofParaDAO monitorDAO;


    //@Scheduled(cron = "0/10 * * * * *")
	public void timer() {
		logger.info("EchartsToImageController任务执行中");
		try {
			HtmlToImageMain.HtmlImageGeneratorFile("http://localhost:8889/monitor/echarts.html");
		} catch (Exception e) {
			logger.info("HtmlToImageERROR",e);
		}

	}


	public static void main(String[] args) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime time = LocalDateTime.now();
		String localTime = df.format(time);
		LocalDateTime ldt = LocalDateTime.parse("2017-09-28 17:07:05",df);
		System.out.println("LocalDateTime转成String类型的时间："+localTime);
		System.out.println("String类型的时间转成LocalDateTime："+ldt);

		int min = 5;
		df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		String nowStr = LocalDateTime.now().plusMinutes(-min).format(df);
		System.out.println("nowStr："+nowStr);
//        System.out.println("args = [" + new EchartsToImageController().isSend("09:10~15:00") + "]");
        LocalDateTime nowDT = LocalDateTime.now();
        System.out.println("nowDT："+nowDT);
        LocalDateTime s = nowDT.plusMinutes(-10);
        System.out.println("s："+s);
    }
}
