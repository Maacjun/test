package edu.jit.nsi.iot_ms.config;


import edu.jit.nsi.iot_ms.config.InfluxDBConnect;
import edu.jit.nsi.iot_ms.domain.EnvirDataDO;
import edu.jit.nsi.iot_ms.domain.EquipDO;
import edu.jit.nsi.iot_ms.transport.msg.WhrStnAnaly;
import edu.jit.nsi.iot_ms.transport.msg.WhrStnIlluIntgl;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @className: InfluxdbDaoImpl
 * @author: kay
 * @date: 2019/7/22 9:18
 * @packageName: com.jit.iot.lnfluxdbDao.Impl
 */
@Slf4j
@Service
public class InfluxdbDaoImpl {
    @Autowired
    private InfluxDB influx;

    @Value("${influxdb.database}")
    private String database;
    @Value("${influxdb.retention-policy}")
    private String retentionPolicy;  // 保留策略

    String phy_measurement = "phy_monitor";
    String param_measurement = "param_monitor";
    String whsta_measurement="params_downsamp_1day";
    String ctrl_measurement = "control";

    //插入data数据
    public void insertReport(String bioTagValue, List<EnvirDataDO> commons){
        //获取当前系统时间
        long time = new Date().getTime();
        Map<Integer, Map<String, Float>> phyMap = new HashMap<Integer, Map<String, Float>>();

        int termid= commons.get(0).getTermid();

        BatchPoints batchPoints = BatchPoints
                .database(database)
                .retentionPolicy(retentionPolicy)
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        for(EnvirDataDO envir : commons){

            int addr = envir.getAddr();
            String type = envir.getType();
            float value = envir.getValue();
            //添加phy
            if(!phyMap.containsKey(addr)){
                phyMap.put(addr, new HashMap<>());
            }
            phyMap.get(addr).put(type, value);

            //添加param
            Point papoint = Point.measurement(param_measurement)
                    //tag属性——只能存储String类型
                    .tag("bio", bioTagValue)
                    .tag("termid", String.valueOf(envir.getTermid()))
                    .tag("snpid", String.valueOf(envir.getSnpid()))
                    .tag("type", envir.getType())
                    .addField("value", value)
                    .build();
            batchPoints.point(papoint);
        }

        //添加phy sensor
        Iterator<Integer> addritor= phyMap.keySet().iterator();
        while (addritor.hasNext()){
            int addr = addritor.next();
            Point.Builder pointbuilder = Point.measurement(phy_measurement)
                    //tag属性——只能存储String类型
                    .tag("bio", bioTagValue)
                    .tag("termid", String.valueOf(termid))
                    .tag("addr", String.valueOf(addr));

            Map<String, Float> addrMaps= phyMap.get(addr);
            Iterator<String> typeitor = addrMaps.keySet().iterator();
            while(typeitor.hasNext()){
                String type = typeitor.next();
                pointbuilder.addField(type, addrMaps.get(type));
            }
            Point phypoint= pointbuilder.build();
            batchPoints.point(phypoint);
        }

        influx.write(batchPoints);
    }

    //查询统计数据
    public void queryAnaly(int termid, String type, String start_time, String end_time, List<WhrStnAnaly> whrStnAnaLst) {
        DecimalFormat df1 = new DecimalFormat("0.0");
        String command = "select count, min, mean, max, stddev from "+whsta_measurement+" where termid='"+termid+"' and type = '"+type+"' and time>='" + start_time + "' and time<='" + end_time + "' tz('Asia/Shanghai')" ;
        QueryResult queryResult = influx.query(new Query(command, database));
        if(queryResult.getResults()==null || queryResult.getResults().size()==0
                ||queryResult.getResults().get(0).getSeries()==null
                || queryResult.getResults().get(0).getSeries().size()==0)
            return;
        //snpid查询param_measurement只有一个结果
        Series series = queryResult.getResults().get(0).getSeries().get(0);
        List<String> columns = series.getColumns();
        int fieldSize = columns.size();
        series.getValues().forEach(value->{
            WhrStnAnaly whana  = new WhrStnAnaly();
            for(int i=0;i<fieldSize;i++){
                switch(columns.get(i)){
                    case "time":
                        String dt= (String)value.get(i);
                        whana.setTime(dt);
                        break;
                    case "count":
                        double cnt = (Double) value.get(i);
                        whana.setCnt((int)cnt);
                        break;
                    case "min":
                        Double dmin = (Double) value.get(i);
                        float min = Float.parseFloat(df1.format(dmin));
                        whana.setMin(min);
                        break;
                    case "mean":
                        Double dmean = (Double) value.get(i);
                        float mean = Float.parseFloat(df1.format(dmean));
                        whana.setMean(mean);
                        break;
                    case "max":
                        Double dmax = (Double) value.get(i);
                        float max = Float.parseFloat(df1.format(dmax));
                        whana.setMax(max);
                        break;
                    case "stddev":
                        Double dstd = (Double) value.get(i);
                        float std = Float.parseFloat(df1.format(dstd));
                        whana.setStddev(std);
                        break;
                    default:
                        break;
                }
            }
            whrStnAnaLst.add(whana);
        });
    }

