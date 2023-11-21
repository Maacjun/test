package edu.jit.nsi.iot_ms.config;


import com.fasterxml.jackson.core.type.TypeReference;
import edu.jit.nsi.iot_ms.commons.util.JacksonUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;

/**
 * @packageName: com.jit.iot.utils.hardware
 * @className: JsonUtils
 * @Description:
 * @author: xxz
 * @date: 2019/7/26 21:16
 */
@Data
@Log4j2
@Configuration
public class SensorCmdCfg {
    public List<SensorCmd<SensorValue>> sensorList;
    public List<FixedSensor<RegParam>> fixsnrList;
    public List<RelayCtlCmd> relaylist;
    public List<EquipType> equiplist;
    public List<TermType> termlist;
    public List<AgriCellType> cellist;
    public List<EnvParamDesc> evdesist;
    public List<EnvThrsdParam> evctlist;

    private Map<String, FixedSensor> fixedSensorMap;
    private Map<String, SensorCmd> sensorValueMap;
    private Map<Integer, RegParam> seeedMap = new HashMap<>();
    private Map<Integer, RegParam> ep400Map = new HashMap<>();
    private Map<Integer, RegParam> easycommMap = new HashMap<>();
    private Map<String, EnvParamDesc> envDesMap = new HashMap<>();
    private Map<String, EnvThrsdParam> envCtlParmMap = new HashMap<>();

    private StringBuilder all;
//
//    @Value(value = "classpath:def_sensor_type.json")
//    private Resource sensorRes;
//
//    @Value(value = "classpath:dmaCtl.json")
//    private Resource dmaRes;
//
//    @Value(value = "classpath:equipment_type.json")
//    private Resource eqipRes;

    public SensorCmdCfg() {
        all = new StringBuilder();
        sensorList = new ArrayList<>();
        fixsnrList = new ArrayList<>();
        relaylist = new ArrayList<>();
        equiplist = new ArrayList<>();
        termlist = new ArrayList<>();
        cellist = new ArrayList<>();
        evdesist = new ArrayList<>();
        evctlist = new ArrayList<>();
        fixedSensorMap = new HashMap<>();
        sensorValueMap = new HashMap<>();
        initCfgFromFile();
    }

    //查询所有的厂家
    public List<TermType> getManus(){
        return termlist;
    }

    //查询某厂家下所有的终端产品
    public Set<String> getProductsByManu(String manu){
        Iterator<TermType> itor = termlist.iterator();
        while(itor.hasNext()){
            TermType term = itor.next();
            if(term.getManu().equals(manu)){
                return term.getProducts();
            }
        }
        return null;
    }

    //查询某厂家下所有的终端产品
    public Set<String> getProductsbyID(int typeid){
        Iterator<TermType> itor = termlist.iterator();
        while(itor.hasNext()){
            TermType term = itor.next();
            if(term.getId() == typeid){
                return term.getProducts();
            }
        }
        return null;
    }

    //查询终端默认的上报周期
//    public int getManuPeriod(int typeid){
//        Iterator<TermType> itor = termlist.iterator();
//        while(itor.hasNext()){
//            TermType term = itor.next();
//            if(term.getId() == typeid){
//                return term.getDtcycle();
//            }
//        }
//        return 300;
//    }

    //查询生产单元类型
    public List<AgriCellType> getCellType(){
        return cellist;
    }
    //查询生产单元下的农作物
    public List<String> getAgriProducts(String ctype){
        Iterator<AgriCellType> itor = cellist.iterator();
        while(itor.hasNext()){
            AgriCellType cell = itor.next();
            if(cell.getCelltype().equals(ctype)){
                return cell.getAgprods();
            }
        }
        return null;
    }

