package com.elso.hkvideodemo;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.RealPlayCallBack;

import org.MediaPlayer.PlayM4.Player;
import org.MediaPlayer.PlayM4.PlayerCallBack;

/**
 * Created by elso on 2016/12/20.
 * Video工具类
 */

class VideoUtil {

    private Context context;
    private SurfaceHolder holder;
    private int login_ID;       //登陆结果
    private int realplay_ID;    //实时播放结果
    private int playPort;       //播放端口
    //错误代码
    private final int[] errorCode = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8 ,9 ,10};
    //错误具体信息
    private final String[] errorMessage = new String[]{"获取播放端口失败", "设置流播放模式失败", "打开流失败",
            "设置播放缓冲区最大缓冲帧数失败", "设置视频帧解码类型失败", "失败",  "播放失败",
            "停止实时播放失败", "停止播放失败", "关闭流失败", "释放端口失败"};

    /**
     * 构造函数
     * 2016/12/20   elso
     * @param context   上下文
     * @param holder    需要显示的SurfaceView
     */
    VideoUtil(Context context, SurfaceHolder holder){
        this.context = context;
        this.holder = holder;
        initSDK();
    }

    /**
     * 初始化SDK
     * 2016/12/20   elso
     */
    private void initSDK(){
        if (HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.i("------------>", "初始化SDK成功");
        }else {
            Log.i("------------>", "初始化SDK失败");
        }
    }

    /**
     * 登陆方法
     * 2016/12/20   elso
     * @param ip        IP
     * @param port      端口
     * @param userName  用户名
     * @param userPwd   密码
     */
    void userLogin(String ip, int port, String userName, String userPwd){
        NET_DVR_DEVICEINFO_V30 deviceinfo_v30 = new NET_DVR_DEVICEINFO_V30();   //设备信息
        login_ID =  HCNetSDK.getInstance().NET_DVR_Login_V30(ip, port, userName, userPwd, deviceinfo_v30);
        System.out.println("下面是设备信息************************");
        System.out.println("userId=" + login_ID);
        System.out.println("通道开始=" + deviceinfo_v30.byStartChan);
        System.out.println("通道个数=" + deviceinfo_v30.byChanNum);
        System.out.println("设备类型=" + deviceinfo_v30.byDVRType);
        System.out.println("ip通道个数=" + deviceinfo_v30.byIPChanNum);
    }

    /**
     * 开始实时播放
     * 2016/12/20   elso
     * @param port  Channel
     */
    void startRealPlay(int port){
        if (login_ID != -1){
            NET_DVR_PREVIEWINFO previewinfo = new NET_DVR_PREVIEWINFO();    //预览参数
            previewinfo.lChannel = port;
            previewinfo.dwStreamType = 1;
            previewinfo.dwLinkMode = 0;
            previewinfo.byPreviewMode = 0;
            realplay_ID =  HCNetSDK.getInstance().NET_DVR_RealPlay_V40(login_ID, previewinfo, realPlayCallBack);
        }else {
            Toast.makeText(context, "登陆失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 停止实时播放
     * 2016/12/20   elso
     */
    void stopRealPlay(){
        boolean[] stopStates = new boolean[]{HCNetSDK.getInstance().NET_DVR_StopRealPlay(realplay_ID), Player.getInstance().stop(playPort),
                Player.getInstance().closeStream(playPort), Player.getInstance().freePort(playPort)};
        for (int i = 0; i < stopStates.length; i++) {
            if (!stopStates[i]) {
                errorList(i + 7);
                break;
            }
        }
    }

    /**
     * 码流数据回调函数
     * 2016/12/20   elso
     */
    private RealPlayCallBack realPlayCallBack = new RealPlayCallBack() {
        @Override
        public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
            processRealData(iDataType, pDataBuffer, iDataSize);
        }
    };

    /**
     * 实时播放数据处理
     * 2016/12/20   elso
     * @param iDataType     数据类型
     * @param pDataBuffer   数据信息
     * @param iDataSize     数据长度
     */
    private void processRealData(int iDataType, byte[] pDataBuffer, int iDataSize){
        switch (iDataType){
            case HCNetSDK.NET_DVR_SYSHEAD:
                Log.d("------------>", "处理头数据");
                playPort = Player.getInstance().getPort();
                boolean[] realPlayState = new boolean[]{playPort != -1, Player.getInstance().setStreamOpenMode(playPort, Player.STREAM_REALTIME),
                        Player.getInstance().openStream(playPort, pDataBuffer, iDataSize, 200 * 1024),
                        Player.getInstance().setDisplayBuf(playPort, 6),
                        Player.getInstance().setDecodeFrameType(playPort, 0),
                        Player.getInstance().setDisplayCB(playPort, null),
                        Player.getInstance().play(playPort, holder)};
                for (int i = 0; i < realPlayState.length; i++) {
                    if (!realPlayState[i]){
                        errorList(i);
                        break;
                    }
                }
                break;
            case HCNetSDK.NET_DVR_STREAMDATA:
                if (!Player.getInstance().inputData(playPort, pDataBuffer, iDataSize)){
                    Log.i("------------>", "输入数据失败");
                }
        }
    }

    /**
     * 抓图回调函数
     * 2016/12/20   elso
     */
    PlayerCallBack.PlayerDisplayCB playerDisplayCB = new PlayerCallBack.PlayerDisplayCB() {
        @Override
        public void onDisplay(int nPort, byte[] data, int nDataLen, int nWidth, int nHeight, int nFrameTime, int nDataType, int Reserved) {

        }
    };

    /**
     * 释放SDK
     * 2016/12/20   elso
     */
    void freeSDK(){
        if (HCNetSDK.getInstance().NET_DVR_Cleanup()) {
            Log.i("------------>", "释放SDK资源成功");
        }else {
            Log.e("------------>", "释放SDK资源失败");
        }
    }

    /**
     * 错误列表
     * 2016/12/20   elso
     * @param num   发生错误的位置
     */
    private void errorList(int num){
        for (int code : errorCode){
            if (num == code) {
                Log.i("------------>", errorMessage[num]);
            }
        }
    }

}
