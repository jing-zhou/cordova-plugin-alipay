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
    public static final String RESULT_STATUS = "resultStatus";
    public static final String RESULT = "result";
    public static final String MEMO = "memo";
    private static String TAG = "cordova-alipay-base";


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "Execute:" + action + " with :" + args.toString());
        if (action.equals("pay")) {
            String payParameters = null;
            if (args.get(0) instanceof JSONObject){
                JSONObject obj = args.getJSONObject(0);
                payParameters = buildCallString(obj, callbackContext);
            }else if (args.get(0) instanceof String){
                payParameters = (String) args.get(0);
            }else{
                callbackContext.error("Unsported parameter:" + args.get(0));
                return true;
            }
            doCallPayment(callbackContext, payParameters);
        }else{
            callbackContext.error("Known service: " + action);
        }
        return true;
    }

    private void doCallPayment(final CallbackContext callbackContext, final String parameters) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Calling Alipay with: " + parameters);
                    PayTask task = new PayTask(cordova.getActivity());
                    // 调用支付接口，获取支付结果
                    Map<String, String> rawResult = task.payV2(parameters, true);
                    Log.d(TAG, "Alipay returns:" + rawResult.toString());
                    final JSONObject result = buildPaymentResult(rawResult);
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callbackContext.success(result);
                        }
                    });
                }
                catch (JSONException e){
                    Log.e(TAG, "Manipulating json", e);
                    callbackContext.error("Manipulating json");
                }
            }

        });
    }

    private String buildCallString(JSONObject args, CallbackContext context) throws JSONException {
        StringBuffer buf = new StringBuffer();
        List<String> keys = new ArrayList<String>();
        Iterator<String> itr = args.keys();
        while (itr.hasNext()) {
            String key = itr.next();
            if (TextUtils.isEmpty(key)) continue;;
            if ("sign".equals(key)) continue;;
            keys.add(key);
        }

        //Let's sort the order info and attach sign to the end
        Collections.sort(keys);
        keys.add("sign");

        for (String key : keys){
            String value = args.getString(key);
            if (TextUtils.isEmpty(value)){
                Log.w(TAG, "Empty value for: " + key);
                continue;
            }
            buf.append(key).append('=');
            buf.append(value);
            buf.append('&');
        }
        if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }

    private JSONObject buildPaymentResult(Map<String, String> rawResult) throws JSONException {
        JSONObject result = new JSONObject();
        if (rawResult == null) {
            return result;
        }

        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, "resultStatus")) {
                result.put(RESULT_STATUS, rawResult.get(key));
            } else if (TextUtils.equals(key, "result")) {
                result.put(RESULT, rawResult.get(key));
            } else if (TextUtils.equals(key, "memo")) {
                result.put(MEMO, rawResult.get(key));
            }
        }

        return result;
    }
}
