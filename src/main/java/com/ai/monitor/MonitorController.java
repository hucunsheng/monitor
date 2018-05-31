package com.ai.monitor;

import ch.qos.logback.core.net.server.ConcurrentServerRunner;
import com.ai.base.MonitorCofParaDAO;
import com.ai.base.MySqlPullDao;
import com.ai.util.PageData;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 通过获取监控配置表进行查询待处理任务，执行超过阀值的进行告警。
 */
@RestController
//public class MonitorController implements ApplicationRunner {
public class MonitorController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private MonitorCofParaDAO monitorDAO;

	private LinkedBlockingQueue<PageData> queue = new LinkedBlockingQueue<PageData>();
	private static ThreadFactory factory = new ThreadFactory() {
		private final Logger logger = LoggerFactory.getLogger(getClass());

		private final AtomicInteger integer = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			String name = "Consumer: " + integer.getAndIncrement();
			logger.info("ThreadFactory==="+name);
			return new Thread(r, name);
		}
	};

	private static ThreadFactory factoryProducer = new ThreadFactory() {
		private final Logger logger = LoggerFactory.getLogger(getClass());

		private final AtomicInteger integer = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			String name = "Producer: " + integer.getAndIncrement();
			logger.info("ThreadFactory==="+name);
			return new Thread(r, name);
		}
	};
    @Scheduled(cron = "0 0/5 * * * *")
	public void timer() {
		logger.info("timer任务执行中");
		RejectedExecutionHandlerImpl rejectionHandlerProducer = new RejectedExecutionHandlerImpl();
		ThreadPoolExecutor producerThreadPoolExecutor = new ThreadPoolExecutor(1,2,10,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(1),factoryProducer,rejectionHandlerProducer);
		producerThreadPoolExecutor.execute(new Producer());
        producerThreadPoolExecutor.shutdown();



		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
		//Get the ThreadFactory implementation to use
		//ThreadFactory threadFactory = Executors.defaultThreadFactory();
		//creating the ThreadPoolExecutor
		//启动生产线程，查询符合执行周期的监控插入到queue中
		ThreadPoolExecutor consumerThreadPoolExecutor = new ThreadPoolExecutor(2,10,10,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(6),factory,rejectionHandler);
		for (int i =0 ;i<1;i++){
			consumerThreadPoolExecutor.execute(new Consumer());
		}

        consumerThreadPoolExecutor.shutdown();
		//启动消费线程，从queue中take出数据进行处理。
		//查询可执行监控判断阀值，超出阀值进行告警

	}


