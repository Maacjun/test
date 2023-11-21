package edu.jit.nsi.iot_ms.transport.msg;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhrStnSunRiseSetAnaly {
    private String date;    //日期   2022-10-06
    private String rise;    //日出时间   06:10:00
    private String set;    //日落时间    17:50:00
    private int rise_5min;    //日出时间(5分钟为单位计数)   00:10:00  代表2
    private int set_5min;    //日落时间(5分钟为单位计数)    01:00:00 代表12
    private float  suntime;  //日照时长（单位小时）  11.3 hour

    public WhrStnSunRiseSetAnaly(String datestr){
        date=datestr;
    }
}
