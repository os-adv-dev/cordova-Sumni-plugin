package com.outsystems.sumnisdk;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import android.util.Log;
import android.view.Display;

import com.outsystems.sumnisdk.present.WebviewDisplay;
import com.outsystems.sumnisdk.utils.DataModel;
import com.outsystems.sumnisdk.utils.ScreenManager;
import com.outsystems.sumnisdk.utils.SharePreferenceUtil;
import com.outsystems.sumnisdk.utils.UPacketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sunmi.ds.DSKernel;
import sunmi.ds.callback.ICheckFileCallback;
import sunmi.ds.callback.IConnectionCallback;
import sunmi.ds.callback.IReceiveCallback;
import sunmi.ds.callback.ISendCallback;
import sunmi.ds.callback.ISendFilesCallback;
import sunmi.ds.callback.QueryCallback;
import sunmi.ds.data.DSData;
import sunmi.ds.data.DSFile;
import sunmi.ds.data.DSFiles;
import sunmi.ds.data.DataPacket;


public class SumniPlugin extends CordovaPlugin {

    private final String TAG = this.getServiceName();
    private DSKernel mDSKernel = null;
    private CallbackContext mCallback;
    private ScreenManager screenManager = ScreenManager.getInstance();
    private WebviewDisplay webviewDisplay;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        screenManager.init(cordova.getActivity());
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext){
        PluginResult pr;
        switch (action) {
            case "presentWebView":
                if(args.length()<1){
                    sendErrorMessage(7,"This action needs 1 argument to be used!",callbackContext);
                    return false;
                }
                try {
                    String url = args.getString(0);
                    Display[] displays = screenManager.getDisplays();
                    Log.e(TAG, "Display's available" + displays.length);
                    for (int i = 0; i < displays.length; i++) {
                        Log.e(TAG, "Display information" + displays[i]);
                    }
                    Display display = screenManager.getPresentationDisplays();
                    if (display != null) {//&& !isVertical
                        cordova.getActivity().runOnUiThread(() -> {
                            webviewDisplay = new WebviewDisplay(cordova.getActivity(), display,url,callbackContext);
                            webviewDisplay.show();
                        });

                    }
                }catch(JSONException e){
                    sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),callbackContext);
                    return false;
                }
                return true;
            case "getWebviewData":
                String jsonFromWebview = SharePreferenceUtil.getParam(cordova.getActivity(),"webviewData");
                pr = new PluginResult(PluginResult.Status.OK,jsonFromWebview);
                pr.setKeepCallback(false);
                callbackContext.sendPluginResult(pr);
                return true;
            case "setWebviewData":
                if(args.length()<1){
                    sendErrorMessage(7,"This action needs 1 argument to be used!",callbackContext);
                    return false;
                }
                String jsonToWebview = args.optString(0);
                SharePreferenceUtil.setParam(cordova.getActivity(),"webviewData",jsonToWebview);
                pr = new PluginResult(PluginResult.Status.OK);
                pr.setKeepCallback(false);
                callbackContext.sendPluginResult(pr);
                return true;
            case "initsdk":
                //------------------------------Init-------------------------------------------//
                initSdk();
                mCallback = callbackContext;
                pr = new PluginResult(PluginResult.Status.OK);
                pr.setKeepCallback(true);
                callbackContext.sendPluginResult(pr);
                return true;
            case "test":
                //------------------------------Test-------------------------------------------//
                if(args.length()<1){
                    sendErrorMessage(7,"This action needs 1 argument to be used!",callbackContext);
                    return false;
                }
                String testString = args.optString(0);
                mDSKernel.TEST(testString);
                pr = new PluginResult(PluginResult.Status.OK);
                pr.setKeepCallback(false);
                callbackContext.sendPluginResult(pr);
                return true;
            case "sendFiles":
                //------------------------------Send Files-------------------------------------------//
                if(args.length()<3){
                    sendErrorMessage(7,"This action needs 3 argument to be used!",callbackContext);
                    return false;
                }
                String filesPackageName = args.optString(0);
                String filesMessage = args.optString(1);
                try {
                    JSONArray filesPaths = new JSONArray(args.getString(2));
                    sendFiles(filesPackageName, filesMessage, filesPaths, callbackContext);
                }catch (JSONException e){
                    sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),callbackContext);
                    return false;
                }
                return true;
            case "sendFile":
                //------------------------------Send File-------------------------------------------//
                if(args.length()<3){
                    sendErrorMessage(7,"This action needs 3 argument to be used!",callbackContext);
                    return false;
                }
                String filePackage = args.optString(0);
                String fileMessage = args.optString(1);
                String filePath = args.optString(2);
                sendFile(filePackage,fileMessage,filePath,callbackContext);
                return true;
            case "sendCMD":
                //------------------------------Send Command-------------------------------------------//
                if(args.length()<2){
                    sendErrorMessage(7,"This action needs at least 2 argument to be used!",callbackContext);
                    return false;
                }
                String cmdPackName = args.optString(0);
                String cmdOption = args.optString(1);
                ISendCallback cmdCallback = new ISendCallback() {
                    @Override
                    public void onSendSuccess(long taskId) {
                        PluginResult prcmd = new PluginResult(PluginResult.Status.OK,taskId);
                        prcmd.setKeepCallback(false);
                        callbackContext.sendPluginResult(prcmd);
                    }

                    @Override
                    public void onSendFail(int errorId, String errorInfo) {
                        sendErrorMessage(errorId,errorInfo,callbackContext);
                    }

                    @Override
                    public void onSendProcess(long totle, long sended) {

                    }
                };
                switch(cmdOption){
                    case "CUSTOM":
                        if(args.length()<4){
                            sendErrorMessage(7,"This action needs 4 argument to be used!",callbackContext);
                            return false;
                        }
                        String cmdMessage = args.optString(2);
                        long cmdCFileID = args.optLong(3);
                        sendCMD(cmdPackName,cmdMessage,cmdCFileID,callbackContext);
                        break;
                    case "CMDPACK":
                        if(args.length()<5){
                            sendErrorMessage(7,"This action needs 5 argument to be used!",callbackContext);
                            return false;
                        }
                        String cmdModelString = args.optString(4);
                        DataModel cmdModel = getDataModel(cmdModelString);
                        if(cmdModel == null){
                            sendErrorMessage(10,"Model could not be identified!",callbackContext);
                            return false;
                        }
                        String cmdJson = args.optString(2);
                        long cmdfileID = args.optLong(3);
                        mDSKernel.sendCMD(UPacketFactory.buildCMDPack(cmdPackName, cmdModel, cmdJson, cmdfileID, cmdCallback));
                        break;
                    case "OPENAPP":
                        mDSKernel.sendCMD(UPacketFactory.buildOpenApp(cmdPackName, cmdCallback));
                        break;
                    case "SHUTDOWN":
                        mDSKernel.sendCMD(UPacketFactory.buildShutDown(cmdPackName, cmdCallback));
                        break;
                    case "REBOOT":
                        mDSKernel.sendCMD(UPacketFactory.buildReboot(cmdPackName, cmdCallback));
                        break;
                    case "SECONDSCREENDATA":
                        mDSKernel.sendCMD(UPacketFactory.buildSecondScreenData(cmdPackName, cmdCallback));
                        break;
                    case "SCREENUNLOCK":
                        mDSKernel.sendCMD(UPacketFactory.buildScreenUnlock(cmdPackName, cmdCallback));
                        break;
                    case "READBRIGHTNESS":
                        if(args.length()<3){
                            sendErrorMessage(7,"This action needs 3 argument to be used!",callbackContext);
                            return false;
                        }
                        String sender = args.optString(2);
                        mDSKernel.sendCMD(UPacketFactory.buildReadbrightness(cmdPackName,sender, cmdCallback));
                        break;
                    case "SETBRIGHTNESS":
                        if(args.length()<3){
                            sendErrorMessage(7,"This action needs 3 argument to be used!",callbackContext);
                            return false;
                        }
                        int brightness = Integer.parseInt(args.optString(2));
                        mDSKernel.sendCMD(UPacketFactory.buildSetbrightness(cmdPackName,brightness, cmdCallback));
                        break;
                    case "REMOVE_FOLDERS":
                        if(args.length()<3){
                            sendErrorMessage(7,"This action needs 3 argument to be used!",callbackContext);
                            return false;
                        }
                        String cmdText = args.optString(2);
                        mDSKernel.sendCMD(UPacketFactory.remove_folders(cmdPackName, cmdText, cmdCallback));
                        break;
                    case "CLOSEAPP":
                        mDSKernel.sendCMD(UPacketFactory.buildCloseApp(cmdPackName, cmdCallback));
                        break;
                    case "SHOWDATE":
                        if(args.length()<3){
                            sendErrorMessage(7,"This action needs 3 argument to be used!",callbackContext);
                            return false;
                        }
                        String text = args.optString(2);
                        mDSKernel.sendCMD(UPacketFactory.buildShowDate(cmdPackName, text, cmdCallback));
                        break;
                    default :
                        sendErrorMessage(5,"Command Option could not be identified/ does not exist!",callbackContext);
                        return false;

                }
                return true;
            case "sendQuery":
                //------------------------------Send Query-------------------------------------------//
                if(args.length()<3){
                    sendErrorMessage(7,"This action needs at least 3 arguments to be used!",callbackContext);
                    return false;
                }
                String queryData = args.optString(0);
                String queryPackageName = args.optString(1);
                String queryOption = args.optString(2);
                switch(queryOption){
                    case "QUERY_STRING":
                        mDSKernel.sendQuery(queryPackageName, queryData,new ISendCallback() {
                                    @Override
                                    public void onSendSuccess(long taskId) {

                                    }

                                    @Override
                                    public void onSendFail(int errorId, String errorInfo) {
                                        sendErrorMessage(errorId,errorInfo,callbackContext);
                                    }

                                    @Override
                                    public void onSendProcess(long totle, long sended) {

                                    }
                                }
                                ,new QueryCallback() {
                                    @Override
                                    public void onReceiveData(DSData data) {
                                        JSONObject result = dsDataToJson(data);
                                        PluginResult prquery = new PluginResult(PluginResult.Status.OK,result);
                                        prquery.setKeepCallback(false);
                                        callbackContext.sendPluginResult(prquery);
                                    }
                                });
                        break;
                    case "QUERY_DATAPACK":
                        if(args.length()<6){
                            sendErrorMessage(7,"This action needs 6 arguments to be used!",callbackContext);
                            return false;
                        }
                        String queryType = args.optString(3);
                        Boolean queryIsReport = args.optBoolean(4);
                        long queryFileId = args.optLong(5,-1);
                        sendQuery(queryType,queryPackageName,queryData,queryIsReport,queryFileId,callbackContext);
                        break;
                    default :
                        sendErrorMessage(5,"Query Option could not be identified/ does not exist!",callbackContext);
                        return false;
                }
                return true;
            case "checkFileExists":
                //------------------------------Check File Exits-------------------------------------------//
                if(args.length()<2){
                    sendErrorMessage(7,"This action needs 2 arguments to be used!",callbackContext);
                    return false;
                }
                long CheckExitsfileId = args.optLong(0,-1);
                String CheckExitspackageName = args.optString(1);
                sendQuery("CHECK_FILE",CheckExitspackageName,"def",true,CheckExitsfileId,callbackContext);
                return true;
            case "deleteFileExists":
                //------------------------------Delete File Exits-------------------------------------------//
                if(args.length()<1){
                    sendErrorMessage(7,"This action needs 1 argument to be used!",callbackContext);
                    return false;
                }
                long DeleteExitsTaskId = args.optLong(0);
                mDSKernel.deleteFileExist(DeleteExitsTaskId, new ICheckFileCallback() {
                    @Override
                    public void onCheckFail() {
                        sendErrorMessage(8,"There was an issue deleting this file! Id:"+DeleteExitsTaskId,callbackContext);
                    }

                    @Override
                    public void onResult(boolean exist) {
                        PluginResult prdelete = new PluginResult(PluginResult.Status.OK);
                        prdelete.setKeepCallback(false);
                        callbackContext.sendPluginResult(prdelete);
                    }
                });
                return true;
            case "checkConnection":
                //------------------------------Check Connection-------------------------------------------//
                pr = new PluginResult(PluginResult.Status.OK,mDSKernel.isConnected());
                mDSKernel.checkConnection();
                pr.setKeepCallback(false);
                callbackContext.sendPluginResult(pr);
                return true;
            case "sendData":
                //------------------------------Send Data-------------------------------------------//
                if(args.length()<3){
                    sendErrorMessage(7,"This action needs at least 3 arguments to be used!",callbackContext);
                    return false;
                }
                String dataString = args.optString(0);
                String dataPackage = args.optString(1);
                String dataOption = args.optString(2);
                DataPacket dataPacket;
                ISendCallback dataCallback = new ISendCallback() {
                    @Override
                    public void onSendSuccess(long taskId) {
                        PluginResult prData = new PluginResult(PluginResult.Status.OK,taskId);
                        prData.setKeepCallback(false);
                        callbackContext.sendPluginResult(prData);
                    }

                    @Override
                    public void onSendFail(int errorId, String errorInfo) {
                        sendErrorMessage(errorId,errorInfo,callbackContext);
                    }

                    @Override
                    public void onSendProcess(long total, long sent) {
                        //noresponse
                    }
                };
                switch(dataOption){
                    case "SHOW_TEXT":
                        dataPacket = UPacketFactory.buildShowText(dataPackage, dataString, dataCallback);
                        break;
                    case "SHOW_SINGLE_TEXT":
                        dataPacket = UPacketFactory.buildShowSingleText(dataPackage, dataString, dataCallback);
                        break;
                    case "CUSTOM":
                        if(args.length()<5){
                            sendErrorMessage(7,"This action needs at least 5 arguments to be used!",callbackContext);
                            return false;
                        }
                        DataModel customModel = getDataModel(args.optString(3));
                        if(customModel == null){
                            sendErrorMessage(10,"Model could not be identified!",callbackContext);
                            return false;
                        }
                        DSData.DataType customtype = getDataType(args.optString(4));
                        if(customtype == null){
                            sendErrorMessage(9,"DataType could not be identified!",callbackContext);
                            return false;
                        }
                        dataPacket = UPacketFactory.buildPack(dataPackage, customtype, customModel, dataString, dataCallback);
                        break;
                    case "CUSTOM_W_FILEID":
                        if(args.length()<6){
                            sendErrorMessage(7,"This action needs at least 6 arguments to be used!",callbackContext);
                            return false;
                        }
                        DataModel cfileModel = getDataModel(args.optString(3));
                        if(cfileModel == null){
                            sendErrorMessage(10,"Model could not be identified!",callbackContext);
                            return false;
                        }
                        DSData.DataType cfiletype = getDataType(args.optString(4));
                        if(cfiletype == null){
                            sendErrorMessage(9,"DataType could not be identified!",callbackContext);
                            return false;
                        }
                        long datafileId = args.optLong(5);
                        dataPacket = UPacketFactory.buildPack(dataPackage, datafileId, cfiletype, cfileModel, dataString, dataCallback);
                        break;
                    default :
                        sendErrorMessage(4,"Data Option could not be identified / does not exist!",callbackContext);
                        return false;
                }
                //The first parameter is the package name of data receiving sub-application, you can refer the demo here,
                //the second parameter is the displaying contents string, the third parameter is the result callback.

                mDSKernel.sendData(dataPacket);    //Call SendData to send text
                return true;
            case "getDSDPackageName":
                //------------------------------Get Package Name-------------------------------------------//
                pr = new PluginResult(PluginResult.Status.OK,DSKernel.getDSDPackageName());
                pr.setKeepCallback(false);
                callbackContext.sendPluginResult(pr);
                return true;
            case "createJson":
                //------------------------------Create Json-------------------------------------------//
                if(args.length()<2){
                    sendErrorMessage(7,"This action needs 2 arguments to be used!",callbackContext);
                    return false;
                }
                String jsonModel = args.optString(0);
                String jsonString = args.optString(1);
                String json = UPacketFactory.createJson(getDataModel(jsonModel), jsonString);
                pr = new PluginResult(PluginResult.Status.OK,json);
                pr.setKeepCallback(false);
                callbackContext.sendPluginResult(pr);
                return true;

            default:
                return false;
        }
    }

    private void sendErrorMessage(int errorCode,String errorMessage,CallbackContext callback){
        PluginResult pr;
        try {
            JSONObject error = new JSONObject();
            error.put("ErrorCode", errorCode);
            error.put("ErrorMessage", errorMessage);
            pr = new PluginResult(PluginResult.Status.ERROR, error);
            pr.setKeepCallback(false);
            callback.sendPluginResult(pr);
        }catch (JSONException e){
            pr = new PluginResult(PluginResult.Status.ERROR, "{\"ErrorCode\":"+errorCode+",\"ErrorMessage\":\""+errorMessage+"\"}");
            pr.setKeepCallback(false);
            callback.sendPluginResult(pr);
        }
    }

    private DataModel getDataModel(String modelString){
        DataModel model;
        switch(modelString){
            case "APK":
                model = DataModel.APK;
                break;
            case "OTA":
                model = DataModel.OTA;
                break;
            case "IMAGE":
                model = DataModel.IMAGE;
                break;
            case "IMAGES":
                model = DataModel.IMAGES;
                break;
            case "VIDEO":
                model = DataModel.VIDEO;
                break;
            case "AUDIO":
                model = DataModel.AUDIO;
                break;
            case "READ_BRIGHTNESS":
                model = DataModel.READ_BRIGHTNESS;
                break;
            case "SET_BRIGHTNESS":
                model = DataModel.SET_BRIGHTNESS;
                break;
            case "SHUTDOWN":
                model = DataModel.SHUTDOWN;
                break;
            case "SCREEN_UNLOCK":
                model = DataModel.SCREEN_UNLOCK;
                break;
            case "QRCODE":
                model = DataModel.QRCODE;
                break;
            case "GET_SECOND_SCREEN_DATA":
                model = DataModel.GET_SECOND_SCREEN_DATA;
                break;
            case "SET_MUSIC_VOLUME":
                model = DataModel.SET_MUSIC_VOLUME;
                break;
            case "OPEN_APP":
                model = DataModel.OPEN_APP;
                break;
            case "REBOOT":
                model = DataModel.REBOOT;
                break;
            case "SHOW_IMG_WELCOME":
                model = DataModel.SHOW_IMG_WELCOME;
                break;
            case "SHOW_IMG_LIST":
                model = DataModel.SHOW_IMG_LIST;
                break;
            case "SHOW_IMGS_LIST":
                model = DataModel.SHOW_IMGS_LIST;
                break;
            case "SHOW_VIDEO_LIST":
                model = DataModel.SHOW_VIDEO_LIST;
                break;
            case "CLEAN_FILES":
                model = DataModel.CLEAN_FILES;
                break;
            case "CLOSE_APP":
                model = DataModel.CLOSE_APP;
                break;
            case "SHOW_DATE":
                model = DataModel.SHOW_DATE;
                break;
            case "GET_SUB_MODEL":
                model = DataModel.GET_SUB_MODEL;
                break;
            case "OPEN_LAST_ORDER":
                model = DataModel.OPEN_LAST_ORDER;
                break;
            case "VIDEOS":
                model = DataModel.VIDEOS;
                break;
            case "MENUVIDEOS":
                model = DataModel.MENUVIDEOS;
                break;
            case "GETVICECACHEFILESIZE":
                model = DataModel.GETVICECACHEFILESIZE;
                break;
            //DATA
            case "RESULT":
                model = DataModel.RESULT;
                break;
            case "NORMOL":
                model = DataModel.NORMOL;
                break;
            case "TEXT":
                model = DataModel.TEXT;
                break;
            case "TEXT_SINGLE":
                model = DataModel.TEXT_SINGLE;
                break;
            //File
            case "FILE":
                model = DataModel.FILE;
                break;
            default:
                model = null;
                break;
        }
        return model;
    }

    private DSData.DataType getDataType(String typeString){
        DSData.DataType type;
        switch(typeString){
            case "CHECK_CONN":
                type = DSData.DataType.CHECK_CONN;
                break;
            case "CHECK_FILE":
                type = DSData.DataType.CHECK_FILE;
                break;
            case "CMD":
                type = DSData.DataType.CMD;
                break;
            case "DATA":
                type = DSData.DataType.DATA;
                break;
            case "FILE":
                type = DSData.DataType.FILE;
                break;
            case "OK_CONN":
                type = DSData.DataType.OK_CONN;
                break;
            case "PRE_FILES":
                type = DSData.DataType.PRE_FILES;
                break;
            default:
                type = null;
                break;
        }
        return type;
    }

    private IConnectionCallback mIConnectionCallback = new IConnectionCallback() {
        @Override
        public void onDisConnect() {
            try {
                JSONObject message = new JSONObject();
                message.put("Callback", "Disconnection");
                message.put("Message", "DISCONECTED");
                PluginResult pr = new PluginResult(PluginResult.Status.OK, DSKernel.getDSDPackageName());
                pr.setKeepCallback(true);
                mCallback.sendPluginResult(pr);
            }catch (JSONException e){
                sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),mCallback);
            }
        }

        @Override
        public void onConnected(ConnState state) {
            try {
                JSONObject message = new JSONObject();
                message.put("Callback", "Connection");
                switch (state) {
                    case AIDL_CONN:
                        message.put("Message", "AIDL_CONN");
                        break;
                    case VICE_SERVICE_CONN:
                        message.put("Message", "VICE_SERVICE_CONN");
                        break;
                    case VICE_APP_CONN:
                        message.put("Message", "VICE_APP_CONN");
                        break;
                    default:
                        message.put("Message", "Not identified!");
                        break;
                }
                PluginResult pr = new PluginResult(PluginResult.Status.OK, DSKernel.getDSDPackageName());
                pr.setKeepCallback(true);
                mCallback.sendPluginResult(pr);
            }catch (JSONException e){
                sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),mCallback);
            }

        }
    };

    private IReceiveCallback mIReceiveCallback = new IReceiveCallback() {
        @Override
        public void onReceiveData(DSData data) {
            JSONObject message = new JSONObject();
            try{
                message.put("Callback","OnReceiveData");
                message.put("Payload",dsDataToJson(data));
                PluginResult pr = new PluginResult(PluginResult.Status.OK,DSKernel.getDSDPackageName());
                pr.setKeepCallback(true);
                mCallback.sendPluginResult(pr);
            }catch(JSONException e){
                sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),mCallback);
            }
        }

        @Override
        public void onReceiveFile(DSFile file) {
            JSONObject message = new JSONObject();
            try{
                message.put("Callback","OnReceiveFile");
                message.put("Payload",dsFileToJson(file));
                PluginResult pr = new PluginResult(PluginResult.Status.OK,DSKernel.getDSDPackageName());
                pr.setKeepCallback(true);
                mCallback.sendPluginResult(pr);
            }catch(JSONException e){
                sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),mCallback);
            }
        }

        @Override
        public void onReceiveFiles(DSFiles files) {
            JSONObject message = new JSONObject();
            try{
                message.put("Callback","OnReceiveFiles");
                message.put("Payload",dsFilesToJson(files));
                PluginResult pr = new PluginResult(PluginResult.Status.OK,DSKernel.getDSDPackageName());
                pr.setKeepCallback(true);
                mCallback.sendPluginResult(pr);
            }catch(JSONException e){
                sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),mCallback);
            }
        }

        @Override
        public void onReceiveCMD(DSData cmd) {
            JSONObject message = new JSONObject();
            try{
                message.put("Callback","OnReceiveCMD");
                message.put("Payload",dsDataToJson(cmd));
                PluginResult pr = new PluginResult(PluginResult.Status.OK,DSKernel.getDSDPackageName());
                pr.setKeepCallback(true);
                mCallback.sendPluginResult(pr);
            }catch(JSONException e){
                sendErrorMessage(11,"JSONException:"+e.getLocalizedMessage(),mCallback);
            }
        }
    };



    private void initSdk() {
        mDSKernel = DSKernel.newInstance();
        mDSKernel.init(cordova.getActivity().getApplicationContext(), mIConnectionCallback);
        mDSKernel.addReceiveCallback(mIReceiveCallback);
        mDSKernel.addConnCallback(mIConnectionCallback);
    }

    private void sendFile(String packagename,String message,String filePath,final CallbackContext callback){
        mDSKernel.sendFile(packagename, message, filePath, new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {
                PluginResult pr = new PluginResult(PluginResult.Status.OK,taskId);
                pr.setKeepCallback(false);
                callback.sendPluginResult(pr);
            }

            @Override
            public void onSendFail(int errorId, String errorInfo) {
                sendErrorMessage(errorId,errorInfo,callback);
            }

            @Override
            public void onSendProcess(long total, long sent) {
                //noresponse
            }
        });
    }
    private void sendFiles(String packagename,String message,JSONArray filePaths,final CallbackContext callback){
        List<String> files = new ArrayList<>();
        for (int i=0;i<filePaths.length();i++){
            files.add(filePaths.optString(i));
        }
        mDSKernel.sendFiles(packagename, message, files, new ISendFilesCallback() {
            @Override
            public void onAllSendSuccess(long fileId) {
                PluginResult pr = new PluginResult(PluginResult.Status.OK,fileId);
                pr.setKeepCallback(false);
                callback.sendPluginResult(pr);
            }
            @Override
            public void onSendSuccess(String path, long taskId) {

            }

            @Override
            public void onSendFaile(int errorId, String errorInfo) {
                sendErrorMessage(errorId,errorInfo,callback);
            }

            @Override
            public void onSendFileFaile(String path, int errorId, String errorInfo) {
                sendErrorMessage(errorId,errorInfo,callback);
            }

            @Override
            public void onSendProcess(String path,long total, long sent) {
                //noresponse
            }
        });
    }

    private void sendCMD(String packname,String message,long id,final CallbackContext callback){
        mDSKernel.sendCMD(packname, message, id,  new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {
                PluginResult pr = new PluginResult(PluginResult.Status.OK,taskId);
                pr.setKeepCallback(false);
                callback.sendPluginResult(pr);
            }

            @Override
            public void onSendFail(int errorId, String errorInfo) {
                sendErrorMessage(errorId,errorInfo,callback);
            }

            @Override
            public void onSendProcess(long totle, long sended) {

            }
        });
    }

    private void sendQuery(String type,String packageName,String data,Boolean isReport,long fileId,final CallbackContext callback){
        DSData.DataType dataType = getDataType(type);
        if(dataType == null){
            sendErrorMessage(9,"DataType could not be identified!",callback);
            return;
        }
        DataPacket packet = new DataPacket.Builder(dataType).data(data).recPackName(packageName)
                .addCallback(new ISendCallback() {
                    @Override
                    public void onSendSuccess(long taskId) {

                    }

                    @Override
                    public void onSendFail(int errorId, String errorInfo) {
                        sendErrorMessage(errorId,errorInfo,callback);
                    }

                    @Override
                    public void onSendProcess(long totle, long sended) {

                    }
                }).isReport(isReport).build();
        if(fileId != -1){
            packet.getData().fileId = fileId;
        }
        mDSKernel.sendQuery(packet, new QueryCallback() {
            @Override
            public void onReceiveData(DSData data) {
                JSONObject result = dsDataToJson(data);
                PluginResult pr = new PluginResult(PluginResult.Status.OK,result);
                pr.setKeepCallback(false);
                callback.sendPluginResult(pr);
            }
        });
    }

    private JSONObject dsFileToJson(DSFile file){
        try{
            JSONObject result = new JSONObject();
            result.put("taskId",file.taskId);
            result.put("path",file.path);
            result.put("sender",file.sender);
            return result;
        }catch(JSONException e){
            return null;
        }
    }

    private JSONObject dsFilesToJson(DSFiles files){
        try{
            JSONObject result = new JSONObject();
            result.put("taskId",files.taskId);
            result.put("describe",files.filesDescribe);
            result.put("sender",files.sender);
            JSONArray filesJson = new JSONArray();
            for (DSFile file:files.files) {
                filesJson.put(dsFileToJson(file));
            }
            result.put("files",filesJson);
            return result;
        }catch(JSONException e){
            return null;
        }
    }

    private JSONObject dsDataToJson(DSData data){
        try{
            JSONObject result = new JSONObject();
            result.put("data",data.data);
            result.put("fileId",data.fileId);
            result.put("taskId",data.taskId);
            result.put("queryId",data.queryId);
            result.put("sender",data.sender);
            result.put("dataType",dataTypeToString(data.dataType));
            return result;
        }catch(JSONException e){
            return null;
        }
    }

    private String dataTypeToString(DSData.DataType type){
        switch (type){
            case CMD:
                return "CMD";
            case DATA:
                return "DATA";
            case FILE:
                return "FILE";
            case OK_CONN:
                return "OK_CONN";
            case PRE_FILES:
                return "PRE_FILES";
            case CHECK_CONN:
                return "CHECK_CONN";
            case CHECK_FILE:
                return "CHECK_FILE";
            default:
                return null;
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        Log.d(TAG, "onResume: ------------>" + (mDSKernel == null));
        if (mDSKernel != null) {
            mDSKernel.checkConnection();
        } else {
            initSdk();
        }
    }
    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause: ------------>" + (mDSKernel == null));
        super.onPause(multitasking);
        mDSKernel.removeReceiveCallback(mIReceiveCallback);
        mDSKernel.onDestroy();
        mDSKernel = null;
    }
}