    private void initCfgFromFile(){
        InputStream stream = null;
        sensorList.clear();
        all.setLength(0);
        all = readFile("json/def_sensor_type.json");
//        all = ReadFileUtils.readFile("json/def_sensor_type.json");
//        all = readRes(sensorRes);
//        stream = getClass().getClassLoader().getResourceAsStream("/json/def_sensor_type.json");
//        all = ReadFileUtils.readFileStream(stream);
        if(all!=null){
            sensorList = JacksonUtils.readValue(all.toString(), new TypeReference<List<SensorCmd<SensorValue>>>() { });
            for(SensorCmd scmd : sensorList){
                sensorValueMap.put(scmd.getType(), scmd);
            }
        }

        fixsnrList.clear();
        all.setLength(0);
        all = readFile("json/fixed_sensor_type.json");
        if(all!=null){
            fixsnrList = JacksonUtils.readValue(all.toString(), new TypeReference<List<FixedSensor<RegParam>>>() { });
            for(FixedSensor fixed : fixsnrList) {
                fixedSensorMap.put(fixed.getProduct(), fixed);
                int manuid = fixed.getManuid();
                for(RegParam rp : (List<RegParam>) fixed.getParams()){
                    if(rp==null)
                        break;
                    switch (manuid){
                        case 2:
                            seeedMap.put(rp.getReg(), rp);
                            break;
                        case 3:
                            ep400Map.put(rp.getReg(), rp);
                            break;
                        case 4:
                            easycommMap.put(rp.getReg(),rp);
                            break;
                    }
                }
            }
        }

        relaylist.clear();
        all.setLength(0);
        all = readFile("json/dmaCtl.json");
        if(all!=null){
            relaylist = JacksonUtils.readValue(all.toString(), new TypeReference<List<RelayCtlCmd>>() { });
        }

        equiplist.clear();
        all.setLength(0);
//        all = readRes(eqipRes);
        all = readFile("json/equipment_type.json");
        if(all!=null){
            equiplist = JacksonUtils.readValue(all.toString(), new TypeReference<List<EquipType>>() { });
        }

        termlist.clear();
        all.setLength(0);
        all = readFile("json/term_type.json");
        if(all!=null){
            termlist = JacksonUtils.readValue(all.toString(), new TypeReference<List<TermType>>() { });
            for(TermType tt: termlist){
                if(tt.getId()==1||tt.getId()==5){
                    tt.setProducts(sensorValueMap.keySet());
                }else {
                    Set<String> prods = new HashSet();
                    for(FixedSensor fix:fixsnrList){
                        if(fix.getManuid()==tt.getId()){
                            prods.add(fix.getProduct());
                        }
                    }
                    tt.setProducts(prods);
                }
            }
        }

        cellist.clear();
        all.setLength(0);
        all = readFile("json/cell_type.json");
        if(all!=null){
            cellist = JacksonUtils.readValue(all.toString(), new TypeReference<List<AgriCellType>>() { });
        }

        envDesMap.clear();
        all.setLength(0);
        all= readFile("json/envparam_desc.json");
        if(all!=null){
            evdesist = JacksonUtils.readValue(all.toString(), new TypeReference<List<EnvParamDesc>>() { });
            for(EnvParamDesc desc :evdesist){
                envDesMap.put(desc.getEnv(), desc);
            }
        }

        envCtlParmMap.clear();
        all.setLength(0);
        all= readFile("json/envctl_param.json");
        if(all!=null){
            evctlist = JacksonUtils.readValue(all.toString(), new TypeReference<List<EnvThrsdParam>>() { });
            for(EnvThrsdParam ctlParam :evctlist){
                envCtlParmMap.put(ctlParam.getEnv(), ctlParam);
            }
        }
    }

    public List<RegParam> getFixedParams(String prod) {
        if(fixedSensorMap.get(prod)!=null)
            return fixedSensorMap.get(prod).getParams();
        else
            return null;
    }

    public List<RegParam> getDefParms(String prod) {
        List<RegParam> regParamList = new ArrayList<>();
        SensorCmd defcmd = sensorValueMap.get(prod);
        List<Integer> reglist = defcmd.getReg();
        for(int reg : reglist){
            int sv_size = defcmd.getLen();
            if(defcmd.getType().toLowerCase().contains("relay")){
                if(sv_size>=8)
                    sv_size /= 8;
                else if (sv_size==4)
                    sv_size=1;
            }else if(defcmd.getType().toLowerCase().contains("illu_dj")||
                    defcmd.getType().toLowerCase().contains("illu_jc")||
                    defcmd.getType().toLowerCase().contains("energy_dlx")||
                    defcmd.getType().toLowerCase().contains("wcd_jc")){
                sv_size = 1;
            }
            for(int i=0;i<sv_size;i++){
                SensorValue sv = (SensorValue)defcmd.getRspvalue().get(i);
                regParamList.add(new RegParam(reg++, sv.getUnit(), sv.getStype()));
            }
        }
        return regParamList;
    }

    public String getSeeedFrameType(int reg){
        if(seeedMap.get(reg)!=null)
            return seeedMap.get(reg).getEnvparam();
        else
            return null;
    }

    public int getSeeedFrameUnion(int reg){
        if(seeedMap.get(reg)!=null)
            return seeedMap.get(reg).getUnit();
        else
            return 1;
    }

    public String getEP400FrameType(int reg){
        return ep400Map.get(reg).getEnvparam();
    }

    public int getEP400FrameUnion(int reg){
        return ep400Map.get(reg).getUnit();
    }

    public String getEasycommFrameType(int reg){
        return easycommMap.get(reg).getEnvparam();
    }

    public int getEasycommFrameUnit(int reg){
        return easycommMap.get(reg).getUnit();
    }

    public String getEnvDesc(String env){
        return envDesMap.get(env).getDesc();
    }

    public String getEnvSuffix(String env){
        return envDesMap.get(env).getSuffix();
    }

    public boolean isEnvToUsr(String env){
//        log.info("env map {} contain env{}", envDesMap.containsKey(env), env);
        return envDesMap.get(env).isTo_usr();
    }

    //控制参数获取
    public float getEnvCtlWnUp(String env){
        return envCtlParmMap.get(env).getWnup();
    }
    public float getEnvCtlWnDw(String env){
        return envCtlParmMap.get(env).getWndw();
    }
    public float getEnvCtlActUp(String env){
        return envCtlParmMap.get(env).getActup();
    }
    public float getEnvCtlActDw(String env){
        return envCtlParmMap.get(env).getActdw();
    }

    private StringBuilder readFile(String filename) {
        //读取到静态资源文件
//        Resource resource = new ClassPathResource(filename);
        File file = null;
        StringBuilder all = new StringBuilder();
        try {
//            file = resource.getFile();
            //使用io读出数据
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename)));
            String str = null;
            while((str = br.readLine()) != null){
                all.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return all;
    }

    private StringBuilder readRes(Resource resource) {
        //读取到静态资源文件
        File file = null;
        StringBuilder all = new StringBuilder();
        try {
            file = resource.getFile();
            //使用io读出数据
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String str = null;
            while((str = br.readLine()) != null){
                all.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return all;
    }

}
