package jp.co.atika.pepperrc;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.util.List;

public class FragmentSayEdit extends Fragment {
    private static final String TAG = "FragmentSayEdit";

    // 共通関数
    private Cmn mCmn;
    private Pepper mPepper;
    private View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 共通関数
        mCmn = new Cmn(this.getActivity());
        mPepper = new Pepper(this.getActivity());
        mPepper.Connect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // レイアウトを描画して返す
        v = inflater.inflate(R.layout.fragment_say_edit, null);
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
     *
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

        // ----------------------------------------
        // 録音
        // ----------------------------------------
        // ボタンにリスナを割当
        Button recStartButton = (Button) v.findViewById(R.id.recStartButton);
        recStartButton.setOnClickListener(recStartButtonClickListener);
        Button recStopButton = (Button) v.findViewById(R.id.recStopButton);
        recStopButton.setOnClickListener(recStopButtonClickListener);

        // ----------------------------------------
        // 一覧
        // ----------------------------------------
        List<Pair<String, String>> mSayList = mCmn.getSayList(true, false);
        SayAdapter mSayAdapter = new SayAdapter(mCmn.getContext(), 0, mSayList, mCmn);
        ListView listView = (ListView) v.findViewById(R.id.sayListView);
        listView.setAdapter(mSayAdapter);

        // タップでしゃべる
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                // 選択アイテムを取得
                ListView listView = (ListView)parent;
                final Pair<String, String> item = (Pair<String, String>)listView.getItemAtPosition(pos);

                String sayMsg = item.second;
                if (sayMsg.length() > 0) {
                    mPepper.Say(sayMsg);
                } else {
                    Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.str_say_nostring), Toast.LENGTH_LONG).show();
                }
            }
        });

        // 長押しで編集
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                // 選択アイテムを取得
                ListView listView = (ListView)parent;
                final Pair<String, String> item = (Pair<String, String>)listView.getItemAtPosition(pos);

                final EditText editView = new EditText(mCmn.getContext());
                editView.setText(item.second);
                new AlertDialog.Builder(mCmn.getContext())
                        //setViewにてビューを設定します。
                        .setView(editView)
                        .setPositiveButton(mCmn.getString(R.string.action_save), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                SpannableStringBuilder sb = (SpannableStringBuilder)editView.getText();
                                String str = sb.toString();
                                mCmn.setSayItem(new Pair<String, String>(item.first, str));
                                setMe();
                            }
                        })
                        .setNeutralButton(mCmn.getString(R.string.action_delete), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCmn.delSayItem(item);
                                setMe();
                            }
                        })
                        .setNegativeButton(mCmn.getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return false;
            }
        });

        // 戻るボタン
        Button returnButton = (Button) v.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(returnButtonClickListener);

        // 追加ボタン
        Button addButton = (Button) v.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentView = (View) v.getParent().getParent();
                EditText msgEditText = (EditText) parentView.findViewById(R.id.msgEditText);
                SpannableStringBuilder sb = (SpannableStringBuilder)msgEditText.getText();
                String str = sb.toString();
                if(str==null || str.length()==0){
                    Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.str_say_nostring2), Toast.LENGTH_LONG).show();
                } else {
                    // free版
                    if(mCmn.getSayCnt()>=5){
                        Toast.makeText(mCmn.getContext(), mCmn.getString(R.string.msg_free_5), Toast.LENGTH_LONG).show();
                    } else {
                        mCmn.setSayItem(new Pair<String, String>("", str));
                        msgEditText.setText("");
                        setMe();
                    }
                }
            }
        });
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

    /**
     * --------------------------------------------------------------------------------
     * 戻る
     * --------------------------------------------------------------------------------
     */
    View.OnClickListener returnButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int backStackCnt = getFragmentManager().getBackStackEntryCount();
            if (backStackCnt != 0) {
                getFragmentManager().popBackStack();
            }
        }
    };

}