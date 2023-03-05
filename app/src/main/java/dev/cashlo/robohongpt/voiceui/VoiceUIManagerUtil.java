package dev.cashlo.robohongpt.voiceui;

import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.co.sharp.android.voiceui.VoiceUIListener;
import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;

/**
 * VoiceUIManager関連のUtilityクラス.
 */
public class VoiceUIManagerUtil {
    public static final String TAG = VoiceUIManagerUtil.class.getSimpleName();

    /**
     * Country種別(Locale#getCountry()の値).
     */
    private static final String LOCALE_COUNTRY_JAPAN = "JP";
    private static final String LOCALE_COUNTRY_US = "US";
    private static final String LOCALE_COUNTRY_CHINA = "CN";
    private static final String LOCALE_COUNTRY_TAIWAN = "TW";
    private static final String LOCALE_COUNTRY_KOREA = "KR";

    //static クラスとして使用する.
    private VoiceUIManagerUtil(){}

    /**
     * 音声UIリスナー登録.<br>
     * {@link VoiceUIManager#registerVoiceUIListener} のラッパー関数.<br>
     *
     * @param vm {@link VoiceUIManager}
     * @param listener {@link VoiceUIListener}
     * @return registerVoiceUIListenerの実行結果
     * @see VoiceUIManager#registerVoiceUIListener(VoiceUIListener)
     */
    public static int registerVoiceUIListener (VoiceUIManager vm, VoiceUIListener listener) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        if (vm != null) {
            try {
                result = vm.registerVoiceUIListener(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed registerVoiceUIListener.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * 音声UIリスナー解除.<br>
     * {@link VoiceUIManager#unregisterVoiceUIListener} のラッパー関数.<br>
     *
     * @param vm {@link VoiceUIManager}
     * @param listener {@link VoiceUIListener}
     * @return unregisterVoiceUIListenerの実行結果
     * @see VoiceUIManager#unregisterVoiceUIListener(VoiceUIListener)
     */
    public static int unregisterVoiceUIListener (VoiceUIManager vm, VoiceUIListener listener) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        if (vm != null) {
            try {
                result = vm.unregisterVoiceUIListener(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed unregisterVoiceUIListener.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * sceneを有効にする.<br>
     * 指定のsceneを1つだけ有効化するのみであり、複数指定も発話指定もしない.<br>
     *
     * @param vm {@link VoiceUIManager}
     * @param scene 有効にするscene名.
     *              {@code null}や空文字の場合は {@code VoiceUIManager.VOICEUI_ERROR} を返す.
     * @return updateAppInfoの実行結果
     * @see VoiceUIManager#updateAppInfo(List)
     */
    public static int enableScene(VoiceUIManager vm, final String scene) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        // 引数チェック.
        if (vm == null || scene == null || "".equals(scene)) {
            return result;
        }
        VoiceUIVariable variable = new VoiceUIVariable(ScenarioDefinitions.TAG_SCENE, scene);
        variable.setExtraInfo(VoiceUIManager.SCENE_ENABLE);
        ArrayList<VoiceUIVariable> listVariables = new ArrayList<>();
        listVariables.add(variable);
        try {
            result = vm.updateAppInfo(listVariables);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
        }
        return result;
    }

    /**
     * sceneを無効にする.<br>
     * 指定のsceneを1つだけ無効にするのみであり、複数指定も発話指定もしない.<br>
     *
     * @param vm {@link VoiceUIManager}
     * @param scene 有効にするscene名.
     *              {@code null}や空文字の場合は {@code VoiceUIManager.VOICEUI_ERROR} を返す.
     * @return updateAppInfoの実行結果
     * @see VoiceUIManager#updateAppInfo(List)
     */
    public static int disableScene(VoiceUIManager vm, final String scene) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        // 引数チェック.
        if (vm == null || scene == null || "".equals(scene)) {
            return result;
        }
        VoiceUIVariable variable = new VoiceUIVariable(ScenarioDefinitions.TAG_SCENE, scene);
        variable.setExtraInfo(VoiceUIManager.SCENE_DISABLE);
        ArrayList<VoiceUIVariable> listVariables = new ArrayList<VoiceUIVariable>();
        listVariables.add(variable);
        try {
            result = vm.updateAppInfo(listVariables);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
        }
        return result;
    }

    /**
     * 発話を中止する.<br>
     * {@link VoiceUIManager#stopSpeech} のラッパー関数.
     */
    public static void stopSpeech() {
        try {
            VoiceUIManager.stopSpeech();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed StopSpeech.[" + e.getMessage() + "]");
        }
    }

    /**
     * memory_pに値を記憶する.<br>
     *
     * @param vm {@link VoiceUIManager}
     * @param key memory_pのkey名(${memory_p:key}のkey部分).
     * @param value memory_pに記憶する値.
     * @return updateAppInfoの実行結果
     * @see VoiceUIManager#updateAppInfo(List)
     */
    public static int setMemory(VoiceUIManager vm, final String key, final String value) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        String name;
        if (vm == null || key == null || "".equals(key)) {
            return result;
        }else{
            name = ScenarioDefinitions.TAG_MEMORY_P + key;
        }
        VoiceUIVariable variable = new VoiceUIVariable(name, value);
        ArrayList<VoiceUIVariable> variables = new ArrayList<VoiceUIVariable>();
        variables.add(variable);
        try {
            result = vm.updateAppInfo(variables);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
        }
        return result;

    }

    /**
     * memory_pの値を削除する.<br>
     * {@link VoiceUIManager#removeVariable} のラッパー関数.
     * <br>
     * @param vm {@link VoiceUIManager}
     * @param key memory_pのkey名(${memory_p:key}のkey部分).
     * @return removeVariableの実行結果
     * @see VoiceUIManager#updateAppInfo(List)
     */
    public static int clearMemory(VoiceUIManager vm, final String key) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        if (vm != null) {
            try {
                result = vm.removeVariable(key);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed removeVariable.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * 音声認識の言語を設定する.<br>
     * {@link VoiceUIManager#setAsrLanguage}のラッパー関数
     *
     * @param vm {@link VoiceUIManager}
     * @param locale 指定言語種別({@link Locale})
     * @return setAsrLanguageの実行結果
     * @see VoiceUIManager#setAsrLanguage
     */
    public static int setAsr(VoiceUIManager vm, Locale locale) {
        String lang;
        String country;
        int result = VoiceUIManager.VOICEUI_ERROR;
        if(vm == null) return result;
        if(locale == null){
            country = Locale.getDefault().getCountry();
        }else{
            country = locale.getCountry();
        }
        if(LOCALE_COUNTRY_JAPAN.equals(country)){
            lang = VoiceUIManager.LANG_JAPANESE;
        }else if(LOCALE_COUNTRY_US.equals(country)){
            lang = VoiceUIManager.LANG_ENGLISH;
        }else if(LOCALE_COUNTRY_CHINA.equals(country) || LOCALE_COUNTRY_TAIWAN.equals(country)){
            lang = VoiceUIManager.LANG_CHINESE;
        }else{
            Log.w(TAG, "setAsr() : unexpected language");
            lang = VoiceUIManager.LANG_JAPANESE;
        }
        try {
            result = vm.setAsrLanguage(lang);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed setAsrLanguage.[" + e.getMessage() + "]");
        }
        return result;
    }

    /**
     * 音声発馬の言語を設定する.<br>
     * {@link VoiceUIManager#setTtsLanguage}のラッパー関数
     *
     * @param vm {@link VoiceUIManager}
     * @param locale 指定言語種別({@link Locale})
     * @return setTtsLanguageの実行結果
     * @see VoiceUIManager#setTtsLanguage
     */
    public static int setTts(VoiceUIManager vm, Locale locale) {
        String lang;
        String country;
        int result = VoiceUIManager.VOICEUI_ERROR;
        if(vm == null) return result;

        if (locale == null) {
            country = Locale.getDefault().getCountry();
        } else {
            country = locale.getCountry();
        }
        if (LOCALE_COUNTRY_JAPAN.equals(country)) {
            lang = VoiceUIManager.LANG_JAPANESE;
        } else if (LOCALE_COUNTRY_US.equals(country)) {
            lang = VoiceUIManager.LANG_ENGLISH;
        } else if (LOCALE_COUNTRY_CHINA.equals(country) || LOCALE_COUNTRY_TAIWAN.equals(country)) {
            lang = VoiceUIManager.LANG_CHINESE;
        } else {
            Log.w(TAG, "setTts() : unexpected language");
            lang = VoiceUIManager.LANG_JAPANESE;
        }
        try {
            result = vm.setTtsLanguage(lang);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed setTtsLanguage.[" + e.getMessage() + "]");
        }
        return result;
    }
}

