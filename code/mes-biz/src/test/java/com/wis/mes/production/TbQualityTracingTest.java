package com.wis.mes.production;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.wis.mes.basis.BizBaseTestCase;
import com.wis.mes.master.workcalendar.service.TmWorktimeService;
import com.wis.mes.production.producttracing.service.TbProductTracingService;
import com.wis.mes.production.qualitytracing.service.TbQualityTracingService;

public class TbQualityTracingTest extends BizBaseTestCase {

	@Autowired
	TbQualityTracingService tbQualityTracingService;
	@Autowired
	TbProductTracingService tbProductTracingService;
	@Autowired
	private TmWorktimeService worktimeService;

	@Test
	public void getProductStationVoTest() {
		System.out.println(tbProductTracingService.getEgModelName("A078"));
	}

	public static void readFile(String sourceFilePath, String encode) throws IOException{
		  File file = new File(sourceFilePath);
		  BufferedReader br = new   BufferedReader(new InputStreamReader(new FileInputStream(file), encode));
		  StringBuilder strBuilder = new StringBuilder();
		  String sLine = null;   
		  while((sLine = br.readLine()) != null){
		   strBuilder.append(sLine);
		   strBuilder.append("\r\n");
		  }

		  br.close();
		 }

	public static String[] getFileNames(String path) {
		File dirFile = new File(path);
		if (dirFile.isDirectory()) {
			File[] files = dirFile.listFiles();
			String[] fileNames = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				fileNames[i] = files[i].getAbsolutePath();
			}
			return fileNames;
		} else {
			return null;
		}
	}

	public static void main(String[] args) throws IOException {

		// 读取单个文件
		readFile("\\\\192.168.90.4\\共享文件\\KUKURI_PROCA.csv", "utf-8");

//		// 读取某个目录下所有文件
//
//		String[] fileNames = getFileNames("\\\\192.168.0.100\\public\\aa");
//		String encode = "utf-8";
//		for (String fileName : fileNames) {
//			try {
//				readFile(fileName, encode);
//			} catch (IOException e) {
//			}
//		}
	}
	
	@Test
	public void testWorkTime() {
		Map<String, Object> workTime = worktimeService.sendBjPlcWorkingCalendar();
	}
}
