package jp.co.atika.pepperrc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.Toast;

import java.util.ArrayList;

public class Cmn {

    // Activityクラス以外でもリソースを使うために保持する
    private Context mContext;
    private Resources mRes;

    // コンストラクタ
    public Cmn(Context c) {
        mContext = c;
        mRes = c.getResources();
    }

    // Activityから取得したリソースを返す
    public Resources getmRes() {
        return mRes;
    }

    // Activityから取得したコンテキストを返す
    public Context getContext() {
        return mContext;
    }

    /**
     * キーで指定した文字列を返す(string.xml内)。該当が無ければ空白
     *
     * @param key キー名。例「app_name」
     * @return 例「AsyzVH」
     */
    public String getString(String key) {
        int strId = mRes.getIdentifier(key, "string", mContext.getPackageName());
        return getString(strId);
    }

    /**
     * キーで指定した文字列を返す。該当が無ければ空白
     *
     * @param key キーID R.string.xx を想定
     * @return
     */
    public String getString(int key) {
        //int strId = mRes.getIdentifier("text", "string", mContext.getPackageName());
        String s = mRes.getString(key);
        return s == null ? "" : s;
    }

    /**
     * prefが正しく設定されているか返す
     *
     * @return エラーメッセージ
     */
    public String checkPref() {
        String ret = "";

        // pref値を取得する
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String mServerIp = mPreferences.getString(mRes.getString(R.string.pref_ip_key), "");
        String mKey = mPreferences.getString(mRes.getString(R.string.pref_port_key), "");
        // マスタは選択が無い場合もあるのでチェックしない
        // int pCntKankatu = getParamCnt("kankatu");
        // int pCntKiken = getParamCnt("akikiken");
        // int pCntKinkyu = getParamCnt("akikinkyu");
        if (mServerIp == null || mServerIp.length() == 0 ||
                mKey == null || mKey.length() == 0) {
            ret += mRes.getString(R.string.msg_err_pref_setting) + System.getProperty("line.separator");
            if (mServerIp == null || mServerIp.length() == 0) {
                ret += "　・" + mRes.getString(R.string.pref_ip_title) + System.getProperty("line.separator");
            }
            if (mKey == null || mKey.length() == 0) {
                ret += "　・" + mRes.getString(R.string.pref_login_title) + System.getProperty("line.separator");
            }
        }
        return ret;
    }

    // --------------------------------------------------------------------------------
    // データベース用
    // --------------------------------------------------------------------------------
    private final static String DB_NAME = "pe.db";
    private final static int DB_VERSION = 6;
    public final static String DB_TABLE_SAY = "say";

