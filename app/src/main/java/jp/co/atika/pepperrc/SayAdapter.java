package jp.co.atika.pepperrc;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SayAdapter extends ArrayAdapter<Pair<String, String>> {
    private LayoutInflater layoutInflater_;

    Cmn mCmn;

    public SayAdapter(Context context, int textViewResourceId, List<Pair<String, String>> objects, Cmn cmn) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCmn = cmn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 特定の行(position)のデータを得る
        final Pair<String, String> item = (Pair<String, String>)getItem(position);

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = layoutInflater_.inflate(R.layout.say_item, null);
        }

        TextView msgTextView = (TextView)convertView.findViewById(R.id.msgTextView);
        msgTextView.setText(item.second);

        /*
        // 編集ボタン
        {
            Button editButton = (Button)convertView.findViewById(R.id.editButton);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //テキスト入力を受け付けるビューを作成します。
                    final EditText editView = new EditText(getContext());
                    editView.setText(item.second);
                    new AlertDialog.Builder(getContext())
                            //setViewにてビューを設定します。
                            .setView(editView)
                            .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    SpannableStringBuilder sb = (SpannableStringBuilder)editView.getText();
                                    String str = sb.toString();
                                    mCmn.setSayItem(new Pair<String, String>(item.first, str));
                                    //SayAdapter.this.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                }
            });
        }

        // 削除ボタン
        {
            Button delButton = (Button)convertView.findViewById(R.id.delButton);
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mCmn.delSayItem(item);
                    //SayAdapter.this.notifyDataSetChanged();
                }
            });
        }
        */

        return convertView;
    }

}