    //统计日升日落时长数据
    public void querySuntime(int termid, String start_time, String end_time, List<String> timelst) {
        //select count(value) from param_monitor where termid='252' and type='illu' and value<0.1 and time>now()-5d group by time(5m) fill(none) tz('Asia/Shanghai')
        String command = "select count(value) from  "+param_measurement+" where termid='"+termid+"' and type = 'illu' and time>='" + start_time + "' and time<='" + end_time + "' and value<0.01 group by time(5m) fill(none) tz('Asia/Shanghai')" ;
        QueryResult queryResult = influx.query(new Query(command, database));
        if(queryResult.getResults()==null || queryResult.getResults().size()==0
                ||queryResult.getResults().get(0).getSeries()==null
                || queryResult.getResults().get(0).getSeries().size()==0)
            return;
        //snpid查询param_measurement只有一个结果
        Series series = queryResult.getResults().get(0).getSeries().get(0);
        List<String> columns = series.getColumns();
        int fieldSize = columns.size();
        series.getValues().forEach(value->{
            WhrStnAnaly whana  = new WhrStnAnaly();
            for(int i=0;i<fieldSize;i++){
                if(columns.get(i).equals("time")){
                    String dt= (String)value.get(i);
                    timelst.add(dt);
                }

            }
        });
    }

    //统计日辐射强度积分数据
    public void illuIntegral(int termid, String start_time, String end_time, List<WhrStnIlluIntgl> intglst) {
        //select INTEGRAL(value) from param_monitor where termid='252' and type='illu' and time>='2022-09-30 00:00:00' and time<'2022-10-01 00:00:00' tz('Asia/Shanghai')
        String command = "select INTEGRAL(value) from  " + param_measurement + " where termid='" + termid + "' and type = 'illu' and time>='" + start_time + "' and time<='" + end_time + "' group by time(1d)  tz('Asia/Shanghai')";
        QueryResult queryResult = influx.query(new Query(command, database));
        if (queryResult.getResults() == null || queryResult.getResults().size() == 0
                || queryResult.getResults().get(0).getSeries() == null
                || queryResult.getResults().get(0).getSeries().size() == 0) {
            return ;
        }
        //snpid查询param_measurement只有一个结果
        Series series = queryResult.getResults().get(0).getSeries().get(0);
        List<String> columns = series.getColumns();
        int fieldSize = columns.size();
        series.getValues().forEach(value->{
            WhrStnIlluIntgl dailyitgl = new WhrStnIlluIntgl();
            for(int i=0;i<fieldSize;i++){
                if(columns.get(i).equals("time")){
                    String dt= (String)value.get(i);
                    dailyitgl.setTime(dt);

                }else if(columns.get(i).equals("integral")){
                    double va = (Double) value.get(i);
                    dailyitgl.setRecords((float)va);
                }
            }
            intglst.add(dailyitgl);
        });
    }

    //查询monitor数据
    public void querySNTV(int snpid, String start_time, String end_time, List<Long> times, List<Float> values) {
        //数据格式转换
        String command = "select value from "+param_measurement+" where time>='" + start_time + "' and time<='" + end_time + "' and snpid='" +snpid+"' tz('Asia/Shanghai')" ;
        QueryResult queryResult = influx.query(new Query(command, database));

//        queryResult.getResults().forEach(result->{
//            result.getSeries().forEach(serial->{
//                List<String> columns = serial.getColumns();
//                int fieldSize = columns.size();
//                serial.getValues().forEach(value->{
//                    for(int i=0;i<fieldSize;i++){
//                        if(columns.get(i).equals("time")){
//                            times.add((Long) value.get(i));
//                        }else if(columns.get(i).equals("value")){
//                            values.add((float)value.get(i));
//                        }
//
//                    }
//                });
//            });
//        });

        if(queryResult.getResults()==null || queryResult.getResults().size()==0
                ||queryResult.getResults().get(0).getSeries()==null
                || queryResult.getResults().get(0).getSeries().size()==0)
            return;
        //snpid查询param_measurement只有一个结果
        Series series = queryResult.getResults().get(0).getSeries().get(0);
        List<String> columns = series.getColumns();
        int fieldSize = columns.size();
        series.getValues().forEach(value->{
            for(int i=0;i<fieldSize;i++){
                if(columns.get(i).equals("time")){
                    String dt= (String)value.get(i);
                    times.add(dateRFC3339String2Long(dt));
                }else if(columns.get(i).equals("value")){
                    Double va = (Double) value.get(i);
                    values.add(va.floatValue());
                }
            }
        });
    }




    public long dateRFC3339String2Long(String rfcStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
//        DateTimeFormatter SDFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Asia/Shanghai"));
        Date date = null;
        try {
            date = sdf.parse(rfcStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date!=null){
            return date.getTime()/1000;
        }else{
            return 0;
        }
    }

    //查询control的数据
    public Series findLog(int cell_id, long start_time, long end_time){
        //数据格式转换
        String start = String.valueOf(start_time) + "000000";
        String end = String.valueOf(end_time) + "000000";

        String command = "select equip_name,action from log where time>=" + start + " and time<=" + end + " and cell_id='" +cell_id+"'";
        return findInflux(command);
    }


    //查询
    public Series findInflux(String command){
        //InfluxDBConnect influx_conn = influxdbConfig.influxDbUtils();
        System.out.println("查询influx db：command=" + command);
        QueryResult queryResult = influx.query(new Query(command, database));
        Series series = queryResult.getResults().get(0).getSeries().get(0);
        return  series;
    }

}
