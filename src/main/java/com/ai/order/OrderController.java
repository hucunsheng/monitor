package com.ai.order;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
import com.ai.base.MySqlPullDao;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.ai.util.PageData;
import org.thymeleaf.util.StringUtils;

//@RestController
public class OrderController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private MySqlPullDao mySqlPullDao;

	@RequestMapping(value="/getSlowSQL",method=RequestMethod.POST)
	public @ResponseBody Map<String,Object> getSlowSQL(String from ,String to ,Integer pageIndex){
        Map<String,Object> map = new HashMap<String,Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			if(pageIndex==null){
				pageIndex = 1;
			}
			if(StringUtils.isEmpty(from)&&StringUtils.isEmpty(to)){
                String dateFromTime = sdf.format(new Date());
                Calendar ca = Calendar.getInstance();
                ca.add(Calendar.DAY_OF_MONTH, 1);
				from = dateFromTime;
				to = sdf.format(ca.getTime());
			}
			PageData pd = new PageData();
			pd.put("from",from);
			pd.put("to",to);
			pd.put("pageIndex",(pageIndex-1)*10);
			pd.put("pageSize",10);
			List<PageData> pdList=mySqlPullDao.getSlowSQL(pd);
			if(pdList!=null&&pdList.size()>0){
				for (PageData pdVO : pdList){
					String fingerprint = String.valueOf(pdVO.get("fingerprint"));
					if(fingerprint.length()>2000){
						pdVO.put("fingerprint",fingerprint.substring(0,2000));
					}
					String sample = String.valueOf(pdVO.get("sample"));
					if(sample.length()>2000){
						pdVO.put("sample",sample.substring(0,2000));
					}
				}
			}
            map.put("res", pdList);
            map.put("pageIndex", pageIndex.intValue());
            map.put("from",from);
            map.put("to",to);
		} catch (Exception e){
            logger.error("查询数据异常",e);
		}
		return map;
	}

	@RequestMapping(value = "download", method = RequestMethod.POST)
	public void download(String from ,String to,HttpServletResponse response)  {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("慢SQL信息表");
		createTitle(sheet);
		List<PageData> pdList= null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateTime = sdf.format(new Date());
		try {
			if(StringUtils.isEmpty(from)&&StringUtils.isEmpty(to)){
				from = dateTime;
				Calendar ca = Calendar.getInstance();
				ca.add(Calendar.DAY_OF_MONTH, 1);
				to = sdf.format(ca.getTime());
			}
			PageData pd = new PageData();
			pd.put("from",from);
			pd.put("to",to);
			pd.put("pageIndex",0);
			pd.put("pageSize",Integer.MAX_VALUE);
			pdList=mySqlPullDao.getSlowSQL(pd);


			//拼装blobName
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
			String fileName = "all-" + dateFormat.format(new Date())+ ".xls";
			//新增数据行，并且设置单元格数据
			int rowNum = 1;
			for (PageData pData : pdList) {
				HSSFRow row = sheet.createRow(rowNum);
				row.createCell(0).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("minute"))));
				row.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("query_time_avg"))));
				String fingerprint = String.valueOf(pData.get("fingerprint"));
				if(fingerprint.length()>3000){
                    row.createCell(2).setCellValue(new HSSFRichTextString(fingerprint.substring(0,3000)));
                }else{
                    row.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("fingerprint"))));

                }
                String sample = String.valueOf(pData.get("sample"));
                if(sample.length()>3000){
                    row.createCell(2).setCellValue(new HSSFRichTextString(sample.substring(0,3000)));
                }else{
                    row.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("sample"))));

                }
				row.createCell(4).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("hostname_max"))));
				row.createCell(5).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("db_max"))));
				row.createCell(6).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("ts_cnt"))));
                logger.info("第"+rowNum+"行开始");
                logger.info(String.valueOf(pData.get("minute")));
                logger.info(String.valueOf(pData.get("query_time_avg")));
                logger.info(String.valueOf(pData.get("fingerprint")));
                logger.info(String.valueOf(pData.get("sample")));
                logger.info(String.valueOf(pData.get("hostname_max")));
                logger.info(String.valueOf(pData.get("db_max")));
                logger.info(String.valueOf(pData.get("ts_cnt")));
                logger.info("第"+rowNum+"行结束");
				rowNum++;
			}

			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			response.flushBuffer();
			workbook.write(response.getOutputStream());
		} catch (Exception e){

            logger.error("导出数据异常",e);
		} finally {
        }
    }


	/***
	 * 创建表头
	 * @param sheet
	 */
	private void createTitle(HSSFSheet sheet)
	{
		HSSFRow row = sheet.createRow(0);
		//设置列宽，setColumnWidth的第二个参数要乘以256，这个参数的单位是1/256个字符宽度
		sheet.setColumnWidth(0, 80*256);
		sheet.setColumnWidth(1, 80*256);
		sheet.setColumnWidth(2, 80*256);
		sheet.setColumnWidth(3, 80*256);
		sheet.setColumnWidth(4, 80*256);
		sheet.setColumnWidth(5, 80*256);
		sheet.setColumnWidth(6, 80*256);


		HSSFCell cell;
		cell = row.createCell(0);
		cell.setCellValue("执行时间");

		cell = row.createCell(1);
		cell.setCellValue("平均时长");

		cell = row.createCell(2);
		cell.setCellValue("模版");

		cell = row.createCell(3);
		cell.setCellValue("样例");

		cell = row.createCell(4);
		cell.setCellValue("主机host");

		cell = row.createCell(5);
		cell.setCellValue("数据库名称");

		cell = row.createCell(6);
		cell.setCellValue("执行次数");
	}

	@Scheduled(cron = "0 10 0 * * *")
	//@Scheduled(cron = "0 0/2 * * * *")
	public void timer(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar caFile = Calendar.getInstance();
		caFile.add(Calendar.DAY_OF_MONTH, -1);
		String fileDate = sdf.format(caFile.getTime());
		String fromDate = fileDate;
		String toDate = sdf.format(new Date());
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("慢SQL信息表");
		createTitle(sheet);
		List<PageData> pdList= null;

		try {
			PageData pd = new PageData();

			pd.put("from",fromDate);
			pd.put("to",toDate);
			pd.put("pageIndex",0);
			pd.put("pageSize",Integer.MAX_VALUE);
			pdList = mySqlPullDao.getSlowSQL(pd);

			List<PageData> insList= new ArrayList<PageData>();
			List<PageData> twouseList= new ArrayList<PageData>();
			List<PageData> campaignList= new ArrayList<PageData>();
			List<PageData> ordList= new ArrayList<PageData>();
			List<PageData> rateList= new ArrayList<PageData>();
			List<PageData> otherList= new ArrayList<PageData>();

			//拼装blobName
			String fileName = "all-" + fileDate+ ".xls";
			//新增数据行，并且设置单元格数据
			int rowNum = 1;
			for (PageData pData : pdList) {
					if(String.valueOf(pData.get("db_max")).contains("ins")){
						insList.add(pData);
					}
					if(String.valueOf(pData.get("db_max")).contains("twouse")){
						twouseList.add(pData);
					}
					if(String.valueOf(pData.get("db_max")).contains("campaign")){
						campaignList.add(pData);
					}
					if(String.valueOf(pData.get("db_max")).contains("ord")){
						ordList.add(pData);
					}
					if(String.valueOf(pData.get("db_max")).contains("rate")){
						rateList.add(pData);
					}
					if(String.valueOf(pData.get("db_max")).equals("null")){
						otherList.add(pData);
					}
				HSSFRow row = sheet.createRow(rowNum);
				row.createCell(0).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("minute"))));
				row.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("query_time_avg"))));
				String fingerprint = String.valueOf(pData.get("fingerprint"));
				if(fingerprint.length()>3000){
					row.createCell(2).setCellValue(new HSSFRichTextString(fingerprint.substring(0,3000)));
				}else{
					row.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("fingerprint"))));

				}
				String sample = String.valueOf(pData.get("sample"));
				if(sample.length()>3000){
					row.createCell(2).setCellValue(new HSSFRichTextString(sample.substring(0,3000)));
				}else{
					row.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("sample"))));

				}
				row.createCell(4).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("hostname_max"))));
				row.createCell(5).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("db_max"))));
				row.createCell(6).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("ts_cnt"))));
