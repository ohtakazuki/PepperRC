package jp.co.atika.pepperrc;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Fragmentのライフサイクル
 * ・Fragmentが生成された
 * ↓onAttach()
 * ↓onCreate()
 * ↓onCreateView()
 * ↓onActivityCreated()
 * ↓onStart()
 * ↓onResume()
 */
public class FragmentControl extends Fragment {
    private static final String TAG = "FragmentControl";

    private Cmn mCmn;
    private Pepper mPepper;
    private View v;

    /***
     * Fragmentの初期化処理を行う
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 共通関数
        mCmn = new Cmn(this.getActivity());
        mPepper = new Pepper(this.getActivity());
        mPepper.Connect();
    }

    /***
     * FragmentのView階層を生成し戻り値として返す
     * Fragment内で表示させたいViewを作成してる
     * inflater レイアウトを描画するために必要なインスタンス
     * container このFragmentを配置する際の親となるViewGroupオブジェクト
     * savedInstanceState onCreate()メソッドに渡されているのと同じBundleインスタンス
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // レイアウトを描画して返す
        v = inflater.inflate(R.layout.fragment_control, null);
        setMe();
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPepper.ImgStop();
        mPepper.RecStop();
        mPepper.SetAutonomous(true);

        Switch mSwitch = (Switch) v.findViewById(R.id.autoSwitch);
        mSwitch.setChecked(true);
    }


    /**
     * --------------------------------------------------------------------------------
     * 画面描画
     * --------------------------------------------------------------------------------
     */
    public void setMe() {
        // free版
        AdView mAdView = (AdView)v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // ----------------------------------------
        // オートノマスライフ
        // ----------------------------------------
        Switch mSwitch = (Switch) v.findViewById(R.id.autoSwitch);
        mSwitch.setOnCheckedChangeListener(autoSwitchCheckedChangeListener);

        // ----------------------------------------
        // 撮影
        // ----------------------------------------
        // ボタンにリスナを割当
        Button imgStartButton = (Button) v.findViewById(R.id.imgStartButton);
        imgStartButton.setOnClickListener(imgStartButtonClickListener);
        Button imgStopButton = (Button) v.findViewById(R.id.imgStopButton);
        imgStopButton.setOnClickListener(imgStopButtonClickListener);
        Button imgPictureButton = (Button) v.findViewById(R.id.imgPictureButton);
        imgPictureButton.setOnClickListener(imgPictureButtonClickListener);

        // ----------------------------------------
        // 話す
        // ----------------------------------------
        // ボリューム
        SeekBar saySeekBar = (SeekBar) v.findViewById(R.id.saySeekBar);
        saySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seek) {
            }

