package com.myapartment1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.myapartment1.api.Api;
import com.myapartment1.model.paytm.Checksum;
import com.myapartment1.model.paytm.Constants;
import com.myapartment1.model.paytm.Paytm;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.HashMap;
import java.util.Map;

public class MerchantActivity extends AppCompatActivity {

    Button buttonBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        findViewById(R.id.buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //calling the method generateCheckSum() which will generate the paytm checksum for payment

                String chksum = "X2bpLEZB39dspQVjHK7UY6EI0wcvCYaxdGpaxTmVNrTnZEvY57L4FUemF6n9iwzJSxz581uke0gUFZjTouXnJK5eQh7IMuXu4W5+XKl941Q=";
                final Paytm paytm = new Paytm(
                        Constants.M_ID,
                        Constants.CHANNEL_ID,
                        "100",
                        Constants.WEBSITE,
                        Constants.CALLBACK_URL,
                        Constants.INDUSTRY_TYPE_ID
                );
                initializePaytmPayment(chksum, paytm);
            }
        });
    }

    private void generateCheckSum() {

        //getting the tax amount first.
        //  String txnAmount = textViewPrice.getText().toString().trim();

        //creating a retrofit object.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //creating the retrofit api service
        Api apiService = retrofit.create(Api.class);

        //creating paytm object
        //containing all the values required
        final Paytm paytm = new Paytm(
                Constants.M_ID,
                Constants.CHANNEL_ID,
                "100",
                Constants.WEBSITE,
                Constants.CALLBACK_URL,
                Constants.INDUSTRY_TYPE_ID
        );

        //creating a call object from the apiService
        Call<Checksum> call = apiService.getChecksum(paytm);


        //making the call to generate checksum
        call.enqueue(new Callback<Checksum>() {
            @Override
            public void onResponse(Call<Checksum> call, Response<Checksum> response) {

                //once we get the checksum we will initiailize the payment.
                //the method is taking the checksum we got and the paytm object as the parameter
                Log.i("checksum", response.body().getChecksumHash());
                Log.i("response", response.toString());
                initializePaytmPayment("X2bpLEZB39dspQVjHK7UY6EI0wcvCYaxdGpaxTmVNrTnZEvY57L4FUemF6n9iwzJSxz581uke0gUFZjTouXnJK5eQh7IMuXu4W5+XKl941Q=", paytm);
            }

            @Override
            public void onFailure(Call<Checksum> call, Throwable t) {
                Log.i("fail", "Here in Failure");
                System.out.println(t);

            }
        });
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {

        //getting paytm service
        PaytmPGService Service = PaytmPGService.getStagingService();

        //use this when using for production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //creating a hashmap and adding all the values required
        Map<String, String> paramMap = new HashMap<>();


        paramMap.put("MID", Constants.M_ID);
        paramMap.put("ORDER_ID", paytm.getOrderId());
        paramMap.put("CUST_ID", paytm.getCustId());
        paramMap.put("CHANNEL_ID", paytm.getChannelId());
        paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
        paramMap.put("WEBSITE", paytm.getWebsite());
        paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());


        //creating a paytm order object using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //intializing the paytm service
        Service.initialize(order, null);

        //finally starting the payment transaction
        //.startPaymentTransaction(this, true, true, this);

        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {

            @Override
            public void onTransactionResponse(Bundle bundle) {
                Log.d("LOG", "Payment Transaction is successful " + bundle);
                Toast.makeText(getApplicationContext(), "Payment Transaction response " + bundle.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void networkNotAvailable() {

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
                Toast.makeText(MerchantActivity.this, "Back pressed. Transaction cancelled", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {
                Log.d("LOG", "Payment Transaction Failed " + s);
                Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();

            }
        });

    }
}