//				logger.info("第"+rowNum+"行开始");
//				logger.info(String.valueOf(pData.get("minute")));
//				logger.info(String.valueOf(pData.get("query_time_avg")));
//				logger.info(String.valueOf(pData.get("fingerprint")));
//				logger.info(String.valueOf(pData.get("sample")));
//				logger.info(String.valueOf(pData.get("hostname_max")));
//				logger.info(String.valueOf(pData.get("db_max")));
//				logger.info(String.valueOf(pData.get("ts_cnt")));
//				logger.info("第"+rowNum+"行结束");
				rowNum++;
			}
			File file = new File("/data/migup1/why/slowsql/"+fileName);
			FileOutputStream fos = new FileOutputStream(file);
			workbook.write(fos);// 写文件
			logger.info("生成"+fileName+"文件完毕");
			createFile(insList,"ins",fileDate);
			createFile(twouseList,"twouse",fileDate);
			createFile(campaignList,"campaign",fileDate);
			createFile(ordList,"ord",fileDate);
			createFile(rateList,"rate",fileDate);
			createFile(otherList,"other",fileDate);
		} catch (Exception e){

			logger.error("生成文件异常",e);
		} finally {
		}

	}

	public void createFile(List<PageData> pdList,String name,String fileDate){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//拼装blobName
		String fileName = name+"-" + fileDate+ ".xls";
		try {
			if(pdList!=null&&pdList.size()>0){

				HSSFWorkbook workbook = new HSSFWorkbook();
				HSSFSheet sheet = workbook.createSheet("慢SQL信息表");
				createTitle(sheet);

				//新增数据行，并且设置单元格数据
				int rowNum = 1;
				for (PageData pData : pdList) {
					HSSFRow row = sheet.createRow(rowNum);
					row.createCell(0).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("minute"))));
					row.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("query_time_avg"))));
					String fingerprint = String.valueOf(pData.get("fingerprint"));
					if(fingerprint.length()>3000){
						row.createCell(2).setCellValue(new HSSFRichTextString(fingerprint.substring(0,3000)));
					}else{
						row.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("fingerprint"))));

					}
					String sample = String.valueOf(pData.get("sample"));
					if(sample.length()>3000){
						row.createCell(2).setCellValue(new HSSFRichTextString(sample.substring(0,3000)));
					}else{
						row.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("sample"))));

					}
					row.createCell(4).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("hostname_max"))));
					row.createCell(5).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("db_max"))));
					row.createCell(6).setCellValue(new HSSFRichTextString(String.valueOf(pData.get("ts_cnt"))));
//					logger.info("第"+rowNum+"行开始");
//					logger.info(String.valueOf(pData.get("minute")));
//					logger.info(String.valueOf(pData.get("query_time_avg")));
//					logger.info(String.valueOf(pData.get("fingerprint")));
//					logger.info(String.valueOf(pData.get("sample")));
//					logger.info(String.valueOf(pData.get("hostname_max")));
//					logger.info(String.valueOf(pData.get("db_max")));
//					logger.info(String.valueOf(pData.get("ts_cnt")));
//					logger.info("第"+rowNum+"行结束");
					rowNum++;
				}
				File file = new File("/data/migup1/why/slowsql/"+fileName);
				FileOutputStream fos = new FileOutputStream(file);
				workbook.write(fos);// 写文件
				logger.info("生成"+fileName+"文件完毕");
			}

		} catch (Exception e){

			logger.error("生成"+fileName+"文件异常",e);
		} finally {
		}
	}

}
