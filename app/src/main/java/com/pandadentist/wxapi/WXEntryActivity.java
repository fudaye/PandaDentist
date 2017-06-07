package com.pandadentist.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.pandadentist.config.Constants;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = WXEntryActivity.class.getSimpleName();

    IWXAPI api;
    private static final String APP_ID = "wxa2fe13a5495f3908";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TextView(this));
        api = WXAPIFactory.createWXAPI(this,APP_ID);

        try {
            api.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Log.d("req","req"+req.toString());
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        SendAuth.Resp r = (SendAuth.Resp) resp;
        Log.d(TAG,"errorCode-->"+r.errCode);
        Log.d(TAG,"code-->"+r.code);
        LocalBroadcastManager lb = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Constants.BROADCAST_FLAG_CODE_MESSAGE);
        intent.putExtra(Constants.BUNDLE_KEY.VALUE,r.code);
        lb.sendBroadcast(intent);
        finish();
//        switch (resp.errCode) {
//            case BaseResp.ErrCode.ERR_OK:
//                result = R.string.errcode_success;
//                break;
//            case BaseResp.ErrCode.ERR_USER_CANCEL:
//                result = R.string.errcode_cancel;
//                break;
//            case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                result = R.string.errcode_deny;
//                break;
//            case BaseResp.ErrCode.ERR_UNSUPPORT:
//                result = R.string.errcode_unsupported;
//                break;
//            default:
//                result = R.string.errcode_unknown;
//                break;
//        }

//        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

}