            @Override
            public void onProgressChanged(SeekBar seek, int progress, boolean touch) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seek) {
                mPepper.SayVolume(seek.getProgress());
            }
        });

        // リストに割当
        KeyValuePairAdapter saySpinnerAdapter = new KeyValuePairAdapter(this.getActivity(),
                android.R.layout.simple_spinner_item, mCmn.getSayList(false, true));
        saySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner saySpinner = (Spinner) v.findViewById(R.id.saySpinner);
        saySpinner.setAdapter(saySpinnerAdapter);


        // 話すボタンにリスナを割当
        Button sayButton = (Button) v.findViewById(R.id.sayButton);
        sayButton.setOnClickListener(sayButtonClickListener);

        // 話すボタンにリスナを割当
        Button sayEditButton = (Button) v.findViewById(R.id.sayEditButton);
        sayEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FragmentTransaction transaction = ((Activity)mCmn.getContext()).getFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, new FragmentSayEdit());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // ----------------------------------------
        // 動く
        // ----------------------------------------
        // ボタンにリスナを割当
        Button move1Button = (Button) v.findViewById(R.id.move1Button);
        move1Button.setOnTouchListener(moveButtonTouchListener);
        Button move2Button = (Button) v.findViewById(R.id.move2Button);
        move2Button.setOnTouchListener(moveButtonTouchListener);
        Button move3Button = (Button) v.findViewById(R.id.move3Button);
        move3Button.setOnTouchListener(moveButtonTouchListener);
        Button move4Button = (Button) v.findViewById(R.id.move4Button);
        move4Button.setOnTouchListener(moveButtonTouchListener);
        Button move6Button = (Button) v.findViewById(R.id.move6Button);
        move6Button.setOnTouchListener(moveButtonTouchListener);
        Button move7Button = (Button) v.findViewById(R.id.move7Button);
        move7Button.setOnTouchListener(moveButtonTouchListener);
        Button move8Button = (Button) v.findViewById(R.id.move8Button);
        move8Button.setOnTouchListener(moveButtonTouchListener);
        Button move9Button = (Button) v.findViewById(R.id.move9Button);
        move9Button.setOnTouchListener(moveButtonTouchListener);
        Button moveRoteLeftButton = (Button) v.findViewById(R.id.moveRoteLeftButton);
        moveRoteLeftButton.setOnTouchListener(moveButtonTouchListener);
        Button moveRoteRightButton = (Button) v.findViewById(R.id.moveRoteRightButton);
        moveRoteRightButton.setOnTouchListener(moveButtonTouchListener);

        // ----------------------------------------
        // App
        // ----------------------------------------
        KeyValuePairAdapter appSpinnerAdapter = new KeyValuePairAdapter(this.getActivity(),
                android.R.layout.simple_spinner_item, mCmn.getAppList());
        appSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner appSpinner = (Spinner) v.findViewById(R.id.appSpinner);
        appSpinner.setAdapter(appSpinnerAdapter);

        // ボタンにリスナを割当
        Button appButton = (Button) v.findViewById(R.id.appButton);
        appButton.setOnClickListener(appButtonClickListener);
        Button appStopButton = (Button) v.findViewById(R.id.appStopButton);
        appStopButton.setOnClickListener(appStopButtonClickListener);

        // ----------------------------------------
        // Gesture
        // ----------------------------------------
        KeyValuePairAdapter gestureSpinnerAdapter = new KeyValuePairAdapter(this.getActivity(),
                android.R.layout.simple_spinner_item, mCmn.getGestureList());
        gestureSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner gestureSpinner = (Spinner) v.findViewById(R.id.gestureSpinner);
        gestureSpinner.setAdapter(gestureSpinnerAdapter);

        // ボタンにリスナを割当
        Button gestureButton = (Button) v.findViewById(R.id.gestureButton);
        gestureButton.setOnClickListener(gestureButtonClickListener);

        // ----------------------------------------
        // 録音
        // ----------------------------------------
        // ボタンにリスナを割当
        Button recStartButton = (Button) v.findViewById(R.id.recStartButton);
        recStartButton.setOnClickListener(recStartButtonClickListener);
        Button recStopButton = (Button) v.findViewById(R.id.recStopButton);
        recStopButton.setOnClickListener(recStopButtonClickListener);
    }

    /**
     * --------------------------------------------------------------------------------
     * オートノマスライフ
     * --------------------------------------------------------------------------------
     */
    Switch.OnCheckedChangeListener autoSwitchCheckedChangeListener = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //
            mPepper.SetAutonomous(isChecked);
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 撮影開始
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener imgStartButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentView = (View) v.getParent().getParent();
            ImageView imgImageView = (ImageView) parentView.findViewById(R.id.imgImageView);
            mPepper.ImgStart(imgImageView);
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 撮影終了
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener imgStopButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPepper.ImgStop();
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 画像保存
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener imgPictureButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPepper.ImgPic();
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 話すボタン
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener sayButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sayMsg = "";

            // テキストに入力されていたらそれを取得
            View parentView = (View) v.getParent().getParent();
            EditText sayEditText = (EditText) parentView.findViewById(R.id.sayEditText);
            if (sayEditText != null && sayEditText.getText().toString().length() > 0) {
                sayMsg = sayEditText.getText().toString();
            } else {
                // 入力されてなければスピナから
                Spinner saySpinner = (Spinner) parentView.findViewById(R.id.saySpinner);
                Pair<String, String> pair = (Pair<String, String>) saySpinner.getSelectedItem();
                if (pair.first != null && pair.first.length() > 0) {
                    sayMsg = pair.second;
                }
            }

            // 話す
            if (sayMsg.length() > 0) {
                mPepper.Say(sayMsg);
            } else {
                Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.str_say_nostring), Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 移動
     * --------------------------------------------------------------------------------
     */
    View.OnTouchListener moveButtonTouchListener = new View.OnTouchListener() {
        // ボタンがタッチされた時のハンドラ
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 指がタッチした時の処理を記述
                View parentView = (View) v.getParent().getParent().getParent();
                SeekBar moveSeekBar = (SeekBar) parentView.findViewById(R.id.moveSeekBar);
                int p = moveSeekBar.getProgress();
                int mx = moveSeekBar.getMax();
                if (p > 0 && p <= mx) {
                    float ss =  ((float)moveSeekBar.getProgress()) / ((float)moveSeekBar.getMax());
                    float x = 0f, y = 0f, theta = 0f;
                    switch (v.getId()) {
                        case R.id.move1Button:
                            x = ss;
                            y = ss;
                            break;

                        case R.id.move2Button:
                            x = ss;
                            break;

                        case R.id.move3Button:
                            x = ss;
                            y = -ss;
                            break;

                        case R.id.move4Button:
                            y = ss;
                            break;

                        case R.id.move6Button:
                            y = -ss;
                            break;

                        case R.id.move7Button:
                            x = -ss;
                            y = ss;
                            break;

                        case R.id.move8Button:
                            x = -ss;
                            break;

                        case R.id.move9Button:
                            x = -ss;
                            y = -ss;
                            break;

                        case R.id.moveRoteLeftButton:
                            theta = -ss;
                            break;

                        case R.id.moveRoteRightButton:
                            theta = ss;
                            break;
                    }
                    mPepper.Move(x, y, theta);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // タッチした指が離れた時の処理を記述
                mPepper.Move(0, 0, 0);
            }
            return false;
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * アプリ実行
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener appButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentView = (View) v.getParent().getParent();
            Spinner appSpinner = (Spinner) parentView.findViewById(R.id.appSpinner);
            Pair<String, String> pair = (Pair<String, String>) appSpinner.getSelectedItem();
            if (pair.first != null && pair.first.length() > 0) {
                mPepper.App(pair.first);
            } else {
                Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.str_app_nothing), Toast.LENGTH_LONG).show();
            }
        }
    };

    View.OnClickListener appStopButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentView = (View) v.getParent().getParent();
            Spinner appSpinner = (Spinner) parentView.findViewById(R.id.appSpinner);
            Pair<String, String> pair = (Pair<String, String>) appSpinner.getSelectedItem();
            if (pair.first != null && pair.first.length() > 0) {
                mPepper.AppStop(pair.first);
            } else {
                Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.str_app_nothing), Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * Gesture
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener gestureButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentView = (View) v.getParent().getParent();
            Spinner gestureSpinner = (Spinner) parentView.findViewById(R.id.gestureSpinner);
            Pair<String, String> pair = (Pair<String, String>) gestureSpinner.getSelectedItem();
            if (pair.first != null && pair.first.length() > 0) {
                mPepper.Gesture(pair.first);
            } else {
                Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.str_gesture_nothing), Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 録音開始
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener recStartButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPepper.RecStart();
        }
    };

    /**
     * --------------------------------------------------------------------------------
     * 録音終了
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener recStopButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPepper.RecStop();
        }
    };
}