//	public void run(ApplicationArguments args) throws Exception {
//		logger.info("开始启动timer");
//		timer();
//		logger.info("启动timer结束");
//	}

	class Producer implements Runnable {
		@Override
		public void run() {
//			while (true){
				List<PageData> list = monitorDAO.queryMonitorCofParas();
				if (list!=null&&list.size()>0) {
					DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
					LocalDateTime ldt = LocalDateTime.now();
					for (PageData pd : list) {
						try {
							//logger.info("当前时间="+ldt.format(df));
							LocalDateTime dbTime = LocalDateTime.parse(pd.get("DEAL_TIME").toString(), df);
                            LocalDateTime localDateTime2 = LocalDateTime.of(dbTime.getYear(), dbTime.getMonth(), dbTime.getDayOfMonth(), dbTime.getHour(), dbTime.getMinute(), 0, 0);
							//logger.info("数据时间="+localDateTime2);
							//logger.info("当前时间晚于数据时间"+ldt.isAfter(localDateTime2));
							if(ldt.isAfter(localDateTime2)){
								queue.offer(pd,1, TimeUnit.SECONDS);
								logger.info("向queue中插入pd成功，队列长度=="+queue.size());
							}else{
								logger.info("任务尚未到达执行时间");
							}
						} catch (InterruptedException e) {
							logger.error(pd+"插入队列失败",e);
						}
					}
//				}
//				try {
//					TimeUnit.SECONDS.sleep(20);
//					logger.info("等待20秒");
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}

		}
	}

	class Consumer implements Runnable {

		@Override
		public void run() {
			while (true) {
                try {
                    PageData pd = queue.take();
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                    LocalDateTime nowDT = LocalDateTime.now();
                    String nowTime = nowDT.format(df);
                    logger.info(Thread.currentThread().getName() + "消费者获得上次执行时间==" + pd);
                    int min = Integer.valueOf(pd.get("MONITOR_SCHEDULE").toString());
                    String nextExTime = nowDT.plusMinutes(min).format(df);
                    //LocalDateTime ldt = LocalDateTime.parse(pd.get("DEAL_TIME").toString(), df);
                    //logger.info("消费者获得上次执行时间==" + ldt);
                    DateTimeFormatter dfMin = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    //String beforeExTime = ldt.format(dfMin);
                    //logger.info("消费者获得上次执行时间Str-MIN==" + beforeExTime);
                    String sql = String.valueOf(pd.get("MAPITEM_VALUE"));
                    Double result = monitorDAO.queryMonitorResult(pd);
                    Double threshold = Double.valueOf(String.valueOf(pd.get("PARA_SERVICETHRESHOLD")));
                    PageData pdResult = new PageData();
                    DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    pdResult.put("MONITOR_SPLIT_TIME", getSplitTime(nowDT, min));
                    pdResult.put("MONITOR_DATA_VALUE", result);
                    pdResult.put("CREATE_TIME", nowTime);
                    pdResult.put("EXPIRE_FLAG", "0");
                    pdResult.put("UPDATE_TIME", nowTime);
                    pdResult.put("MONITOR_SCHEDULE_DATE", nowDT.format(dfDate));
                    pdResult.put("DATA_RESULT", "$1:" + result);
                    pdResult.put("MONITOR_CON_PARA_ID", pd.get("MONITOR_CON_PARA_ID"));
                    int isInsertSuccess = monitorDAO.insertMonitorRecord(pdResult);
                    if (isInsertSuccess == 1) {
                        logger.info(pdResult + "插入成功");
                    }
                    if ("0".equals(pd.get("PRE_COMPARE_SWITCH"))) { //不与前一天数据比较
                        if (compareTwoNum(pd.get("OPERATOR_SYMBOL").toString(), result, threshold)) {
                            String notice = pd.get("PARA_NOTIFYMESSAGE").toString().replace(pd.get("PARA_EXPRESSION").toString(), result.toString());
                            String monitorName = pd.get("PARA_SERVICENAME").toString();
                            String sendMsgValidTime = pd.get("SEND_MSG_VALID_TIME").toString();
                            sendMessage(monitorName, notice, sendMsgValidTime);
                        }
                    } else {
                        PageData queryPd = new PageData();
                        queryPd.put("MONITOR_SPLIT_TIME", getSplitTime(nowDT, min));
                        queryPd.put("MONITOR_SCHEDULE_DATE", nowDT.plusDays(-1).format(dfDate));
                        logger.info(queryPd.toString());
                        List<PageData> pdList = monitorDAO.queryMonitorRecords(queryPd);
                        if (pdList != null && pdList.size() > 0) {
                            for (PageData pdVO : pdList) {
                                logger.info("迭代返回集合=" + pdVO.toString());
                                    /*
                                    1、同环比区别是本期统计数据与上期比较，例如2014年7月份与2014年6月份相比较，叫环比。与历史同时期比较，例如2014年7月份与2013年7月份相比，叫同比。
                                    1）环比增长率=（本期数－上期数）/上期数×100%。
                                    2）同比增长率=（本期数－同期数）/同期数×100%。
                                     */
                                //此处做环比
                                Double lastResult = Double.valueOf(pdVO.get("MONITOR_DATA_VALUE").toString());
                                if (lastResult.intValue() == 0) {
                                    lastResult = 1D;
                                }
                                Double offset = (result - lastResult) / lastResult * 100;
                                if (offset >= 0) {//为了测试，暂时将阀值设置为0；
                                    logger.info("offset==" + offset + "%");
                                    String notice = pd.get("PARA_NOTIFYMESSAGE").toString().replace(pd.get("PARA_EXPRESSION").toString(), result.toString())
                                            .replace("lastResult", lastResult.toString())
                                            .replace("offset", offset + "%");
                                    String monitorName = pd.get("PARA_SERVICENAME").toString();
                                    String sendMsgValidTime = pd.get("SEND_MSG_VALID_TIME").toString();
                                    sendMessage(monitorName, notice, sendMsgValidTime);
                                }
                            }
                        } else {
                            logger.info("pdList为空，暂时无法对比");
                        }
                    }
                    PageData dealPd = new PageData();
                    logger.info("下次执行时间==" + nextExTime);
                    dealPd.put("DEAL_TIME", nextExTime);
                    dealPd.put("MONITOR_CON_PARA_ID", pd.get("MONITOR_CON_PARA_ID"));
                    int hasUpdateSuccess = monitorDAO.updateMonitorCofParas(dealPd);
                    if (hasUpdateSuccess == 1) {
                        logger.info(dealPd + "修改成功");
                    }

                    logger.info("当前队列长队==" + queue.size());
                } catch (InterruptedException e) {
                    logger.error("消费数据失败", e);
                }
            }
		}
	}

	public  void sendMessage(String monitorName,String msg,String sendMsgValidTime){
	    if(isSend(sendMsgValidTime)){
            logger.info("监控："+monitorName+"执行告警消息："+msg);
        }
	}

	public String getSplitTime(LocalDateTime nowDT,int min){
	    logger.info("nowDT="+nowDT+";min="+min);
        int startSplitTimeHour = nowDT.plusMinutes(-min).getHour();
        int startSplitTimeMin = nowDT.plusMinutes(-min).getMinute();
//        int startSplitTimeHour = nowDT.getHour();
//        int startSplitTimeMin = nowDT.getMinute();
        int endSplitTimeHour = nowDT.getHour();
        int endSplitTimeMin = nowDT.getMinute();
        String splitTime = getTimebyZero(startSplitTimeHour)+":"+getTimebyZero(startSplitTimeMin)+"~"+getTimebyZero(endSplitTimeHour)+":"+getTimebyZero(endSplitTimeMin);
        logger.info("splitTime="+splitTime);
        return splitTime;
    }

    public String getTimebyZero(int num){
	    if(num<10){
	        return "0"+num;
        }else{
	        return String.valueOf(num);
        }
    }


	public  boolean isSend(String sendMsgValidTime){
	    boolean isSend = false;
        try {
            String begin = sendMsgValidTime.split("~")[0];
            String beginHour = begin.split(":")[0];
            String beginMin = begin.split(":")[1];
            LocalTime beginTime = LocalTime.of(Integer.valueOf(beginHour),Integer.valueOf(beginMin));
            String end= sendMsgValidTime.split("~")[1];
            String endHour = end.split(":")[0];
            String endMin = end.split(":")[1];
            LocalTime endTime = LocalTime.of(Integer.valueOf(endHour),Integer.valueOf(endMin));

            LocalTime lt = LocalTime.now();
            if(lt.isAfter(beginTime)&&lt.isBefore(endTime)){
                isSend = true;
            }
        } catch (Exception e) {
            logger.error("isSend判断异常",e);
        }
        return isSend;
    }

	public static boolean compareTwoNum(String operator, Double a, Double b){
		boolean flag = false;
		double doubleA = a.doubleValue();
		double doubleB = b.doubleValue();
		if("<".equals(operator)){
			flag = (doubleA<doubleB?true:false);
		}else if("<=".equals(operator)){
			flag = (doubleA<=doubleB?true:false);
		}else if("=".equals(operator)){
			flag = ((doubleA==doubleB)?true:false);
		}else if(">".equals(operator)){
			flag = (doubleA>doubleB?true:false);
		}else if(">=".equals(operator)){
			flag = (doubleA>=doubleB?true:false);
		}
		return flag;
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
        System.out.println("args = [" + new MonitorController().isSend("09:10~15:00") + "]");
        LocalDateTime nowDT = LocalDateTime.now();
        System.out.println("nowDT："+nowDT);
        LocalDateTime s = nowDT.plusMinutes(-10);
        System.out.println("s："+s);
    }
}
