package com.autulin.updatehosts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class EditDialog extends AlertDialog {
    Context mContext;
    MyApplication myApplication;
    EditText title, address;
    LinearLayout addressll;


    /**
     * @param context
     * @param position 编辑数据传送位置值，添加数据传-1，备份数据传-2
     */
    public EditDialog(Context context, final MyApplication myApplication, final int position) {
        super(context);
        mContext = context;
        this.myApplication = myApplication;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.dialog_resource_add, null);
        title = (EditText) v.findViewById(R.id.title);
        address = (EditText) v.findViewById(R.id.address);
        addressll = (LinearLayout) v.findViewById(R.id.address_ll);

        //添加备份
        if (position == -2) {
            this.setTitle("备份当前源");
            addressll.setVisibility(View.GONE);

            this.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (title.getText().toString().equals("")) {
                        Toast.makeText(mContext, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        myApplication.bakHosts(title.getText().toString());
                    }
                }
            });

        } else {

            //添加源
            if (position == -1) {
                this.setTitle("添加源");
            } else {

                //编辑源
                title.setText(myApplication.getTitle(position));
                address.setText(myApplication.getAddress(position));
                this.setTitle("编辑源");
            }

            this.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (title.getText().toString().equals("") || address.getText().toString().equals("")) {
                        Toast.makeText(mContext, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (position == -1) {
                            myApplication.add2List(title.getText().toString(), address.getText().toString(), false);
                        } else {
                            myApplication.editList(position, title.getText().toString(), address.getText().toString());
                        }
                    }
                }
            });

            //应用源
            this.setButton(DialogInterface.BUTTON_NEUTRAL, "应用", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (position == -1) { //如果是添加源的时候直接点“应用”,则先添加，后应用
                        myApplication.applyHosts(myApplication.add2List(title.getText().toString(), address.getText().toString(), false));
                    } else {
                        myApplication.editList(position, title.getText().toString(), address.getText().toString());
                        myApplication.applyHosts(position);
                    }
                }
            });

        }


        this.setCancelable(true);
        this.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        this.setView(v);
    }

}
