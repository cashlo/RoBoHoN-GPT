package dev.cashlo.robohongpt.voiceui;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.co.sharp.android.voiceui.VoiceUIListener;
import jp.co.sharp.android.voiceui.VoiceUIVariable;

/**
 * 音声UIからの通知時の共通処理を実装するクラス.
 * ActivityやService毎の個別の処理はCallback内で実装する.
 * 時間を要する処理は避け、直ぐ抜けること.
 */
public class VoiceUIListenerImpl implements VoiceUIListener {
    private static final String TAG = VoiceUIListenerImpl.class.getSimpleName();

    private ScenarioCallback mCallback;

    /**
     * シナリオイベント種別定義(onVoiceUIEvent).
     */
    public static final int ACTION_START        = 0;
    /**
     * シナリオイベント種別定義(onVoiceUIActionEnd).
     */
    public static final int ACTION_END          = 1;
    /**
     * シナリオイベント種別定義(onVoiceUIResolveVariable).
     */
    public static final int RESOLVE_VARIABLE    = 2;
    /**
     * シナリオイベント種別定義(onVoiceUIActionCancelled).
     */
    public static final int ACTION_CANCELLED    = 3;
    /**
     * シナリオイベント種別定義(onVoiceUIRejection).
     */
    public static final int ACTION_REJECTED     = 4;

    /**
     * Activity側でのCallback実装チェック（実装してないと例外発生）.
     */
    public VoiceUIListenerImpl(Context context) {
        super();
        try {
            mCallback = (ScenarioCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + TAG);
        }
    }

    @Override
    public void onVoiceUIEvent(List<VoiceUIVariable> variables) {
        //controlタグからの通知(シナリオ側にcontrolタグのあるActionが開始されると呼び出される).
        //発話と同時にアプリ側で処理を実行したい場合はこちらを使う.
        Log.v(TAG, "onVoiceUIEvent");
        if (VoiceUIVariableUtil.isTarget(variables, ScenarioDefinitions.TARGET)) {
            mCallback.onScenarioEvent(ACTION_START, variables);
        }
    }

    @Override
    public void onVoiceUIActionEnd(List<VoiceUIVariable> variables) {
        //Actionの完了通知(シナリオ側にcontrolタグを書いたActionが完了すると呼び出される).
        //発話が終わった後でアプリ側の処理を実行したい場合はこちらを使う.
        Log.v(TAG, "onVoiceUIActionEnd");
        if (VoiceUIVariableUtil.isTarget(variables, ScenarioDefinitions.TARGET)) {
            mCallback.onScenarioEvent(ACTION_END, variables);
        }
    }

    @Override
    public void onVoiceUIResolveVariable(List<VoiceUIVariable> variables) {
        //アプリ側での変数解決用コールバック(シナリオ側にパッケージ名をつけた変数を書いておくと呼び出される).
        Log.v(TAG, "onVoiceUIResolveVariable");
        mCallback.onScenarioEvent(RESOLVE_VARIABLE, variables);
    }

    @Override
    public void onVoiceUIActionCancelled(List<VoiceUIVariable> variables) {
        //priorityが高いシナリオに割り込まれた場合の通知.
        Log.v(TAG, "onVoiceUIActionCancelled");
        if (VoiceUIVariableUtil.isTarget(variables, ScenarioDefinitions.TARGET)) {
            mCallback.onScenarioEvent(ACTION_CANCELLED, variables);
        }
    }

    @Override
    public void onVoiceUIRejection(VoiceUIVariable variable) {
        //priority負けなどで発話が棄却された場合のコールバック.
        Log.v(TAG, "onVoiceUIRejection : " + variable.getStringValue());
        List<VoiceUIVariable> variables = new ArrayList<VoiceUIVariable>();
        variables.add(variable);
        mCallback.onScenarioEvent(ACTION_REJECTED, variables);
    }

    @Override
    public void onVoiceUISchedule(int i) {
        //処理不要(リマインダーアプリ以外は使われない).
    }

    /**
     * Activityへの通知用IFクラス.
     */
    public static interface ScenarioCallback {
        /**
         * 実行されたcontrolの通知.
         *
         * @param event イベント種別.
         * @param variables シナリオからのコールバック情報.
         */
        public void onScenarioEvent(int event, List<VoiceUIVariable> variables);
    }

}
