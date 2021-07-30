package com.mmo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LotoBridge
{
  private static LinkedList<String> DB = new LinkedList();
  final String DB_GIAI = "G.DB";
  private static String lastDate = "";
  static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
  static SimpleDateFormat df_reverse = new SimpleDateFormat("ddMM");
  
  public static void main(String[] args)
    throws Exception
  {
    String file = "temp.csv";
    LotoBridge bridge = new LotoBridge();
    //Đọc data của tất cả các ngày trong tháng và đưa vào dạng map
    Map<String, Object> final_MetaData = bridge.readDaily(file, "");
    
    System.out.println("Last Date = " + lastDate);
    Map<String, String> lastData = (Map)final_MetaData.get(lastDate);
    
    //Tim ra tất cả các cầu lô của những ngày trong meta data
    Map<String, Object> final_ngay_bridges = bridge.lookBridge(final_MetaData);
    
    Map<String, String> final_giai_of_ngay_bigger_than_3 = new HashMap();
    
    Map<String, String> giai_of_ngay = new HashMap();
    Iterator localIterator2;
    for (Iterator localIterator1 = final_ngay_bridges.entrySet().iterator(); localIterator1.hasNext(); localIterator2.hasNext())
    {
      Map.Entry<String, Object> _bridge = (Map.Entry)localIterator1.next();
      String ngay = (String)_bridge.getKey();
      Map<String, String> bridges = (Map)_bridge.getValue();
      localIterator2 = bridges.keySet().iterator(); continue;
      String giai_and_index = (String)localIterator2.next();
      
      String days = (String)giai_of_ngay.get(giai_and_index);
      if (days == null) {
        days = ngay;
      } else if (days.indexOf(ngay) < 0) {
        days = days + "," + ngay;
      }
      giai_of_ngay.put(giai_and_index, days);
      if (days.split(",").length > 4) {
        final_giai_of_ngay_bigger_than_3.put(giai_and_index, days);
      }
    }
    String curDate = args[0];
    int loopCount = 5;
    try
    {
    // Số ngày muốn chạy tính từ ngày bắt đầu - current Date (args[0])
    //Nếu số ngày muốn chạy lớn hơn data hiện có thì sẽ bị bắn exception nhưng ko việc gì đến kết quả những ngày trước
      loopCount = Integer.parseInt(args[4]);
    }
    catch (Exception e)
    {
      loopCount = 5;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(df.parse(curDate));
    for (int i = 1; i <= loopCount; i++)
    {
      String tempDate = df.format(cal.getTime());
      System.out.println("Date " + tempDate);
      
      Object curMetaData = bridge.readDaily(file, tempDate.trim());
      Map<String, Object> cur_ngay_bridges = bridge.lookBridge((Map)curMetaData);
      Map<String, String> curData = (Map)((Map)curMetaData).get(tempDate);
      Map<String, String> cur_giai_of_ngay_bigger_than_3 = new HashMap();
      
      getCurrentData((Map)curMetaData, cur_ngay_bridges, cur_giai_of_ngay_bigger_than_3);
      
      Map<String, Object> outputOfCurDate = new HashMap();
      
      getFinalData(outputOfCurDate, cur_giai_of_ngay_bigger_than_3, curData, args, tempDate);
      
      Map<String, Object> finalData = new HashMap();
      getFinalData(finalData, final_giai_of_ngay_bigger_than_3, lastData, args, tempDate);
      
      writeMultipleDays(outputOfCurDate, finalData, final_giai_of_ngay_bigger_than_3, tempDate);
      //tang them 1 ngay
      cal.add(5, 1);
    }
  }
  
  public static void writeMultipleDays(Map<String, Object> outputOfCurDate, Map<String, Object> finalData, Map<String, String> finalBridgeMap, String curDate)
    throws Exception
  {
    String curFileName = df_reverse.format(df.parse(curDate));
    String nextFileName = df_reverse.format(df.parse(curDate)) + "_next";
    FileWriter curFile = new FileWriter(curFileName, false);
    BufferedWriter curOutput = new BufferedWriter(curFile);
    
    curOutput.write("======== Current: " + curDate + " ========= \n");
    
    List<String> sortedKeys = new ArrayList(outputOfCurDate.keySet());
    Collections.sort(sortedKeys);
    Map<String, Object> sortedResult = new LinkedHashMap();
    for (String key : sortedKeys) {
      sortedResult.put(key, outputOfCurDate.get(key));
    }
    int count = 1;
    Map<String, String> data;
    String days;
    for (Object entry : sortedResult.entrySet())
    {
      String keyData = (String)((Map.Entry)entry).getKey();
      
      data = (Map)((Map.Entry)entry).getValue();
      if (data.size() != 0)
      {
        curOutput.write(String.format("Number is %s", new Object[] { keyData }) + " \n");
        for (Map.Entry<String, String> item : data.entrySet())
        {
          String key = (String)item.getKey();
          days = (String)finalBridgeMap.get(key);
          if (days == null)
          {
            String[] arr = key.split("_VS_");
            String reverseKey = arr[1] + "_VS_" + arr[0];
            
            System.out.println("KEY NOT EXIST == " + key);
            System.out.println("REVERSE KEY == " + reverseKey);
            
            days = (String)finalBridgeMap.get(reverseKey);
          }
          if (days.indexOf(curDate) >= 0) {
            curOutput.write((String)item.getKey() + " [" + count + "] PLUS 1 \n");
          } else {
            curOutput.write((String)item.getKey() + " [" + count + "] \n");
          }
          curOutput.write(days + " \n");
          count++;
        }
      }
    }
    FileWriter nextFile = new FileWriter(nextFileName, false);
    BufferedWriter nextOutput = new BufferedWriter(nextFile);
    
    nextOutput.write("======== Last: " + lastDate + " ========= \n");
    
    sortedKeys = new ArrayList(finalData.keySet());
    Collections.sort(sortedKeys);
    sortedResult = new LinkedHashMap();
    for (String key : sortedKeys) {
      sortedResult.put(key, finalData.get(key));
    }
    count = 1;
    for (Map.Entry<String, Object> entry : sortedResult.entrySet())
    {
      String keyData = (String)entry.getKey();
      
      Object data = (Map)entry.getValue();
      if (((Map)data).size() != 0)
      {
        nextOutput.write(String.format("Number is %s", new Object[] { keyData }) + " \n");
        for (Map.Entry<String, String> item : ((Map)data).entrySet())
        {
          String days = (String)item.getValue();
          nextOutput.write((String)item.getKey() + " [" + count + "] \n");
          nextOutput.write(days + " \n");
          count++;
        }
      }
    }
    curOutput.close();
    nextOutput.close();
  }
  
  private static void getCurrentData(Map<String, Object> curMetaData, Map<String, Object> cur_ngay_bridges, Map<String, String> cur_giai_of_ngay_bigger_than_3)
  {
    Map<String, String> giai_of_ngay = new HashMap();
    Iterator localIterator2;
    for (Iterator localIterator1 = cur_ngay_bridges.entrySet().iterator(); localIterator1.hasNext(); localIterator2.hasNext())
    {
      Map.Entry<String, Object> _bridge = (Map.Entry)localIterator1.next();
      String ngay = (String)_bridge.getKey();
      Map<String, String> bridges = (Map)_bridge.getValue();
      localIterator2 = bridges.keySet().iterator();//continue
      String giai_and_index = (String) localIterator2.next();
      
      String days = (String)giai_of_ngay.get(giai_and_index);
      if (days == null) {
        days = ngay;
      } else if (days.indexOf(ngay) < 0) {
        days = days + "," + ngay;
      }
      giai_of_ngay.put(giai_and_index, days);
      if (days.split(",").length > 4) {
        cur_giai_of_ngay_bigger_than_3.put(giai_and_index, days);
      }
    }
  }
  
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByKey(Map<K, V> map)
  {
    List<Map.Entry<K, V>> list = new LinkedList(map.entrySet());
    Collections.sort(list, new Comparator()
    {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
      {
        return 
          ((Comparable)o1.getKey()).compareTo(o2.getKey());
      }
    });
    Map<K, V> result = new LinkedHashMap();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), (Comparable)entry.getValue());
    }
    return result;
  }
  
  private static void getFinalData(Map<String, Object> finalData, Map<String, String> giai_of_ngay_bigger_than_3, Map<String, String> lastData, String[] args, String curDate)
    throws ParseException
  {
    Date argDate = df.parse(curDate);
    int pre = Integer.parseInt(args[1]);
    int count = Integer.parseInt(args[2]);
    int post = Integer.parseInt(args[3]) - 1;
    
    String except = "";
    Calendar cal = Calendar.getInstance();
    cal.setTime(argDate);
    cal.add(5, -count);
    for (int i = 0; i < pre; i++)
    {
      cal.add(5, -1);
      if (except != "") {
        except = except + ",";
      }
      except = except + df.format(cal.getTime());
    }
    if (except != "") {
      except = except + ",";
    }
    cal = Calendar.getInstance();
    cal.setTime(argDate);
    if (post > 0) {
      except = except + curDate;
    }
    for (int i = 0; i < post; i++)
    {
      if (except != "") {
        except = except + ",";
      }
      cal.add(5, 1);
      except = except + df.format(cal.getTime());
    }
    System.out.println("Except date ======= " + except);
    
    String required = "";
    cal = Calendar.getInstance();
    cal.setTime(argDate);
    cal.add(5, -count);
    required = required + df.format(cal.getTime());
    for (int i = 1; i < count; i++)
    {
      if (required != "") {
        required = required + ",";
      }
      cal.add(5, 1);
      required = required + df.format(cal.getTime());
    }
    System.out.println("required date ======= " + required);
    
    String[] notRequired = new String[0];
    if (except.trim() != "") {
      notRequired = except.split(",");
    }
    Map<String, String> processed = new HashMap();
    for (Map.Entry<String, String> entry : giai_of_ngay_bigger_than_3.entrySet())
    {
      String key = (String)entry.getKey();
      if (!processed.containsKey(key))
      {
        String firstKey = key.substring(0, key.indexOf("_VS_"));
        String lastKey = key.substring(key.indexOf("_VS_") + 4);
        
        String reverseKey = lastKey + "_VS_" + firstKey;
        
        processed.put(key, key);
        processed.put(reverseKey, reverseKey);
        
        String realKey_1 = firstKey.substring(0, firstKey.indexOf("_index"));
        String realKey_2 = lastKey.substring(0, lastKey.indexOf("_index"));
        
        int _index_1 = Integer.parseInt(firstKey.substring(firstKey.length() - 1));
        int _index_2 = Integer.parseInt(lastKey.substring(lastKey.length() - 1));
        
        String firstNum = String.valueOf(((String)lastData.get(realKey_1)).charAt(_index_1 - 1));
        String lastNum = String.valueOf(((String)lastData.get(realKey_2)).charAt(_index_2 - 1));
        
        String keyData = firstNum + lastNum;
        
        Map<String, String> data = (Map)finalData.get(keyData);
        if (data == null) {
          data = new HashMap();
        }
        if (args.length > 0)
        {
          if (((String)entry.getValue()).contains(required))
          {
            boolean isExit = false;
            String[] arrayOfString1;
            int j = (arrayOfString1 = notRequired).length;
            for (int i = 0; i < j; i++)
            {
              String not = arrayOfString1[i];
              if (((String)entry.getValue()).contains(not)) {
                isExit = true;
              }
            }
            if (isExit) {
              continue;
            }
            data.put((String)entry.getKey(), String.format("Ngay: %s", new Object[] { entry.getValue() }));
          }
        }
        else {
          data.put((String)entry.getKey(), String.format("Ngay: %s", new Object[] { entry.getValue() }));
        }
        finalData.put(keyData, data);
      }
    }
  }
  
  public Map<String, Object> lookBridge(Map<String, Object> maps)
    throws Exception
  {
    Map<String, Object> nextDays = new LinkedHashMap();
    
    int i = 0;
    for (Map.Entry<String, Object> data : maps.entrySet()) {
      if (i == 0)
      {
        i++;
      }
      else
      {
        nextDays.put((String)data.getKey(), data.getValue());
        i++;
      }
    }
    Map<String, Object> TOTAL_BRIDGES = new LinkedHashMap();
    for (Object data : nextDays.entrySet())
    {
      String sDate = (String)((Map.Entry)data).getKey();
      Date date = df.parse(sDate);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.add(5, -1);
      String prevDate = df.format(cal.getTime());
      
      Map<String, String> _data_of_date = (Map)((Map.Entry)data).getValue();
      
      Map<String, String> bridge = new HashMap();
      for (Iterator localIterator3 = _data_of_date.entrySet().iterator(); localIterator3.hasNext(); ???.hasNext())
      {
        Map.Entry<String, String> _data_of_giai = (Map.Entry)localIterator3.next();
        String _g_Key = (String)_data_of_giai.getKey();
        System.out.println("Process: " + _g_Key);
        String _g_value = (String)_data_of_giai.getValue();
        
        String loto = "";
        if (_g_value.length() > 3) {
          loto = _g_value.substring(_g_value.length() - 2);
        } else if (_g_value.length() == 3) {
          loto = _g_value.substring(1);
        } else if (_g_value.length() == 2) {
          loto = _g_value;
        }
        String firstNum = String.valueOf(loto.charAt(0));
        String lastNum = String.valueOf(loto.charAt(1));
        
        Map<String, String> _data_of_prev_date = (Map)maps.get(prevDate);
        
        Map<String, String> _dau = new HashMap();
        Map<String, String> _duoi = new HashMap();
        int index;
        for (Map.Entry<String, String> _pre_g : _data_of_prev_date.entrySet())
        {
          String _giai = (String)_pre_g.getKey();
          String number = (String)_pre_g.getValue();
          if (number.indexOf(firstNum) >= 0)
          {
            int index = number.indexOf(firstNum) + 1;
            
            _dau.put(_giai + "_index_" + index, firstNum);
            while (number.substring(index).indexOf(firstNum) >= 0)
            {
              index += number.substring(index).indexOf(firstNum) + 1;
              _dau.put(_giai + "_index_" + index, firstNum);
            }
          }
          if (number.indexOf(lastNum) >= 0)
          {
            index = number.indexOf(lastNum) + 1;
            
            _duoi.put(_giai + "_index_" + index, lastNum);
            while (number.substring(index).indexOf(lastNum) >= 0)
            {
              index += number.substring(index).indexOf(lastNum) + 1;
              _duoi.put(_giai + "_index_" + index, lastNum);
            }
          }
        }
        //??? = _dau.entrySet().iterator(); continue;Map.Entry<String, String> entry = (Map.Entry)???.next();
        
        String first = (String)entry.getKey();
        for (Map.Entry<String, String> e : _duoi.entrySet())
        {
          String last = (String)e.getKey();
          
          bridge.put(first + "_VS_" + last, loto);
          bridge.put(last + "_VS_" + first, loto);
        }
      }
      TOTAL_BRIDGES.put(prevDate, bridge);
    }
    return TOTAL_BRIDGES;
  }
  
  public Map<String, Object> readDaily(String file, String curDate)
  {
    Map<String, Object> result = new LinkedHashMap();
    try
    {
      FileInputStream fstream = new FileInputStream(file);
      
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String sDate = "";
      
      int soNgay = 0;
      int soGiai = 0;
      int soGiaiPhu = 0;
      int n = 0;
      boolean isFinish = false;
      String strLine;
      while ((strLine = br.readLine().trim()) != null)
      {
        String strLine;
        if (!strLine.trim().isEmpty())
        {
          Map<String, String> cac_giai_cua_ngay = new LinkedHashMap();
          if (strLine.startsWith("==="))
          {
            if (isFinish) {
              break;
            }
            strLine = strLine.replace("=", "");
            
            strLine = strLine.replace("Ngay", "");
            sDate = strLine.trim();
            result.put(sDate, cac_giai_cua_ngay);
            if (sDate.trim().equals(curDate.trim())) {
              isFinish = true;
            }
            soNgay++;
            soGiai = 1;
            soGiaiPhu = 1;
            n = 1;
          }
          else
          {
            cac_giai_cua_ngay = (Map)result.get(sDate);
            if (soNgay > 70) {
              break;
            }
            String ten_giai = getTenGiai(soGiai, soGiaiPhu).trim();
            
            String giai = strLine.trim();
            
            cac_giai_cua_ngay.put(ten_giai, giai);
            result.put(sDate, cac_giai_cua_ngay);
            if (curDate.equals("")) {
              lastDate = sDate;
            }
            if (ten_giai.equalsIgnoreCase("G.DB")) {
              if (soNgay > 1) {
                DB.add(giai);
              }
            }
            if (isChangeGiai(n))
            {
              soGiai++;
              soGiaiPhu = 0;
            }
            n++;
            soGiaiPhu++;
          }
        }
      }
    }
    catch (NullPointerException npe)
    {
      System.out.println("Done");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }
  
  public String getTenGiai(int soGiai, int soGiaiPhu)
  {
    String ten_giai = "";
    switch (soGiai)
    {
    case 1: 
      ten_giai = "G.DB";
      break;
    case 2: 
      ten_giai = "G.1." + soGiaiPhu;
      break;
    case 3: 
      ten_giai = "G.2." + soGiaiPhu;
      break;
    case 4: 
      ten_giai = "G.3." + soGiaiPhu;
      break;
    case 5: 
      ten_giai = "G.4." + soGiaiPhu;
      break;
    case 6: 
      ten_giai = "G.5." + soGiaiPhu;
      break;
    case 7: 
      ten_giai = "G.6." + soGiaiPhu;
      break;
    case 8: 
      ten_giai = "G.7." + soGiaiPhu;
      break;
    }
    return ten_giai;
  }
  
  public boolean isChangeGiai(int giaiThu)
  {
    switch (giaiThu)
    {
    case 1: 
      return true;
    case 2: 
      return true;
    case 4: 
      return true;
    case 10: 
      return true;
    case 14: 
      return true;
    case 20: 
      return true;
    case 23: 
      return true;
    }
    return false;
  }
}

