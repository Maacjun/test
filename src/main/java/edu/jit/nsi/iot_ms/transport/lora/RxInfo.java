package edu.jit.nsi.iot_ms.transport.lora;
import lombok.Data;

/**
 * @packageName: xxz.vegetables.entry
 * @className: RxInfo
 * @Description:
 * @author: xxz
 * @date: 2019/12/19 12:58
 */

@Data
public class RxInfo {
    String gatewayID;
    String uplinkID;
    String name;
    int rssi;
    int loRaSNR;
    Location location;
}
