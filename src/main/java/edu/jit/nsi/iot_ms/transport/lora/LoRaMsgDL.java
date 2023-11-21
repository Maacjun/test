package edu.jit.nsi.iot_ms.transport.lora;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoRaMsgDL {
    boolean confirmed;
    int fPort;
    String data;
}
