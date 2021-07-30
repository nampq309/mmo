package com.mmo;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ReadInputFromKQNet1 {
	
	public static void main(String[] args) throws Exception {
		
		SimpleDateFormat dMY = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat yMD = new SimpleDateFormat("yyyyMMdd");
		int dayNum = 360;
		Calendar cal = Calendar.getInstance();
		Date startDate = yMD.parse("20210131");
		cal.setTime(startDate);
		cal.add(Calendar.DAY_OF_MONTH, -dayNum);
		List<String> divIds = new ArrayList<String>();
		divIds.add("#rs_0_0");
		divIds.add("#rs_1_0");
		divIds.add("#rs_2_0");
		divIds.add("#rs_2_1");
		divIds.add("#rs_3_0");
		divIds.add("#rs_3_1");
		divIds.add("#rs_3_2");
		divIds.add("#rs_3_3");
		divIds.add("#rs_3_4");
		divIds.add("#rs_3_5");
		divIds.add("#rs_4_0");
		divIds.add("#rs_4_1");
		divIds.add("#rs_4_2");
		divIds.add("#rs_4_3");
		divIds.add("#rs_5_0");
		divIds.add("#rs_5_1");
		divIds.add("#rs_5_2");
		divIds.add("#rs_5_3");
		divIds.add("#rs_5_4");
		divIds.add("#rs_5_5");
		divIds.add("#rs_6_0");
		divIds.add("#rs_6_1");
		divIds.add("#rs_6_2");
		divIds.add("#rs_7_0");
		divIds.add("#rs_7_1");
		divIds.add("#rs_7_2");
		divIds.add("#rs_7_3");
		System.out.println("Start date " + yMD.format(startDate));
		StringBuilder inputBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		try {
			for (int i = 0; i < dayNum; i++) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
				String url = "https://ketqua1.net/xo-so-truyen-thong.php?ngay=%s";
				String curDate = dMY.format(cal.getTime());
				System.out.println("request: " + String.format(url, curDate));
				String curDate2 = yMD.format(cal.getTime());
				Document doc = Jsoup.connect(String.format(url, curDate)).timeout(30 * 1000).get();
				System.err.println(doc.title());
				inputBuilder.append(String.format("=================== Ngay %s =================== ", curDate2)).append("\n");
				dataBuilder.append(curDate2).append(";");
				for (String div : divIds) {
					Elements newsHeadlines = doc.select(div);
					String value = newsHeadlines.html();
					inputBuilder.append(value).append("\n");
					//cut the last 2 characters
					String last2 = value;
					if (last2.length() > 2) {
						last2 = last2.substring(last2.length() - 2, last2.length());
					} else if (last2.length() < 2) {
						System.out.println("Giai khong hop le !!! " + last2);
						continue;
					}
					dataBuilder.append(last2).append(";");
				}
				inputBuilder.append("\n");
				dataBuilder.append("\n");
				//Elements newsHeadlines = doc.select("#rs_2_0");
				//System.out.println(newsHeadlines.html());
				Thread.sleep(1000);
				if (i % 30 == 0) {
					Files.write(Paths.get("E:\\MMO\\temp.csv"), inputBuilder.toString().getBytes(), StandardOpenOption.APPEND);
					inputBuilder = new StringBuilder();
					Files.write(Paths.get("E:\\MMO\\" + yMD.format(startDate) + ".csv"), dataBuilder.toString().getBytes(), StandardOpenOption.APPEND);
					dataBuilder = new StringBuilder();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(outFileBuilder.toString());
		Files.write(Paths.get("E:\\MMO\\temp.csv"), inputBuilder.toString().getBytes(), StandardOpenOption.APPEND);
		//System.out.println(dataBuilder);
		Files.write(Paths.get("E:\\MMO\\" + yMD.format(startDate) + ".csv"), dataBuilder.toString().getBytes(), StandardOpenOption.APPEND);
	}

}
