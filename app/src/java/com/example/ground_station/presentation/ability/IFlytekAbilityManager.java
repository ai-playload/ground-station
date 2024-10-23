package java.com.example.ground_station.presentation.ability;

import android.content.Context;
import android.util.Log;
import com.example.ground_station.R;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.BaseLibrary;
import com.iflytek.aikit.core.ErrType;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class IFlytekAbilityManager {

    private static final int AUTH_INTERVAL = 333;

    private static volatile IFlytekAbilityManager instance;

    private IFlytekAbilityManager() {
    }

    public static IFlytekAbilityManager getInstance() {
        if (instance == null) {
            synchronized (IFlytekAbilityManager.class) {
                if (instance == null) {
                    instance = new IFlytekAbilityManager();
                }
            }
        }
        return instance;
    }

    public void initializeSdk(Context context) {
        File workDir = new File("/sdcard/iflytekAikit");
        if (!workDir.exists()) {
            boolean created = workDir.mkdirs();
            if (!created) {
//                throw new RuntimeException("Failed to create work directory: /sdcard/iflytekAikit");
                Log.e("initializeSdk", "Failed to create directory: " + workDir.getAbsolutePath());

            }
        }

        BaseLibrary.Params params = BaseLibrary.Params.builder()
                .appId(context.getResources().getString(R.string.appId))
                .apiKey(context.getResources().getString(R.string.apiKey))
                .apiSecret(context.getResources().getString(R.string.apiSecret))
                .workDir("/sdcard/iflytekAikit")
                .iLogMaxCount(1)
                .authInterval(AUTH_INTERVAL)
                .ability(engineIds())
                .build();

        AiHelper.getInst().registerListener((type, code) -> {
            Log.d("IFlytekAbilityManager", "引擎初始化状态 " + (type == ErrType.AUTH && code == 0));
        });

        new Thread(() -> AiHelper.getInst().init(context, params)).start();
    }

    private String engineIds() {
        List<String> engineIds = Arrays.asList(
                AbilityConstant.XTTS_ID,
//                AbilityConstant.IVW_ID,
//                AbilityConstant.ESR_ID,
                AbilityConstant.TTS_ID
//                AbilityConstant.ED_ENCN_ID,
        );
        return String.join(";", engineIds);
    }
}
