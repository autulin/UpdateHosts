package com.autulin.updatehosts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.listener.OnFileDownloadStatusListener;
import org.wlf.filedownloader.listener.simple.OnSimpleFileDownloadStatusListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MyApplication.DataChangeListener {

    private SwipeMenuListView listView;
    private MyListAdapter adapter;
    private MyApplication myApplication;
    private List<HostsBean> list;
    private FloatingActionButton fab;
    Context mContext = this;
    private OnFileDownloadStatusListener mOnFileDownloadStatusListener = new OnSimpleFileDownloadStatusListener() {

        @Override
        public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
            // 下载完成（整个文件已经全部下载完成）
            List<String> commnandList = new ArrayList<>();
            commnandList.add("mount -o rw,remount /system");
            commnandList.add("cp " + downloadFileInfo.getFilePath() + " /system/etc/hosts");
//                commnandList.add("rm " + downloadFileInfo.getFilePath());
            ShellUtils.CommandResult execResult = ShellUtils.execCommand(commnandList, true, true);
            Log.e("root", execResult.toString());
            Snackbar.make(fab, "更新成功，请重启或开/关飞行模式后生效", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        @Override
        public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
            // 下载失败了，详细查看失败原因failReason，有些失败原因你可能必须关心

            String failType = failReason.getType();

            if (FileDownloadStatusFailReason.TYPE_URL_ILLEGAL.equals(failType)) {
                // 下载failUrl时出现url错误
                Snackbar.make(fab, "下载失败，URL(" + url + ")有误", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else if (FileDownloadStatusFailReason.TYPE_STORAGE_SPACE_IS_FULL.equals(failType)) {
                // 下载failUrl时出现本地存储空间不足
                Snackbar.make(fab, "下载失败，本地存储空间不足", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else if (FileDownloadStatusFailReason.TYPE_NETWORK_DENIED.equals(failType)) {
                // 下载failUrl时出现无法访问网络
                Snackbar.make(fab, "下载失败，无法访问网络", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else if (FileDownloadStatusFailReason.TYPE_NETWORK_TIMEOUT.equals(failType)) {
                // 下载failUrl时出现连接超时
                Snackbar.make(fab, "下载失败，连接超时", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                // 更多错误....
                Snackbar.make(fab, "下载失败，未知错误", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

            // 查看详细异常信息
//            Throwable failCause = failReason.getCause();// 或：failReason.getOriginalCause()

            // 查看异常描述信息
//            String failMsg = failReason.getMessage();// 或：failReason.getOriginalCause().getMessage()
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myApplication = (MyApplication) getApplication();
        myApplication.addDataChangeListener(this); //添加事件监听

        list = myApplication.getList();

        initSwipeMenuListView();

        FileDownloader.registerDownloadStatusListener(mOnFileDownloadStatusListener);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加源
                new EditDialog(mContext, myApplication, -1).show();
            }
        });
        showTipDlg(); //第一次运行提醒一下
    }

    private void showTipDlg() {
        if (!IOUtils.isShowTip(this)) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_apl_tip, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.no_more_chk);
            new AlertDialog.Builder(this).setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (checkBox.isChecked()) {
                                IOUtils.markNotShowTip(getApplicationContext());
                            }
                        }
                    }).create().show();
        }
    }

    private void initSwipeMenuListView() {
        listView = (SwipeMenuListView) findViewById(R.id.listView);

        //创建滑动菜单
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem editItem = new SwipeMenuItem(
                        getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                editItem.setWidth(dp2px(90));
                editItem.setTitle("打开");
                editItem.setTitleSize(18);
                editItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(editItem);

                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(52, 52,
                        255)));
                openItem.setWidth(dp2px(90));
                openItem.setTitle("应用");
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(255, 32, 32)));
                deleteItem.setWidth(dp2px(90));
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setMenuCreator(creator);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new EditDialog(mContext, myApplication, position).show();
            }
        });

        //设定滑动按钮监听事件
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0://打开
                        Log.e("click position", position + "");
                        File file = new File(IOUtils.FILE_PATH + list.get(position).getTitle());
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri2 = Uri.fromFile(file);
                        intent.setDataAndType(uri2, "text/plain");
                        startActivity(intent);
                        break;
                    case 1://应用
                        myApplication.applyHosts(position);
                        break;
                    case 2://删除
                        myApplication.deleteHosts(position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        //设定滑动方向
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        adapter = new MyListAdapter(list);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_backup) {

            new EditDialog(mContext, myApplication, -2).show();

        }
        if (id == R.id.action_clear) {
            myApplication.clearData();
        }
        if (id == R.id.action_about) {
            String versionName = null;
            try {
                versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            new AlertDialog.Builder(this).setTitle("Hosts一键更新 " + versionName + "Beta").setMessage(
                    "更新Hosts是最方便快捷的科学上网方法，并且还可以去除广告，" +
                            "缺点是对部分CDN无效（如可以上Google Play但不能下载东西，能打开Youtube但不一定能看视屏），" +
                            "另外有一定的时效性，需定期更新，这是关键，故造此轮，可以更方便的更新各种Hosts源，平时用用Google什么的毫无压力\n\n" +
                            "Powered By autulin\n" +
                            "感谢以下开源软件的支持：\n" +
                            "SwipeMenuListView FileDownloader Fastjson Trinea\n" +
                            "最后警告：Root有风险，操作需谨慎，本人不承担一切后果！").setPositiveButton("确定", null).create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChanged(List<HostsBean> list) {
        this.list = list;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileDownloader.unregisterDownloadStatusListener(mOnFileDownloadStatusListener);
    }

    class MyListAdapter extends BaseAdapter {
        List<HostsBean> list;

        MyListAdapter(List<HostsBean> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public HostsBean getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.listitem, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(list.get(position).getTitle());
            holder.address.setText(list.get(position).getAddress());

            return convertView;
        }

        class ViewHolder {
            TextView title;
            TextView address;

            public ViewHolder(View view) {
                title = (TextView) view.findViewById(R.id.title);
                address = (TextView) view.findViewById(R.id.address);
                view.setTag(this);
            }
        }

        public boolean getSwipEnableByPosition(int position) {
            return position % 2 != 0;
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


}