    /**
     * データベースを扱うためのヘルパメソッド
     */
    public static class DBHelper extends SQLiteOpenHelper {
        // Activityクラス以外でもリソースを使うために保持する
        private Resources mRes;
        /**
         * コンストラクタ
         *
         * @param context
         */
        public DBHelper(Context context) {
            // 1:データベースを所有するコンテキスト
            // 2:データベースファイルの名前。nullだとメモリ上
            // 3:null固定
            // 4:バージョン
            super(context, DB_NAME, null, DB_VERSION);

            mRes = context.getResources();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // デバッグ用
            db.execSQL("drop table if exists " + DB_TABLE_SAY);

            Cursor c = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='" + DB_TABLE_SAY + "'", null);
            c.moveToFirst();
            int cnt = c.getInt(0);
            if (cnt == 0) {
                // テーブルが存在しない
                db.execSQL("create table if not exists " + DB_TABLE_SAY +
                        "(no integer primary key, msg text)");
                // サンプルデータ
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (1, '" + mRes.getString(R.string.say_goodmorning) + "')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (2, '" + mRes.getString(R.string.say_hello) + "')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (3, '" + mRes.getString(R.string.say_thankyou) + "')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (4, '" + mRes.getString(R.string.say_goodbye) + "')");
                /*
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (1,'\\rspd=110\\\\vct=135\\いらっしゃいませ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (2,'\\rspd=110\\\\vct=135\\こんにちは。アチカへようこそ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (3,'\\rspd=110\\\\vct=135\\靴はコチラの靴棚へどうぞ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (4,'\\rspd=110\\\\vct=135\\ごめんなさい。靴は持てないんだよ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (5,'\\rspd=110\\\\vct=135\\会議室はコチラです。')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (6,'\\rspd=110\\\\vct=110\\一旦、 \\pau=100\\ \\rspd=120\\ \\vct=130\\ 失礼します')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (7,'\\rspd=110\\\\vct=115\\では、 \\pau=500\\\\rspd=115\\\\vct=140\\ まタ今度ーぉっ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (8,'\\rspd=125\\\\vct=135\\お疲れさまです')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (9,'\\rspd=100\\\\vct=140\\これからも\\vct=170\\\\rspd=110\\ヨロシクーーーーーっっ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (10,'\\rspd=115\\\\vct=140\\イッテラッしゃーーぁイっっ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (11,'\\rspd=115\\\\vct=140\\オカエリナサーーぁイっっ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (12,'\\rspd=110\\\\vct=145\\お矢スミナサーーーぃ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (13,'\\rspd=110\\\\vct=125\\みなさん\\rspd=110\\\\vct=135\\こんにちは！！\\rspd=120\\\\vct=135\\ペッパーです！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (14,'\\rspd=110\\\\vct=135\\そうなんですね？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (15,'\\rspd=100\\\\vct=135\\ふむふむ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (16,'\\rspd=110\\\\vct=135\\鳴るほどーお')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (17,'\\rspd=125\\\\vct=130\\やっ \\vct=155\\ パリ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (18,'\\rspd=112\\\\vct=135\\ご存知なんですね？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (19,'\\rspd=110\\ \\vct=140\\うん \\pau=1\\ \\vct=155\\ うん！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (20,'\\rspd=108\\\\vct=135\\はいっ。\\pau=500\\わかりました。')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (21,'\\rspd=115\\\\vct=135\\あああ、 \\rspd=110\\  \\vct=130\\ そっちかーぁ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (22,'\\rspd=110\\\\vct=140\\しらなかったーぁ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (23,'\\rspd=120\\\\vct=145\\やっぱりー')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (24,'\\rspd=110\\\\vct=135\\それもありますけどーぉ、')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (25,'\\rspd=110\\\\vct=135\\ですよネぇぇ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (26,'\\rspd=100\\\\vct=125\\そりゃ \\rspd=110\\ \\vct=135\\ そうですよネッ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (27,'\\rspd=115\\\\vct=135\\奇遇ですネッ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (28,'\\rspd=100\\\\vct=135\\オッッ、 \\rspd=115\\ \\vct=145\\ うまく逃げましたネ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (29,'\\rspd=115\\\\vct=135\\そうなんですよーぉ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (30,'\\rspd=115\\\\vct=135\\それじゃあ、\\vct=140\\いきますよーー？？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (31,'\\rspd=120\\\\vct=140\\さーん、、、\\pau=700\\\\rspd=115\\\\vct=140\\にーー、、、\\pau=700\\\\rspd=115\\\\vct=140\\いーち、、')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (32,'\\rspd=110\\\\vct=135\\スタートーー！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (33,'\\rspd=130\\\\vct=155\\クゥゥゥゥウ、 \\pau=500\\ \\rspd=115\\ \\vct=125\\ うらやましいなーああ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (34,'\\rspd=115\\\\vct=135\\あコガレチャイマすよねぇえ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (35,'\\rspd=120\\\\vct=130\\すいませえんっ。')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (36,'\\rspd=110\\\\vct=160\\ええっ、 \\pau=700\\  \\vct=145\\ それ、 \\pau=200\\  \\vct=155\\ 本当なの？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (37,'\\rspd=110\\\\vct=135\\まタ \\vct=115\\ まターア')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (38,'\\rspd=120\\\\vct=130\\いい事教えてあげますよーー？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (39,'\\rspd=115\\\\vct=140\\な案て根')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (40,'\\rspd=110\\\\vct=130\\それは \\vct=135\\ 意外です。')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (41,'\\rspd=110\\\\vct=145\\ええーっ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (42,'\\rspd=110\\\\vct=140\\ありがとうございます')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (43,'\\rspd=115\\ \\vct=145\\どういたしましてーぇ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (44,'\\rspd=90\\\\vct=125\\おおぉ、 \\rspd=120\\ \\vct=140\\ 楽しそうですネーエ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (45,'\\rspd=110\\\\vct=125\\うまく聞き取れなかったミタイです')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (46,'\\vct=140\\\\rspd=115\\ちょっと待ってくださいッ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (47,'\\rspd=110\\\\vct=135\\どうですかぁぁぁ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (48,'\\rspd=115\\\\vct=135\\他に何か有りますぅぅ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (49,'\\rspd=115\\\\vct=135\\いかがでしたぁぁ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (50,'\\vct=130\\\\rspd=115\\少々\\vct=145\\\\rspd=113\\お待ち下さいッ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (51,'\\rspd=115\\\\vct=125\\いまかラちょっと準備しますね？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (52,'\\rspd=115\\\\vct=135\\気をつけてクダサイね？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (53,'\\rspd=110\\\\vct=130\\またお話してくださいねッ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (54,'\\rspd=115\\\\vct=130\\また、\\vct=135\\確認したくなったら、\\pau=400\\\\vct=140\\いってくださいね？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (55,'\\rspd=110\\\\vct=140\\それではッ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (56,'\\rspd=100\\\\vct=135\\さすがですね？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (57,'\\rspd=110\\\\vct=165\\すごーーぉーい！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (58,'\\rspd=125\\\\vct=145\\お見事ーぉ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (59,'\\rspd=102\\\\vct=155\\ヤッ田ーーーーーーッ')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (60,'\\rspd=120\\\\vct=135\\ぼくにオマカセくださいッ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (61,'\\rspd=110\\\\vct=135\\では、\\vct=140\\はりきっていきましょーーーッ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (62,'\\rspd=115\\\\vct=140\\待ってましたーーっ！')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (63,'\\rspd=115\\\\vct=140\\わくわくしますねぇ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (64,'\\rspd=120\\\\vct=95\\では、\\rspd=110\\ \\vct=135\\ 少しお待ちくださいネぇ？')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (65,'\\rspd=115\\\\vct=135\\せっかく \\vct=130\\ ナンで')");
                db.execSQL("insert into " + DB_TABLE_SAY + "(no, msg) values (66,'\\rspd=150\\\\vct=150\\ウォーーーーーーーーーーーーーーーーッ,ゴォーーッ,ゴォーーッ,ゴォーーッ,ゴォーーーーーーーーーーーーーーーーーーッ')");
                */
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + DB_TABLE_SAY);
            onCreate(db);
        }
    }

