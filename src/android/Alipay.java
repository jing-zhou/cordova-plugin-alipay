package org.apache.cordova.ali;

import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Object;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Alipay extends CordovaPlugin {
    private static final String RESULT_STATUS = "resultStatus";
    private static final String RESULT = "result";
    private static final String MEMO = "memo";
    private static final String TAG = "org.apache.cordova.ali.Alipay";
    private static final String Execute = "Execute: ";
    private static final String with = " with: ";
    private static final String unsupported_param = "Unsported parameter:";
    private static final String callAli = "Calling Alipay with: ";
    private static final String aliReturn = "Alipay returns:";
    private static final String maniJson = "Manipulating json";
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, Execute + action + with + args.toString());
        boolean res = true;
        Object obj = args.get(0);
        if (obj != null && obj instanceof String){
            doCallPayment(callbackContext, (String)obj);
        }else{
            res = false;
            callbackContext.error(unsupported_param + obj);
            }
                  
        return res;
    }

    private void doCallPayment(final CallbackContext callbackContext, final String parameters) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, callAli + parameters);
                    PayTask task = new PayTask(cordova.getActivity());
                    // 调用支付接口，获取支付结果
                    Map<String, String> rawResult = task.payV2(parameters, true);
                    Log.d(TAG, aliReturn + rawResult.toString());
                    final JSONObject result = buildPaymentResult(rawResult);
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callbackContext.success(result);
                        }
                    });
                } catch (JSONException e){
                    Log.e(TAG, maniJson, e);
                    callbackContext.error(maniJson);
                }
            }

        });
    }

    private JSONObject buildPaymentResult(Map<String, String> rawResult) throws JSONException {
        JSONObject result = new JSONObject();
        if (rawResult == null) {
            return result;
        }

        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, RESULT_STATUS)) {
                result.put(RESULT_STATUS, rawResult.get(key));
            } else if (TextUtils.equals(key, RESULT)) {
                result.put(RESULT, rawResult.get(key));
            } else if (TextUtils.equals(key, MEMO)) {
                result.put(MEMO, rawResult.get(key));
            }
        }

        return result;
    }
}
