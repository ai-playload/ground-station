package java.com.example.ground_station.data.model;

import java.io.Serializable;

public class SendFunctionProvider implements Serializable {

    public String msgId2;
    public String[] ps;
    public SendFunctionProvider(String msgId2, String... ps){
        this.msgId2 = msgId2;
        this.ps = ps;
    }
}
