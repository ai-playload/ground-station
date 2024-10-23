package java.com.example.ground_station.presentation.ability.tts;

/**
 * @Desc:
 * @Author leon
 * @Date 2024/1/23-22:54
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
public class TtsSpeechPrams {
    private String vcn =  "xiaofeng"; //设置发音人
    private int pitch = 50; //设置发音人音调
    private int volume = 80; //设置发音人音量
    private int speed = 50; //设置发音人语速
    private int language = 1; //语种

    public String getVcn() {
        return vcn;
    }

    public void setVcn(String vcn) {
        this.vcn = vcn;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "TtsSpeechPramsV2{" +
                "vcn='" + vcn + '\'' +
                ", pitch=" + pitch +
                ", volume=" + volume +
                ", speed=" + speed +
                ", language=" + language +
                '}';
    }
}
