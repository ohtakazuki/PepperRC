package jp.co.atika.pepperrc;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * PreferenceFragmentを継承したクラス
 * 個々で定義したPreferenceのリソースを設定します
 */
public class FragmentPref extends PreferenceFragment {
    static final String TAG = "FragmentPref";

    // 共通関数
    private Cmn mCmn;

    /***
     * Fragmentの初期化処理を行う
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 設定画面を定義したXMLを読み込む
        addPreferencesFromResource(R.xml.pref);
        // 共通関数
        mCmn = new Cmn(this.getActivity());

        // マスタ取得のprefにクリックイベントを設定する
        // XMLで設定したPreferenceを取得するにはandroid:keyを使い、PreferenceFragment#findPreference()で行う
        Preference button = findPreference(getString(R.string.pref_test_key));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Pepper pepper = new Pepper(mCmn.getContext());
                pepper.Connect();
                return true;
            }
        });
    }

    /**
     * Fragmentが関連付いているActivityのonCreate()が呼ばれた直後に呼び出される。
     * ListView にAdapter を セットするなどの時は、Viewが作成された後でなければならないのでこのメソッドで行う。
     * 今までActivityのonCreate()で行なっていた処理はこのメソッドに記述する
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 必要であれば、onActivityCreated()で各設定項目の初期値等を設定する
    }
}