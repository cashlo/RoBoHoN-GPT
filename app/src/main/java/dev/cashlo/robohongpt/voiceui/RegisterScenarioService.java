package dev.cashlo.robohongpt.voiceui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.co.sharp.android.voiceui.VoiceUIManager;

/**
 * シナリオ登録するためのサービス.
 * <p>
 * <p>assetsにあるシナリオをアプリローカルにコピーして登録する</p>
 */
public class RegisterScenarioService extends Service {
    private static final String TAG = RegisterScenarioService.class.getSimpleName();
    /**
     * サービスで実行するコマンド：シナリオの登録.
     */
    protected static final int CMD_REQUEST_SCENARIO = 10;
    /**
     * サービスで実行可能なコマンドのキー名.
     */
    private static final String NAME_KEY_COMMAND = "key_cmd";
    /**
     * シナリオ用ルートフォルダー名.
     */
    private static final String SCENARIO_FOLDER_DEFAULT = "hvml";
    /**
     * home用シナリオフォルダー名.
     */
    private static final String SCENARIO_FOLDER_HOME = "home";
    /**
     * other用シナリオフォルダー名.
     */
    private static final String SCENARIO_FOLDER_OTHER = "other";
    /**
     * 言語設定取得用 Extra Key.
     */
    private static final String KEY_LOCALE = "locale";
    /**
     * 言語設定取得用 Extra value(日本語).
     */
    private static final String VALUE_LOCALE_JAPAN = "ja_JP";
    /**
     * 言語設定取得用 Extra value(英語).
     */
    private static final String VALUE_LOCALE_US = "en_US";
    /**
     * 言語設定取得用 Extra value(中国語).
     */
    private static final String VALUE_LOCALE_CHINA = "zh_CN";
    /**
     * 言語設定取得用 Extra value(台湾語).
     */
    private static final String VALUE_LOCALE_TAIWAN = "zh_TW";
    /**
     * 言語設定取得用 Extra value(韓国語).
     */
    private static final String VALUE_LOCALE_KOREA = "ko_KR";
    /**
     * VoiceUIManager
     */
    private VoiceUIManager mVUIManager;

    public RegisterScenarioService() {
    }

