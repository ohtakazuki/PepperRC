package jp.co.atika.pepperrc;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * SpinnerにKey-Valueを設定するためのアダプタークラス
 *
 */
public class KeyValuePairAdapter extends ArrayAdapter<Pair<String, String>> {

    /**
     * コンストラクタ
     *
     * @param context
     *          スピナーを利用するアクティビティ
     * @param resourceId
     *          スピナー表示レイアウトのリソースID
     * @param list
     *          ドロップダウンリストに設定する配列
     */
    public KeyValuePairAdapter(Context context,
                               int resourceId,
                               ArrayList<Pair<String, String>> list) {
        super(context, resourceId, list);
    }

    // スピナー表示用
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(pos, convertView, parent);
        textView.setText(getItem(pos).second);
        return textView;
    }

    // スピナードロップダウン表示用
    @Override
    public View getDropDownView(int pos, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(pos,
                convertView,
                parent);
        textView.setText(getItem(pos).second);
        return textView;
    }

}