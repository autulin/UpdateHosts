package com.autulin.updatehosts;

import android.app.Application;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.wlf.filedownloader.FileDownloadConfiguration;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.listener.OnDetectBigUrlFileListener;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private List<HostsBean> list;
    private DataChangeListener dataChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initFileDownloader();
    }

    private void initFileDownloader() {
        // 1、创建Builder
        FileDownloadConfiguration.Builder builder = new FileDownloadConfiguration.Builder(this);

// 2.配置Builder
// 配置下载文件保存的文件夹
        builder.configFileDownloadDir(IOUtils.FILE_PATH);
// 配置同时下载任务数量，如果不配置默认为2
//        builder.configDownloadTaskSize(3);
// 配置失败时尝试重试的次数，如果不配置默认为0不尝试
//        builder.configRetryDownloadTimes(5);
// 开启调试模式，方便查看日志等调试相关，如果不配置默认不开启
        builder.configDebugMode(true);
// 配置连接网络超时时间，如果不配置默认为15秒
        builder.configConnectTimeout(5000);// 25秒

// 3、使用配置文件初始化FileDownloader
        FileDownloadConfiguration configuration = builder.build();
        FileDownloader.init(configuration);
    }


    private void initData() {
        list = new ArrayList<>();

        String data;
        if (!IOUtils.isFirstTime(this)) {
            data = IOUtils.readStringFromRaw(this, R.raw.init_data);
            if (!data.equals("")) {
                list = JSON.parseArray(data, HostsBean.class);
                IOUtils.markNotFirstTime(this);
                IOUtils.setDataToSP(this, data);
            }
        } else {
            data = IOUtils.getDataFromSP(this);
            if (data != null) {
                list = JSON.parseArray(data, HostsBean.class);
            }
        }
    }


    public List<HostsBean> getList() {
        return list;
    }

    public void clearData() {
        list.clear();
        dataChangeListener.onDataChanged(list);
        IOUtils.setDataToSP(this, "");
    }

    public void editList(int position, String title, String address) {
        list.get(position).setTitle(title);
        list.get(position).setAddress(address);

        dataChangeListener.onDataChanged(list);
        IOUtils.setDataToSP(this, JSON.toJSONString(list));
    }

    public int add2List(String title, String address, boolean isBacked) {
        HostsBean hostsBean = new HostsBean(title, address, isBacked);
        list.add(hostsBean);
        dataChangeListener.onDataChanged(list);
        IOUtils.setDataToSP(this, JSON.toJSONString(list));
        return list.indexOf(hostsBean);
    }

    public void deleteFromeList(int position) {
        list.remove(position);
        dataChangeListener.onDataChanged(list);
        IOUtils.setDataToSP(this, JSON.toJSONString(list));
    }

    public String getTitle(int position) {
        return list.get(position).getTitle();
    }

    public String getAddress(int position) {
        return list.get(position).getAddress();
    }

    public boolean isBacked(int position) {
        return list.get(position).isBacked();
    }

    public void addDataChangeListener(DataChangeListener dataChangeListener) {
        this.dataChangeListener = dataChangeListener;
    }

    public void bakHosts(String title){
        add2List(title, "（备份的文件）", true);

        //备份操作
        List<String> commnandList = new ArrayList<>();
        commnandList.add("mount -o rw,remount /system");
        commnandList.add("cp /system/etc/hosts " + IOUtils.FILE_PATH + title);
        ShellUtils.CommandResult execResult = ShellUtils.execCommand(commnandList, true, true);
        Log.e("root", execResult.toString());
    }

    public void applyHosts(final int position){
        if (this.isBacked(position)) { //恢复文件
            List<String> commnandList = new ArrayList<>();
            commnandList.add("mount -o rw,remount /system");
            commnandList.add("cp " + IOUtils.FILE_PATH + this.getTitle(position) + " /system/etc/hosts");
            ShellUtils.CommandResult execResult = ShellUtils.execCommand(commnandList, true, true);
            Log.e("root", execResult.toString());
        } else {
            FileDownloader.detect(this.getAddress(position), new OnDetectBigUrlFileListener() {
                @Override
                public void onDetectNewDownloadFile(String url, String fileName, String saveDir, long fileSize) {
                    // 如果有必要，可以改变文件名称fileName和下载保存的目录saveDir
                    FileDownloader.createAndStart(url, saveDir, getTitle(position));
                }

                @Override
                public void onDetectUrlFileExist(String url) {
                    // 继续下载，自动会断点续传（如果服务器无法支持断点续传将从头开始下载）
                    FileDownloader.reStart(url);
                }

                @Override
                public void onDetectUrlFileFailed(String url, DetectBigUrlFileFailReason failReason) {
                    // 探测一个网络文件失败了，具体查看failReason
                }
            });

           //之后去MainActivity里面的Listener等结果吧
        }
    }
    public void deleteHosts(final int position){

        if(!isBacked(position)){
            FileDownloader.delete(getAddress(position), true, null); //删除文件
        }else {
            List<String> commnandList = new ArrayList<>();
            commnandList.add("rm " + IOUtils.FILE_PATH + this.getTitle(position));
            ShellUtils.CommandResult execResult = ShellUtils.execCommand(commnandList, false, true);
            Log.e("root", execResult.toString());
        }
        deleteFromeList(position);
    }

    public interface DataChangeListener {
        void onDataChanged(List<HostsBean> list);
    }

}