    /**
     * サービスにコマンドを送信する.
     *
     * @param context    コンテキスト
     * @param baseIntent ベースとなるintent
     * @param command    コマンドの指定
     */
    public static void start(Context context, Intent baseIntent, int command) {
        baseIntent.putExtra(NAME_KEY_COMMAND, command);
        baseIntent.setClass(context, RegisterScenarioService.class);
        context.startService(baseIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mVUIManager == null) {
            mVUIManager = VoiceUIManager.getService(getApplicationContext());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = intent.getIntExtra(NAME_KEY_COMMAND, -1);
        if (cmd == -1) {
            Log.e(TAG, "onStartCommand:not app key_command");
            return Service.START_NOT_STICKY;
        }

        Log.d(TAG, "onStartCommand cmd:" + cmd);
        switch (cmd) {
            //シナリオ登録コマンド受信.
            case CMD_REQUEST_SCENARIO:
                //locale情報を取得.
                String locale = intent.getStringExtra(KEY_LOCALE);
                //homeシナリオ登録.
                registerScenario(locale, true);
                //home以外のシナリオ登録.
                registerScenario(locale, false);
                stopSelf();
                break;
            default:
                break;
        }
        return Service.START_NOT_STICKY;
    }

    /**
     * シナリオの登録を行う.
     */
    private void registerScenario(String locale, Boolean home) {
        Log.d(TAG, "registerScenario-S: " + locale + " : " + home.toString());

        //ローカルフォルダー作成.
        File localFolder = null;
        if (home) {
            localFolder = this.createLocalFolder(SCENARIO_FOLDER_HOME);
        } else {
            localFolder = this.createLocalFolder(SCENARIO_FOLDER_OTHER);
        }
        if (localFolder == null) {
            Log.e(TAG, "can not make local folder");
            return;
        }

        //assetsフォルダー名取得.
        String assetsFolderName = this.getAssetsScenarioFolderName(locale, home);

        //assetsフォルダー内のファイル名リストを取得.
        final AssetManager assetManager = getResources().getAssets();
        String[] fileList = null;
        try {
            fileList = assetManager.list(assetsFolderName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final String hvmlPrefix = getPackageName().replace(".","_");

        //assetsからローカルへhvmlファイルをコピー.ただしHVMLファイル命名規則違反のファイルはコピーしない
        for (String fileName : fileList) {
            if (fileName.endsWith(".hvml")
                && fileName.startsWith(hvmlPrefix)
                && isAvailableFileName(fileName) ) {
                Log.d(TAG, "hvml files = " + fileName);
                this.copyFileFromAssetsToLocal(assetsFolderName, localFolder.getPath(), fileName);
            }
        }

        //ローカルフォルダーのファイル名リストを取得.
        File[] files = localFolder.listFiles();

        //ローカルフォルダーのhvmlファイルのシナリオを登録する.
        for (File file : files) {
            Log.d(TAG, "registerScenario file=" + file.getAbsolutePath());
            int result = VoiceUIManager.VOICEUI_ERROR;
            try {
                if (home) {
                    //home用.
                    result = mVUIManager.registerHomeScenario(file.getAbsolutePath());
                    if (result == VoiceUIManager.VOICEUI_ERROR)
                        Log.e(TAG, "registerScenario:Error");
                } else {
                    //other.
                    result = mVUIManager.registerScenario(file.getAbsolutePath());
                    if (result == VoiceUIManager.VOICEUI_ERROR)
                        Log.e(TAG, "registerScenario:Error");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "registerScenario-E:" + home.toString());
    }

    /**
     * assetsフォルダー(シナリオ)名取得.
     * <p>LocaleとHOME/OTHER指定によってassetsフォルダ名を決める</p>
     * <p>指定のassetsフォルダが存在しない場合はデフォルトのフォルダ名を返す</p>
     */
    private String getAssetsScenarioFolderName(String locale, Boolean home) {
        String result = "";
        //locale指定が無い場合はhvmlフォルダ(日本語)を利用する
        if (locale == null || "".equals(locale)) {
            if (home) {
                result = SCENARIO_FOLDER_DEFAULT + "/" + SCENARIO_FOLDER_HOME;
            } else {
                result = SCENARIO_FOLDER_DEFAULT + "/" + SCENARIO_FOLDER_OTHER;
            }
        } else {
            //hvml_<locale>の形式.
            result = SCENARIO_FOLDER_DEFAULT + "_" + locale;
            final AssetManager assetManager = getResources().getAssets();
            String[] fileList = null;
            try {
                fileList = assetManager.list(result);
            } catch (IOException e) {
                e.printStackTrace();
                result = SCENARIO_FOLDER_DEFAULT;
            }
            //locale指定があるが、該当フォルダが存在しない場合はhvmlフォルダを利用する
            if (fileList == null || fileList.length == 0) {
                Log.w(TAG, "not exist locale folder");
                if(VALUE_LOCALE_TAIWAN.equals(locale)){
                    //locale指定台湾(繁体)の場合は中国(繁体)のフォルダも探してみる.
                    result = SCENARIO_FOLDER_DEFAULT + "_" + VALUE_LOCALE_CHINA;
                    try {
                        fileList = assetManager.list(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = SCENARIO_FOLDER_DEFAULT;
                    }
                    if (fileList == null || fileList.length == 0) {
                        result = SCENARIO_FOLDER_DEFAULT;
                        Log.w(TAG, "not exist locale folder(CHINA)");
                    }
                }else {
                    result = SCENARIO_FOLDER_DEFAULT;
                }
            }
            if (home) {
                result = result + "/" + SCENARIO_FOLDER_HOME;
            } else {
                result = result + "/" + SCENARIO_FOLDER_OTHER;
            }
        }
        Log.d(TAG, "getAssetsScenarioFolderName() result : " + result);
        return result;
    }

    /**
     * ローカルフォルダー作成.
     */
    private File createLocalFolder(String childPath) {
        File folder = null;
        try {
            folder = new File(this.getApplicationContext().getFilesDir(), childPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }else{
                //既にフォルダがある場合は中身ごとフォルダ削除してから再度ファルダを作成する.
                Log.d(TAG, "local folder already exists");
                try {
                    recursiveDeleteFile(folder);
                }catch(Exception e){
                    Log.e(TAG, "recursiveDeleteFile exception : " + e.getMessage());
                }
                folder.mkdirs();
            }
            folder.setReadable(true, false);
            folder.setWritable(true, false);
            folder.setExecutable(true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folder;
    }

    /**
     * assetsからローカルへファイルをコピー.
     */
    private void copyFileFromAssetsToLocal(String assetsFolderName, String localFolderName, String fileName) {
        File assetsFile = null;
        InputStream inputStream = null;
        File localFile = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = null;
        try {
            //   AssetsフォルダのファイルOpen
            assetsFile = new File(assetsFolderName, fileName);
            inputStream = getResources().getAssets().open(assetsFile.getPath());

            //   ローカルフォルダーにファイル作成
            localFile = new File(localFolderName, fileName);
            fileOutputStream = new FileOutputStream(localFile.getPath());
            localFile.setReadable(true, false);
            localFile.setWritable(true, false);
            localFile.setExecutable(true, false);
            buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) >= 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                }
            }
            fileOutputStream = null;

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
            inputStream = null;
            buffer = null;
            assetsFile = null;
            localFile = null;
            assetsFile = null;
        }
    }

    /**
     * 日本語ファイル名のhvmlファイルが含まれるかチェックする.
     *
     * Shift-JISの日本語はFile.list()の時点で死ぬので防げるのはUTF-8のときのみ.
     * (adb pushでSDに格納すると文字コード変換されないので危険).
     */
    private static boolean isAvailableFileName(final String fileName) {
        if (null == fileName) {
            return false;
        }
        if ("".equals(fileName)) {
            return false;
        }
        // ASCII以外は使用不可.
        final String ASCII = "^[\\u0020-\\u007E]+$";
        if (!fileName.matches(ASCII)) {
            Log.e(TAG, "ASCII NG!!!:" + fileName);
            return false;
        }
        // 次の9文字は使用不可(< > : * ? " / \ |).
        final String regularExpression = "^.*[(<|>|:|\\*|?|\"|/|\\\\|\\|)].*$";
        if (fileName.matches(regularExpression)) {
            Log.e(TAG, "regular NG!!!:" + fileName);
            return false;
        }
        return true;
    }

    /**
     * フォルダ削除.
     * フォルダ内のファイル・フォルダも全て削除する.
     */
    private static void recursiveDeleteFile(final File file) throws Exception {
        // 存在しない場合は処理終了
        if (!file.exists()) {
            return;
        }
        // 対象がディレクトリの場合は再帰処理
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                recursiveDeleteFile(child);
            }
        }
        // 対象がファイルもしくは配下が空のディレクトリの場合は削除する
        file.delete();
    }

}
