package com.hackdroid.paytmallinone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {
    String orderId = null, customerId = null;
    String PAYTM_MID = "paqCzh93121643068747";
    String PAYMT_KEY = "KeVLOii#rHGbi1_D";
    String AMT = String.valueOf(10.0), razorpay_Amount = null;
    String TAG = MainActivity.class.getSimpleName();
    String TOKEN_GEN = "http://192.168.1.9/paytm/token.php";
    String ORDER_GEN = "http://192.168.1.9/razorpay/neworder.php";
    String callbackurl = null;
    Button razorpay, paytm;
    String RZP_KEY_ID = "rzp_test_RGlzzSOd2HacmW";
    String RZP_KEY_TOKEN = "jJn1bH9qn5z3mu2Xu5FCar0U";
    Checkout checkout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        orderId = String.valueOf(SystemClock.uptimeMillis());
        Log.d(TAG, "onCreate: " + orderId);
        Log.d(TAG, "onCreate: " + AMT);
        Double d = Double.valueOf(AMT);
        d = d * 100;
        int integer = (int) Math.round(d);
        razorpay_Amount = String.valueOf(integer);

        customerId = "TEST_DELSTO_095";
        callbackurl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId;
        Log.d(TAG, "onCreate: " + callbackurl);
        checkout = new Checkout();
        checkout.setKeyID(RZP_KEY_ID);
        Checkout.preload(getApplicationContext());


//        generateTokenPaytm();


        paytm = findViewById(R.id.paytm);
        razorpay = findViewById(R.id.rzp);
        razorpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                razorpayInit();
            }
        });
        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateTokenPaytm();
            }
        });


    }

    private void razorpayInit() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ORDER_GEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                payWithRazorpay(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("order_id", orderId);
                map.put("amount", razorpay_Amount);
                return map;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

    private void payWithRazorpay(String response) {
        final Activity activity = this;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Delsto");
            jsonObject.put("description", "#Ref-" + orderId);
            jsonObject.put("order_id", response);
            jsonObject.put("currency", "INR");
            jsonObject.put("amount", razorpay_Amount);
            jsonObject.put("prefill.name", "Khalid");
            jsonObject.put("prefill.contact", "+919835555982");
            checkout.open(activity, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateTokenPaytm() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, TOKEN_GEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject body = jsonObject.getJSONObject("body");
//                    Log.d(TAG, "onResponse: "+body);
                    String TXN_TOKEN = body.getString("txnToken");
//                    Log.d(TAG, "onResponse: "+TXN_TOKEN);
                    createPaytmTrans(TXN_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("order_id", orderId);
                map.put("amt", AMT);
                map.put("cst", customerId);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void createPaytmTrans(String txn_token) {

        PaytmOrder paytmOrder = new PaytmOrder(orderId, PAYTM_MID, txn_token, AMT, callbackurl);

        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(Bundle bundle) {
                Log.d(TAG, "onTransactionResponse: " + bundle);
            }

            @Override
            public void networkNotAvailable() {
                Log.d(TAG, "networkNotAvailable: Internet issue ");

            }

            @Override
            public void clientAuthenticationFailed(String s) {

            }

            @Override
            public void someUIErrorOccurred(String s) {

            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {

            }

            @Override
            public void onBackPressedCancelTransaction() {

            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {

            }
        });
        transactionManager.startTransaction(MainActivity.this, 109);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 109:
                Log.d(TAG, "onActivityResult: " + data);
                Toast.makeText(this, data.getStringExtra("nativeSdkForMerchantMessage") + data.getStringExtra("response"), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onActivityResult: " + data.getStringExtra("nativeSdkForMerchantMessage"));
                Log.d(TAG, "onActivityResult: " + data.getStringExtra("response"));
                break;

        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.d(TAG, "onPaymentSuccess: " + s);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.d(TAG, "onPaymentError: " + s);

    }
}
