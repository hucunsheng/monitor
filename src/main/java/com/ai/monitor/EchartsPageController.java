package com.ai.monitor;

import com.ai.base.MonitorCofParaDAO;
import com.ai.util.HtmlToImageMain;
import com.ai.util.PageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * 通过获取监控配置表进行查询待处理任务，执行超过阀值的进行告警。
 */
@RestController
public class EchartsPageController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private MonitorCofParaDAO monitorDAO;


	/**
	 * 获取监控数据，并将数据通过柱状图展示
	 * @param from  开始时间
	 * @param to    结束时间
	 * @param monitorId  监控ID
	 * @return    包含：X轴的描述和Y轴的值
	 */
	@RequestMapping(value="/getEcharts",method= RequestMethod.POST)
	public @ResponseBody Map<String,Object>  echarts(String from ,String to ,Integer monitorId) {
		logger.info("echarts任务执行中");
		List<String> xAxisList = new ArrayList<>();
		List<String> seriesList = new ArrayList<>();
		String scheduleDate = "";
		String from1 = "";
		String to1 = "";
		Map<String,Object> map = new HashMap<String,Object>();
		if((StringUtils.isEmpty(from)||StringUtils.isEmpty(to))&&(StringUtils.isEmpty(from1)||StringUtils.isEmpty(to1))){
			from = LocalDateTime.now().plusHours(-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			to =LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			from1 = LocalDateTime.now().plusDays(-1).plusHours(-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			to1 =LocalDateTime.now().plusDays(-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			map.put("from",from);
			map.put("to",to);
			map.put("from1",from1);
			map.put("to1",to1);
			map.put("resName",scheduleDate);
			map.put("xAxisList",xAxisList);
			map.put("seriesList",seriesList);
			return map;
		}


		List<PageData> pdList = null;
		PageData queryPD = new PageData();
		queryPD.put("from",from);
		queryPD.put("to",to);
		queryPD.put("MONITOR_CON_PARA_ID",monitorId);
		try {
			pdList = monitorDAO.queryMonitorRecords(queryPD);
		} catch (Exception e) {
			logger.info("HtmlToImageERROR",e);
		}

		if(pdList!=null&&pdList.size()>0){
			for (PageData pd : pdList){
				scheduleDate = pd.get("MONITOR_SCHEDULE_DATE").toString();
				xAxisList.add(pd.get("MONITOR_SPLIT_TIME").toString());
				seriesList.add(pd.get("MONITOR_DATA_VALUE").toString());
			}
		}
		map.put("resName",scheduleDate);
		map.put("xAxisList",xAxisList);
		map.put("seriesList",seriesList);
		map.put("from",from);
		map.put("to",to);
		map.put("from1",from1);
		map.put("to1",to1);
		return map;
	}




	/**
	 * 查询当前生效的监控列表，在前端以select组件展示。默认显示第一个
	 * @return
	 */
	@RequestMapping(value="/getMonitors",method= RequestMethod.GET)
	public @ResponseBody Map<String,Object>  getMonitors() {
		logger.info("getMonitors任务执行中");
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,String> mapStr = new HashMap<String,String>();
		List<PageData> monitorList = null;
		List<PageData> selectList = new ArrayList<PageData>();
		try {
			monitorList = monitorDAO.queryMonitorCofParas();
			if(monitorList!=null&&monitorList.size()>0){
				for (PageData pd : monitorList){
					PageData newPd = new PageData();
					newPd.put("MONITOR_CON_PARA_ID",pd.get("MONITOR_CON_PARA_ID").toString());
					newPd.put("PARA_SERVICENAME",pd.get("PARA_SERVICENAME").toString());
					selectList.add(newPd);
					//mapStr.put(pd.get("MONITOR_CON_PARA_ID").toString(),pd.get("PARA_SERVICENAME").toString());
				}
			}
		} catch (Exception e) {
			logger.info("getMonitorsERROR",e);
		}
		map.put("monitors",mapStr);
		map.put("monitorsList",monitorList);
		map.put("selectList",selectList);
		logger.info("getMonitors任务执行完毕");
		return map;
	}


	/**
	 * 修改数据为随机数，为了看出数据波动效果。该方法为测试方法，已废弃
	 * @return
	 */
//	@RequestMapping(value="/updateMonitorRecords",method= RequestMethod.GET)
	public @ResponseBody Map<String,Object>  updateMonitorRecords() {
		logger.info("updateMonitorRecords任务执行中");
		Map<String,Object> map = new HashMap<String,Object>();
		List<PageData> monitorList = null;
		int hasUpdateRecord = 0;
		try {
			monitorList = monitorDAO.queryMonitorRecords(null);
			if(monitorList!=null&&monitorList.size()>0){
				for (PageData pd : monitorList){
					PageData newPd = new PageData();
					if(!"18".equals(pd.get("MONITOR_DATA_VALUE"))){
						newPd.put("MONITOR_RECORD_ID",pd.get("MONITOR_RECORD_ID").toString());
						newPd.put("MONITOR_DATA_VALUE",String.valueOf(new Random().nextInt(100)));
						newPd.put("UPDATE_TIME",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
						if(monitorDAO.updateMonitorRecord(newPd)==1){
							hasUpdateRecord++;
							logger.info("修改成功，当前条数="+hasUpdateRecord);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.info("updateMonitorRecordsERROR",e);
		}
		if(hasUpdateRecord==monitorList.size()){
			map.put("result","SUCCESS");
		}
		logger.info("updateMonitorRecords任务执行完毕");
		return map;
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
        for(int j= 0;j<10;j++){

			//System.out.println("s："+Math.random() * 1000);
			System.out.println("s："+ String.valueOf(new Random().nextInt(1000)));
		}
    }
}