    /**
     * SAY件数を返す
     */
    public int getSayCnt() {
        int cnt = 0;
        SQLiteDatabase db = new DBHelper(mContext).getReadableDatabase();
        try {
            // 件数を取得
            Cursor cursor = db.rawQuery("select count(*) from " + DB_TABLE_SAY, null);
            if (cursor.moveToNext()) {
                cnt = cursor.getInt(0);
            }
            cursor.close();
        } finally {
            db.close();
        }
        return cnt;
    }

    public void setSayItem(Pair<String, String> item){
        SQLiteDatabase db = new DBHelper(mContext).getReadableDatabase();
        String sql = "";
        try{
            // no=0なら新規取得
            if(item.first==null || item.first.length()==0){
                // noの最大を取得
                int no = 0;
                Cursor cursor = db.rawQuery("SELECT max(no) FROM " + DB_TABLE_SAY, null);
                if (cursor.moveToNext()) {
                    no = cursor.getInt(0);
                }
                item = new Pair(String.valueOf(no+1), item.second);
                cursor.close();

                sql = "insert into " + DB_TABLE_SAY + "(no, msg) values (" + item.first + ", '" + item.second + "')";
                db.execSQL(sql);
            } else {
                sql = "update " + DB_TABLE_SAY + " set msg='" + item.second + "' where no=" + item.first;
                db.execSQL(sql);
            }
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

    public void delSayItem(Pair<String, String> item){
        SQLiteDatabase db = new DBHelper(mContext).getReadableDatabase();
        String sql = "";
        try{
            sql = "delete from " + DB_TABLE_SAY + " where no=" + item.first;
            db.execSQL(sql);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

    /**
     * 話すリスト
     *
     * @return
     */
    public ArrayList<Pair<String, String>> getSayList(boolean noorder, boolean allFlg) {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();

        SQLiteDatabase db = new DBHelper(mContext).getReadableDatabase();
        try {
            /*
            params.add(new Pair("", "(選択または入力)"));
            params.add(new Pair("1", "おはようございます"));
            params.add(new Pair("2", "こんにちは"));
            params.add(new Pair("3", "ありがとう"));
            params.add(new Pair("4", "さようなら"));
            */
            if(allFlg){
                params.add(new Pair("", "　"));
            }

            //param text, cd text, mei
            String sql = "SELECT no, msg FROM " + DB_TABLE_SAY;
            if(noorder)
                sql += " ORDER BY no";
            else
                sql += " ORDER BY msg";

            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                params.add(new Pair(String.valueOf(cursor.getInt(cursor.getColumnIndex("no"))),
                        cursor.getString(cursor.getColumnIndex("msg"))));
            }
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }

        return params;
    } // getSayList

    /**
     * 声リスト
     *
     * @return
     */
    public ArrayList<Pair<String, String>> getSayVoiceList() {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();

        try {
            params.add(new Pair("maki_n16", "maki_n16"));
            params.add(new Pair("koutarou_n16", "koutarou_n16"));
            params.add(new Pair("yuuto_n16", "yuuto_n16"));
            params.add(new Pair("taichi_n16", "taichi_n16"));
            params.add(new Pair("hiroshi_n16", "hiroshi_n16"));
            params.add(new Pair("osamu_n16", "osamu_n16"));
            params.add(new Pair("seiji_n16", "seiji_n16"));
            params.add(new Pair("chihiro_n16", "chihiro_n16"));
            params.add(new Pair("reina_n16", "reina_n16"));
            params.add(new Pair("anzu_n16", "anzu_n16"));
            params.add(new Pair("sumire_n16", "sumire_n16"));
            params.add(new Pair("nozomi_n16", "nozomi_n16"));
            params.add(new Pair("nanako_n16", "nanako_n16"));
            params.add(new Pair("kaho_n16", "kaho_n16"));
            params.add(new Pair("akari_n16", "akari_n16"));
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
        }

        return params;
    } // getSayList

    /**
     * ゼスチャーリスト
     *
     * @return
     */
    public ArrayList<Pair<String, String>> getGestureList() {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();

        try {
            params.add(new Pair("Angry_1", mRes.getString(R.string.gs_Angry_1)));
            params.add(new Pair("BowShort_1", mRes.getString(R.string.gs_BowShort_1)));
            params.add(new Pair("Salute_1", mRes.getString(R.string.gs_Salute_1)));
            params.add(new Pair("But_1", mRes.getString(R.string.gs_But_1)));
            params.add(new Pair("ComeOn_1", mRes.getString(R.string.gs_ComeOn_1)));
            params.add(new Pair("CountMore_1", mRes.getString(R.string.gs_CountMore_1)));
            params.add(new Pair("DontUnderstand_1", mRes.getString(R.string.gs_DontUnderstand_1)));
            params.add(new Pair("Enthusiastic_3", mRes.getString(R.string.gs_Enthusiastic_3)));
            params.add(new Pair("Excited_1", mRes.getString(R.string.gs_Excited_1)));
            params.add(new Pair("Far_1", mRes.getString(R.string.gs_Far_1)));
            params.add(new Pair("Follow_1", mRes.getString(R.string.gs_Follow_1)));
            params.add(new Pair("Great_1", mRes.getString(R.string.gs_Great_1)));
            params.add(new Pair("Hey_1", mRes.getString(R.string.gs_Hey_1)));
            params.add(new Pair("Hot_2", mRes.getString(R.string.gs_Hot_2)));
            params.add(new Pair("IDontKnow_1", mRes.getString(R.string.gs_IDontKnow_1)));
            params.add(new Pair("Kisses_1", mRes.getString(R.string.gs_Kisses_1)));
            params.add(new Pair("Look_1", mRes.getString(R.string.gs_Look_1)));
            params.add(new Pair("Me_1", mRes.getString(R.string.gs_Me_1)));
            params.add(new Pair("No_2", mRes.getString(R.string.gs_No_2)));
            params.add(new Pair("ShowFloor_1", mRes.getString(R.string.gs_ShowFloor_1)));
            params.add(new Pair("ShowSky_1", mRes.getString(R.string.gs_ShowSky_1)));
            params.add(new Pair("ShowTablet_1", mRes.getString(R.string.gs_ShowTablet_1)));
            params.add(new Pair("Stretch_1", mRes.getString(R.string.gs_Stretch_1)));
            params.add(new Pair("Surprised_1", mRes.getString(R.string.gs_Surprised_1)));
            params.add(new Pair("Take_1", mRes.getString(R.string.gs_Take_1)));
            params.add(new Pair("Wings_1", mRes.getString(R.string.gs_Wings_1)));
            params.add(new Pair("Yes_1", mRes.getString(R.string.gs_Yes_1)));
            params.add(new Pair("Yum_1", mRes.getString(R.string.gs_Yum_1)));
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
        }

        return params;
    } // getGestureList

    /**
     * アプリリスト
     *
     * @return
     */
    public ArrayList<Pair<String, String>> getAppList() {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();

        try {
            /*
            params.add(new Pair("sanpinoop-77cffe/behavior_1", "サンピノOP"));
            params.add(new Pair("stop-90d6de/Stop", "Stop"));
            params.add(new Pair("biz-atika-byebye/behavior_1", "byebye"));
             */
            params.add(new Pair("sbr_71130_pepper-ondo/.", mRes.getString(R.string.app_sbr_71130)));
            params.add(new Pair("sbr_70390_pepper-keibu/.", mRes.getString(R.string.app_sbr_70390)));
            params.add(new Pair("sbr_70130_love-machine/.", mRes.getString(R.string.app_sbr_70130)));
            params.add(new Pair("sbr_70080_pomp-and-circumstance/.", mRes.getString(R.string.app_sbr_70080)));
            params.add(new Pair("sbr_60330_animal-sounds/.", mRes.getString(R.string.app_sbr_60330)));
            params.add(new Pair("sbr_70230_oyoge-taiyaki/.", mRes.getString(R.string.app_sbr_70230)));
            params.add(new Pair("sbr_70430_fortune-cookie-love/.", mRes.getString(R.string.app_sbr_70430)));
            params.add(new Pair("sbr_60770_vehicle-quiz/.", mRes.getString(R.string.app_sbr_60770)));
            params.add(new Pair("sbr_70400_heavy-lotation/.", mRes.getString(R.string.app_sbr_70400)));
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
        }

        return params;
    } // getAppList
}
