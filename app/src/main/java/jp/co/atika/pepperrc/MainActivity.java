package jp.co.atika.pepperrc;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.aldebaran.qi.EmbeddedTools;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static final String TAG = "MainActivity";

    // 表示中のフラグメント番号(R.id.nav_xxx)
    private int mFragmentNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウトの指定
        setContentView(R.layout.activity_main);

        // ----------------------------------------
        // ツールバー（左上の「三」）とDrawerを対応付ける
        // ----------------------------------------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // ツールバーをActionBarとして使う場合の指定
        setSupportActionBar(toolbar);

        // 親のレベルの要素
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        // toggleの状態をDrawerとシンクさせる
        toggle.syncState();

        // ----------------------------------------
        // 横からスライドして表示されるメニューに対してリスナをthisに設定
        // ----------------------------------------
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Embedded Tools
        EmbeddedTools tools = new EmbeddedTools();
        File dir = getApplicationContext().getCacheDir();
        tools.overrideTempDirectory(dir);
        tools.loadEmbeddedLibraries();

        // 初期表示はコントロール
        mFragmentNo = R.id.nav_control;
    }

    /**
     * Backボタンを押したら NavigationDrawerが表示されていたら閉じる
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ActionBarとレイアウトの対応付け
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 画面の再表示
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // フラグメント番号のフラグメントを表示する
        if(hasFocus){
            showFragment();
        }
    }

    /**
     * フラグメント番号に該当するフラグメントを表示する
     */
    private void showFragment(){
        String fragmentNoString = String.valueOf(mFragmentNo);
        // フラグメント番号のフラグメントが表示されていたら再描画しない
        try {
            Fragment dmy = (Fragment) getFragmentManager().findFragmentByTag(fragmentNoString);
            if(dmy!=null){
                return;
            }
        } catch (ClassCastException e) {}

        // 再描画処理
        if (mFragmentNo == R.id.nav_control) {
            // ダウンロード
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new FragmentControl(), fragmentNoString).commit();

        } else if (mFragmentNo == R.id.nav_manage) {
            // 設定画面
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new FragmentPref(), fragmentNoString).commit();
        }
    }

    /**
     * ドロアメニュー選択イベント
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // フラグメントの再表示
        mFragmentNo = item.getItemId();
        showFragment();

        // ナビゲーションドロアの処理
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
