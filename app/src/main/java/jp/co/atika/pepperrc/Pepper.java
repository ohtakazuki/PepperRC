package jp.co.atika.pepperrc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.aldebaran.qi.DynamicObjectBuilder;
import com.aldebaran.qi.QiService;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.proxies.ALAnimatedSpeech;
import com.aldebaran.qi.helper.proxies.ALAudioDevice;
import com.aldebaran.qi.helper.proxies.ALAutonomousMoves;
import com.aldebaran.qi.helper.proxies.ALBasicAwareness;
import com.aldebaran.qi.helper.proxies.ALBehaviorManager;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALVideoDevice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Pepper {
    private static final String TAG = "Pepper";

    // 共通関数
    private Cmn mCmn;
    private Context mContext;

    // Pepper API
    private Session mQiSession;

    // 動画撮影ID(unscrubleでも使用するためクラス変数)
    private String mImgClient;

    // 動画撮影中フラグ
    private boolean mImgFlg;

    // 写真ボタン押されたフラグ
    private boolean mImgPicFlg;
    private ImageUtility mImageUtility;

    // 音声キャプチャー用
    private boolean mRecFlg;            // 音声中フラグ
    private AudioTrack mAudioTrack;     // マイク取得用
    // 音声バッファを入れていく
    private List<ByteBuffer> mByteBuffers;
    private List<byte[]> mBytes;

    // アプリ中フラグ
    private boolean mAppFlg;


    /**
     * コンストラクタ
     */
    public Pepper(Context c) {
        mContext = c;
        // 共通関数
        mCmn = new Cmn(mContext);

        mImgClient = "";
        mImgFlg = false;
        mImgPicFlg = false;

        mImageUtility = new ImageUtility();
    }

    /**
     * --------------------------------------------------------------------------------
     * 接続
     * --------------------------------------------------------------------------------
     */
    public void Connect() {
        // 接続する
        AsyncConnect task_ = new AsyncConnect();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        task_ = null;
    }

    private class AsyncConnect extends AsyncTask<Void, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(Void... param) {
            // メッセージ格納用
            return _Connect();
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.length() == 0) {
                result = mCmn.getString(R.string.str_connect);
            }
            Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 接続する。各Asyncから呼ばれる
     */
    private String _Connect() {
        if (mQiSession != null && mQiSession.isConnected()) {
            return "";
        }
        String mMsg = "";
        try {
            // prefの取得
            SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String mServerIp = mPreferences.getString(mCmn.getString(R.string.pref_ip_key), "");
            String mPort = mPreferences.getString(mCmn.getString(R.string.pref_port_key), "");

            String ip = "tcp://" + mServerIp + ":" + mPort;
            mQiSession = new Session();
            mQiSession.connect(ip).get();
            // mALTextToSpeech = new ALTextToSpeech( mQiSession );
            // mALAudioDevice = new ALAudioDevice( mQiSession );
            Log.d(TAG,  mCmn.getString(R.string.str_connect));
        } catch (Exception e) {
            mQiSession = null;
            // mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
            mMsg = mCmn.getString(R.string.str_err_ip);
            // Log.d(TAG, mMsg);
            // Log.d(TAG, e.getClass().getName());
        }
        return mMsg;
    }

    /**
     * --------------------------------------------------------------------------------
     * 声
     * --------------------------------------------------------------------------------
     */
    public void SayVoice(String voice) {
        // 接続する
        AsyncVoice task_ = new AsyncVoice();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, voice);
    }

    private class AsyncVoice extends AsyncTask<String, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(String... param) {
            // メッセージ格納用
            String mMsg = "";

            // 音量
            String voice = param[0];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }
                ALTextToSpeech mALTextToSpeech = new ALTextToSpeech(mQiSession);
                // mALTextToSpeech.setLanguage("Japanese");
                mALTextToSpeech.setVoice(voice);
                mALTextToSpeech.say(mCmn.getString(R.string.str_voice_change));
            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }

            Log.d(TAG, "ALTextToSpeech.setVoice(" + voice + ")");
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * オートノマスライフ
     * --------------------------------------------------------------------------------
     */
    public void SetAutonomous(boolean flg) {
        AsyncAutonomous task_ = new AsyncAutonomous();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, flg);
    }

    private class AsyncAutonomous extends AsyncTask<Boolean, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(Boolean... param) {
            // メッセージ格納用
            String mMsg = "";

            // OnOff
            boolean flg = param[0];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }
                ALBasicAwareness mALBasicAwareness = new ALBasicAwareness(mQiSession);
                ALAutonomousMoves mALAutonomousMoves = new ALAutonomousMoves(mQiSession);
                if(!flg){
                    mALBasicAwareness.stopAwareness();
                    mALAutonomousMoves.setBackgroundStrategy("none");
                    mALAutonomousMoves.setExpressiveListeningEnabled(false);
                } else {
                    mALBasicAwareness.startAwareness();
                    mALAutonomousMoves.setBackgroundStrategy("backToNeutral");
                    mALAutonomousMoves.setExpressiveListeningEnabled(true);
                }
            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }

            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * 音量
     * --------------------------------------------------------------------------------
     */
    public void SayVolume(int vol) {
        // 接続する
        AsyncVolume task_ = new AsyncVolume();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, vol);
    }

    private class AsyncVolume extends AsyncTask<Integer, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(Integer... param) {
            // メッセージ格納用
            String mMsg = "";

            // 音量
            int vol = param[0];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }
                ALAudioDevice mALAudioDevice = new ALAudioDevice(mQiSession);
                mALAudioDevice.setOutputVolume(vol);
            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }

            Log.d(TAG, "ALTextToSpeech.setOutputVolume(" + vol + ")");
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * 話す
     * --------------------------------------------------------------------------------
     */
    public void Say(String sayMsg) {
        if (sayMsg == null || sayMsg.length() == 0) {
            Toast.makeText(mContext, mCmn.getString(R.string.str_say_nostring), Toast.LENGTH_LONG).show();
        } else {
            // 接続する
            AsyncSay task_ = new AsyncSay();
            task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sayMsg);
        }
    }

    private class AsyncSay extends AsyncTask<String, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(String... param) {
            // メッセージ格納用
            String mMsg = "";

            // 話すメッセージ
            String sayMsg = param[0];
            if (sayMsg == null || sayMsg.length() == 0) {
                return mMsg;
            }

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }

                // 話す
                ALAnimatedSpeech mALAnimatedSpeech = new ALAnimatedSpeech(mQiSession);
                // 0:動作なし, 1:ランダム, 2:文脈に合わせて
                mALAnimatedSpeech.setBodyLanguageMode(2);
                // sayが全部言い終わるまで次の命令に行かない
                mALAnimatedSpeech.say(sayMsg);
                /*
                ALTextToSpeech mALTextToSpeech = new ALTextToSpeech( mQiSession );
                mALTextToSpeech.setLanguage( "Japanese" );
                mALTextToSpeech.say(sayMsg);
                */
            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }
            Log.d(TAG, "ALAnimatedSpeech.AsyncSay(" + sayMsg + ")");
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * 撮影開始
     * --------------------------------------------------------------------------------
     */
    public void ImgStart(ImageView imageView) {
        // 接続する
        if (mImgFlg) {
            Toast.makeText(mContext, mCmn.getString(R.string.str_img_working), Toast.LENGTH_LONG).show();
        } else {
            mImgFlg = true;
            AsyncImgStart task_ = new AsyncImgStart(imageView);
            task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void ImgStop() {
        if (!mImgFlg) {
            // Toast.makeText(mContext, mCmn.getString(R.string.str_img_notworking), Toast.LENGTH_LONG).show();
        }
        mImgFlg = false;
    }

    public void ImgPic() {
        if (!mImgFlg) {
            Toast.makeText(mContext, mCmn.getString(R.string.str_img_notworking), Toast.LENGTH_LONG).show();
            mImgPicFlg = false;
        } else {
            mImgPicFlg = true;
        }
    }

    private class AsyncImgStart extends AsyncTask<Void, Integer, String> {
        // resolution
        private static final int RESOLUTION_QQVGA = 0;  // 160 x 120
        private static final int RESOLUTION_QVGA = 1;   // 320 x 240
        private static final int RESOLUTION_VGA = 2;    // 640 x 480

        // color space
        // Buffer only contains the Y (luma component) equivalent to one unsigned char
        private static final int COLOR_SPACE_Y = 0;
        // YUV422 : Native format, 0xY’Y’VVYYUU equivalent to four unsigned char for two pixels. With Y luma for pixel n, Y’ luma for pixel n+1, and U and V are the average chrominance value of both pixels.
        private static final int COLOR_SPACE_YUV422 = 9;
        // YUV : Buffer contains triplet on the format 0xVVUUYY, equivalent to three unsigned char
        private static final int COLOR_SPACE_YUV = 10;
        // Buffer contains triplet on the format 0xBBGGRR, equivalent to three unsigned char
        private static final int COLOR_SPACE_RGB = 11;
        // Buffer contains triplet on the format 0xYYSSHH, equivalent to three unsigned cha
        private static final int COLOR_SPACE_HSY = 12;
        // Buffer contains triplet on the format 0xRRGGBB, equivalent to three unsigned char
        private static final int COLOR_SPACE_BGR = 13;
        private static final int COLOR_SPACE = COLOR_SPACE_YUV422;

        // frames per second
        private static final int FPS_5 = 5;
        private static final int FPS_10 = 10;
        private static final int FPS_15 = 15;
        private static final int FPS_30 = 30;

        // ImageFormat.NV21 or ImageFormat.YUY2
        // NV21 : YCrCb format used for images, which uses the NV21 encoding format.
        // YUY2 : YCbCr format used for images, which uses YUYV (YUY2) encoding format.
        private int YUV_FORMAT = ImageFormat.YUY2;

        // image remote
        private static final String GVM_NAME = "android_client";

        private ImageView mImageView;
        private Bitmap mBitmap;

        // コンストラクタ
        public AsyncImgStart(ImageView imageView) {
            mImageView = imageView;
        }

        /**
         * doInBackgroundの事前準備処理（UIスレッド）
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, mCmn.getString(R.string.str_img_start2), Toast.LENGTH_LONG).show();
        }

        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(Void... param) {
            String mMsg = "";
            int regIdx = 0;

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }

                // ----------------------------------------
                // 映像
                // ----------------------------------------
                ALVideoDevice mALVideoDevice = new ALVideoDevice(mQiSession);
                mALVideoDevice.unsubscribeAllInstances(GVM_NAME);

                // 撮影開始
                // free版
                mImgClient = mALVideoDevice.subscribe(GVM_NAME, RESOLUTION_VGA, COLOR_SPACE, FPS_5);
                //mImgClient = mALVideoDevice.subscribe(GVM_NAME, RESOLUTION_VGA, COLOR_SPACE, FPS_10);
                Log.d(TAG, "ALVideoDevice.subscribe");
                mBitmap = null;

                if ((mImgClient != null) && (mImgClient.length() > 0)) {
                    mImgFlg = true;

                    long t1 = System.currentTimeMillis();

                    while (mImgFlg) {
                        long t2 = System.currentTimeMillis();
                        // free版=500、標準=100
                        if (t2 - t1 > 500) {
                            // 動画取得
                            List<Object> list = null;
                            list = (List<Object>) mALVideoDevice.getImageRemote(mImgClient);
                            if ((list != null) && (list.size() > 0)) {
                                ImageRemoteResult result = new ImageRemoteResult(list);
                                if ((result != null) && (result.buf != null) && (result.buf.limit() > 0)) {
                                    ByteBuffer mByteBuffer = result.buf;
                                    mBitmap = mImageUtility.getBitmapFromYuv(mByteBuffer, YUV_FORMAT, result.width, result.height);
                                }
                            }

                            publishProgress();
                            t1 = System.currentTimeMillis();
                        }
                        //Thread.sleep(250);
                    }
                }
                // 撮影終了(InterrptExceptionが発生するのでtryの中
                mALVideoDevice.unsubscribe(mImgClient);

            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            } finally {
                mImgClient = "";
            }
            return mMsg;
        }

        // 進捗状況をUIに反映するための処理(UIスレッド)
        @Override
        protected void onProgressUpdate(Integer... progress) {
            try {
                if (mBitmap != null) {
                    mImageView.setImageBitmap(mBitmap);
                    if (mImgPicFlg) {
                        File extStrageDir = Environment.getExternalStorageDirectory();
                        File file = new File(extStrageDir.getAbsolutePath() + "/" + Environment.DIRECTORY_DCIM, getFileName() + ".png");
                        FileOutputStream outStream = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.close();
                        Toast.makeText(mContext, mCmn.getString(R.string.str_img_picture2), Toast.LENGTH_LONG).show();
                        mImgPicFlg = false;
                    }
                }
            } catch (Exception e) {
                Toast.makeText(mContext, mCmn.getString(R.string.str_err) + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (mImgPicFlg) {
                    mImgPicFlg = false;
                }
            }
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            mImageView.setImageBitmap(null);
            if (result == null || result.length() == 0) {
                result = mCmn.getString(R.string.str_img_stop2);
            }
            Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName() {
        Calendar c = Calendar.getInstance();
        String s = "pepper_" +
                c.get(Calendar.YEAR) +
                (c.get(Calendar.MONTH) + 1) +
                c.get(Calendar.DAY_OF_MONTH) + "_" +
                c.get(Calendar.HOUR_OF_DAY) +
                c.get(Calendar.MINUTE) +
                c.get(Calendar.SECOND) +
                c.get(Calendar.MILLISECOND);
        return s;
    }

    /**
     * --- class ImageRemoteResult ---
     * [0] : width; [1] : height; [2] : number of layers; [3] : ColorSpace; [4] : time stamp (highest 32 bits); [5] : time stamp (lowest 32 bits); [6] : array of size height * width * nblayers containing image data; [7] : cameraID; [8] : left angle; [9] : top angle; [10] : right angle; [11] : bottom angle;
     */
    private class ImageRemoteResult {
        public int width = 0;
        public int height = 0;
        public int layers = 0;
        public int color = 0;
        public int time1 = 0;
        public int time2 = 0;
        public long time = 0;
        public int id = 0;
        public float left = 0;
        public float top = 0;
        public float right = 0;
        public float bottom = 0;
        ByteBuffer buf = null;

        /*
         * Constructor
         */
        public ImageRemoteResult(List<Object> list) {
            width = (int) list.get(0);
            height = (int) list.get(1);
            layers = (int) list.get(2);
            color = (int) list.get(3);
            time1 = (int) list.get(4);
            time2 = (int) list.get(5);
            time = ((long) time1 << 32) + (long) time2;
            buf = (ByteBuffer) list.get(6);
            id = (int) list.get(7);
            left = (float) list.get(8);
            top = (float) list.get(9);
            right = (float) list.get(10);
            bottom = (float) list.get(11);
            debug();
        }

        /*
         * debug
         */
        private void debug() {
            String msg = "";
            msg += " width=" + width;
            msg += " height=" + height;
            msg += " layers=" + layers;
            msg += " color=" + color;
            msg += " time=" + time;
            msg += " id=" + id;
            msg += " left=" + left;
            msg += " top=" + top;
            msg += " right=" + right;
            msg += " bottom=" + bottom;
            msg += " buffer=" + buf;
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * 移動
     * --------------------------------------------------------------------------------
     */
    public void Move(float x, float y, float theta) {
        // 移動する
        AsyncMove task_ = new AsyncMove();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, x, y, theta);
    }

    private class AsyncMove extends AsyncTask<Float, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(Float... param) {
            // メッセージ格納用
            String mMsg = "";

            // 移動方向、角度
            float x = param[0];
            float y = param[1];
            float theta = param[2];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }

                ALMotion mALMotion = new ALMotion(mQiSession);
                if (x == 0 && y == 0 && theta == 0) {
                    mALMotion.stopMove();
                } else {
                    mALMotion.moveToward(x, y, theta);
                }

            } catch (Exception e) {
                mQiSession = null;
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
            }
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * App
     * --------------------------------------------------------------------------------
     */
    public void App(String app) {
        AsyncApp task_ = new AsyncApp();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, app);
        /*
        if(mAppFlg){
            Toast.makeText(mContext, mCmn.getString(R.string.str_app_working), Toast.LENGTH_SHORT).show();
        } else {
            AsyncApp task_ = new AsyncApp();
            task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, app);
            mAppFlg = true;
        }
        */
    }

    public void AppStop(String app) {
        AsyncStop task_ = new AsyncStop();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, app);
        if (!mAppFlg) {
            // Toast.makeText(mContext, mCmn.getString(R.string.str_img_notworking), Toast.LENGTH_SHORT).show();
        }
        mAppFlg = false;
    }

    private class AsyncApp extends AsyncTask<String, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(String... param) {
            // メッセージ格納用
            String mMsg = "";

            // ゼスチャー
            String app = param[0];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }

                ALBehaviorManager mALBehaviorManager = new ALBehaviorManager(mQiSession);
                // 発声込み
                mALBehaviorManager.startBehavior(app);
                /*
                mALBehaviorManager.runBehavior(app);
                while(mALBehaviorManager.isBehaviorRunning(app) && mAppFlg){
                    Thread.sleep(1000);
                }
                */
                // アクションだけ
                // mALBehaviorManager.runBehavior("animations/Stand/Gestures/" + gesture);
                //mALBehaviorManager.async();

            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }

            Log.d(TAG, "ALBehaviorManager.runBehavior(" + app + ")");
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }


    private class AsyncStop extends AsyncTask<String, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(String... param) {
            // メッセージ格納用
            String mMsg = "";

            // ゼスチャー
            String app = param[0];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }

                ALBehaviorManager mALBehaviorManager = new ALBehaviorManager(mQiSession);
                // 発声込み
                mALBehaviorManager.stopBehavior(app);

            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }

            Log.d(TAG, "ALBehaviorManager.stopBehavior(" + app + ")");
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     * --------------------------------------------------------------------------------
     * ゼスチャー
     * --------------------------------------------------------------------------------
     */
    public void Gesture(String gesture) {
        // 接続する
        AsyncGesture task_ = new AsyncGesture();
        task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gesture);
    }

    private class AsyncGesture extends AsyncTask<String, Integer, String> {
        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(String... param) {
            // メッセージ格納用
            String mMsg = "";

            // ゼスチャー
            String gesture = param[0];

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }
                // ポーズ（成功）
                // ALRobotPosture mALRobotPosture = new ALRobotPosture(mQiSession);
                // mALRobotPosture.goToPosture("StandZero", 0.1f);

                ALBehaviorManager mALBehaviorManager = new ALBehaviorManager(mQiSession);
                // 発声込み
                mALBehaviorManager.startBehavior("animations/Stand/Gestures/" + gesture);
                // アクションだけ
                // mALBehaviorManager.runBehavior("animations/Stand/Gestures/" + gesture);
                //mALBehaviorManager.async();

            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            }

            Log.d(TAG, "ALBehaviorManager.runBehavior(" + gesture + ")");
            return mMsg;
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.length() > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------------
     * 録音開始
     * --------------------------------------------------------------------------------
     */
    public void RecStart() {
        // 接続する
        if (mRecFlg) {
            Toast.makeText(mContext, mCmn.getString(R.string.str_rec_working), Toast.LENGTH_SHORT).show();
        } else {
            mRecFlg = true;
            AsyncRecStart task_ = new AsyncRecStart();
            task_.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void RecStop() {
        if (!mRecFlg) {
            // Toast.makeText(mContext, mCmn.getString(R.string.str_img_notworking), Toast.LENGTH_SHORT).show();
        }
        mRecFlg = false;
    }

    private class AsyncRecStart extends AsyncTask<Void, Integer, String> {
        // マイク
        private static final String MIC_NAME = "android_clientm";
        private static final int SAMPLE_RATE = 16000;

        /**
         * doInBackgroundの事前準備処理（UIスレッド）
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, mCmn.getString(R.string.str_rec_start2), Toast.LENGTH_SHORT).show();
        }

        // 別スレッド処理（主処理）
        @Override
        protected String doInBackground(Void... param) {
            String mMsg = "";
            int regIdx = 0;

            try {
                // 未接続なら接続
                if (mQiSession == null) {
                    mMsg = _Connect();
                }

                // ----------------------------------------
                // 音声
                // ----------------------------------------
                // プロセスの優先度を上げる
                android.os.Process.setThreadPriority(
                        android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                int bufferLength = 2730;
                /*
                int bufferLength = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                        */

                // マイクでの再生用
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferLength,
                        AudioTrack.MODE_STREAM);

                // オーディオを再生するスレッド
                mByteBuffers = new ArrayList<ByteBuffer>();
                mBytes = new ArrayList<>();
                mRecFlg = true;

                // オーディオサービスへ登録する
                MyQiService mMyQiService = new MyQiService();
                DynamicObjectBuilder objectBuilder = new DynamicObjectBuilder();
                objectBuilder.advertiseMethod("processRemote::v(mmmm)", mMyQiService, "Callback for ALAudioDevice");
                regIdx = mQiSession.registerService(MIC_NAME, objectBuilder.object());

                // オーディオデバイスの開始
                ALAudioDevice mALAudioDevice = new ALAudioDevice(mQiSession);
                mALAudioDevice.unsubscribe(MIC_NAME);
                mALAudioDevice.setClientPreferences(MIC_NAME, SAMPLE_RATE, 3, 0);
                mALAudioDevice.subscribe(MIC_NAME);

                // 再生の開始
                mAudioTrack.play();

                // 録音終了までぐるぐる
                while (mRecFlg) {
                    Thread.sleep(1000);
                }
                mALAudioDevice.unsubscribe(MIC_NAME);

            } catch (Exception e) {
                mMsg = mCmn.getString(R.string.str_err) + e.getMessage();
                Log.d(TAG, mMsg);
            } finally {
                // 音声サービスの解除
                mQiSession.unregisterService(regIdx);
                // 音声の停止と解除
                if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    mAudioTrack.stop();
                }
                mAudioTrack.flush();
                mAudioTrack.release();
                mRecFlg = false;
            }
            return mMsg;
        }

        // 進捗状況をUIに反映するための処理(UIスレッド)
        @Override
        protected void onProgressUpdate(Integer... progress) {
            try {
            } catch (Exception e) {
                Toast.makeText(mContext, mCmn.getString(R.string.str_err) + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
            }
        }

        // 事後処理(UIスレッド)
        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.length() == 0) {
                result = mCmn.getString(R.string.str_rec_stop2);
            }
            Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyQiService extends QiService {

        /**
         * オーディオのコールバック関数
         */
        public void processRemote(Object nbOfChannels, Object nbrOfSamplesByChannel, Object aTimeStamp, Object buffer) {
            try {
                // こっちはいつ呼ばれるか分かんないので、
                // 再生とバッファに積み重ねだけにする
                ByteBuffer bf = (ByteBuffer) buffer;
                mAudioTrack.write(bf.array(), 0, bf.capacity());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

}
