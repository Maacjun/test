package edu.jit.nsi.iot_ms.transport.msg;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class TermStaLst {
    private int onnum;    //在线个数
    private int offnum;    //离线个数
    List<Integer> onlst;   //在线列表
    List<Integer> offlst;  //离线列表

    public TermStaLst(){
        onlst=new ArrayList<>();
        offlst=new ArrayList<>();
    }

    public void addOnTerm(int e){
        onlst.add(e);
        onnum++;
    }

    public void addOffTerm(int e){
        offlst.add(e);
        offnum++;
    }
}
