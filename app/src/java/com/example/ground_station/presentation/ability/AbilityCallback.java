package java.com.example.ground_station.presentation.ability;

public interface AbilityCallback {

    /**
     * 开始
     */
    void onAbilityBegin();

    /**
     * 能力结果输出
     * @param result 结果
     */
    void onAbilityResult(String result);

    /**
     * 结束
     * @param code 错误码
     * @param error 异常
     */
    void onAbilityError(int code, Throwable error);

    /**
     * 能力结束
     */
    void onAbilityEnd();
}