package dev.cashlo.robohongpt.voiceui;


import java.util.Iterator;
import java.util.List;

import jp.co.sharp.android.voiceui.VoiceUIVariable;


/**
 * VoiceUIVariable関連のUtilityクラス.
 */
public final class VoiceUIVariableUtil {

    //static クラスとして使用する.
    private VoiceUIVariableUtil(){}


    /**
     * 指定したtargetが含まれるか判定する.
     *
     * @param variableList variableリスト
     * @param target target属性のvalue値
     * @return {@code true} : 含む<br>
     *          {@code false} : 含まない
     */
    public static boolean isTarget(final List<VoiceUIVariable> variableList, final String target) {
        boolean result = false;
        for (int i = 0; i < variableList.size(); i++) {
            if (getVariableData(variableList, ScenarioDefinitions.ATTR_TARGET).equals(target)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 指定したtargetとfunctionが含まれるか判定する.
     *
     * @param variableList variableリスト
     * @param target target属性のvalue値
     * @param function function属性のvalue値
     * @return {@code true} : 含む<br>
     *          {@code false} : 含まない
     */
    public static boolean isTargetFuncution(final List<VoiceUIVariable> variableList, final String target, final String function) {
        boolean result = false;
        for (int i = 0; i < variableList.size(); i++) {
            if (getVariableData(variableList, ScenarioDefinitions.ATTR_TARGET).equals(target)) {
                if(getVariableData(variableList, ScenarioDefinitions.ATTR_FUNCTION).equals(function)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    /**
     * variableのリストから指定した名前のvariableに格納されているStringデータを取得する.
     *
     * @param variableList variableリスト
     * @param name 取得するvariableの名前
     * @return 指定したvariableに格納されているString型のvalue値.<br>
     *          {@code name}と一致するものがなくてもvariable空文字を返す.<br>
     *          {@code null}は返さない.
     */
    public static String getVariableData(final List<VoiceUIVariable> variableList, final String name) {
        String result = "";
        int index = getListIndex(variableList, name);
        if (index != -1) {
            result = variableList.get(index).getStringValue();
        }
        return result;
    }

    /**
     * variableのリストから指定した名前のvariableが格納されているindex値を取得する.
     *
     * @param variableList variableリスト
     * @param name 取得するvariableの名前
     * @return {@code index} : 格納されているindex値<br>
     *          {@code -1} : 指定した名前が存在しない
     */
    public static int getListIndex(final List<VoiceUIVariable> variableList, final String name) {
        int index = -1;
        int tmp = 0;
        Iterator<VoiceUIVariable> it = variableList.iterator();
        while (it.hasNext()) {
            VoiceUIVariable variable = it.next();
            if (variable.getName().equals(name)) {
                index = tmp;
                break;
            }
            tmp++;
        }
        return index;
    }
}

