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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Alipay extends CordovaPlugin {
    private static final String RESULT_STATUS = "resultStatus";
    private static final String RESULT = "result";
    private static final String MEMO = "memo";
    private static final String TAG = "cordova-alipay";
    private static final String Execute = "Execute: ";
    private static final String with = " with: ";
    private static final String pay = "pay";
    private static final String unsupported_param = "Unsported parameter:";
    private static final String unknown_action = "unknown action: ";
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
        if (action.equals(pay)) {
            String payParameters = null;
            if (args.get(0) instanceof String){
                payParameters = (String) args.get(0);
            }else{
                callbackContext.error(unsupported_param + args.get(0));
                return true;
            }
            doCallPayment(callbackContext, payParameters);
        }else{
            callbackContext.error(unknown_action + action);
        }
        return true;
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
                }
                catch (JSONException e){
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
