package com.spf.album.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.spf.album.R;
import com.spf.album.event.WordMarkEvent;

import org.greenrobot.eventbus.EventBus;

public class DialogUtils {
    public static void showWordMarkDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_word_mark, null);
        EditText editText = view.findViewById(R.id.et_word);
        RadioButton rbWhite = view.findViewById(R.id.rb_white);
        RadioButton rbBlack = view.findViewById(R.id.rb_black);
        RadioButton rbRed = view.findViewById(R.id.rb_red);
        builder.setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String content = editText.getText().toString().trim();
                        int color = Color.RED;
                        if (rbWhite.isChecked()) {
                            color = Color.WHITE;
                        } else if (rbBlack.isChecked()) {
                            color = Color.BLACK;
                        }
                        EventBus.getDefault().post(new WordMarkEvent(content, color));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EventBus.getDefault().post(new WordMarkEvent("", 0));
                    }
                });
        builder.create().show();
    }
}