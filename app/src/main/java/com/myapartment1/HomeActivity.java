package com.myapartment1;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.myapartment1.model.DataExpenses;
import com.myapartment1.model.DataIncome;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    GoogleAccountCredential mCredential;

    ProgressDialog mProgress;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private Toolbar toolbar;
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    String spreadsheetId = "1YgC4Do_-PEQbbuFEQ6o7WrIba1Gh-PSjWMXz8tSgYeo";
    String type = "income";
    String range = "Income!B3:E";
    String month;


    AutoCompleteTextView actv;
    ArrayAdapter<String> adapter;


    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);
        String[] monthName = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November",
                "December"};

        //by Default selected month is current month
        // month = monthName[calendar.get(Calendar.MONTH)];


        adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, monthName);
        actv = findViewById(R.id.autoCompleteTextView);

        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.BLUE);

        actv.setVisibility(View.INVISIBLE);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                HomeActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Fetching Expenses Details...");

        if (id == R.id.exp) {
           

        }

        if (id == R.id.inc) {
            actv.setVisibility(View.VISIBLE);

            actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                        long id) {
                    Toast.makeText(HomeActivity.this, adapter.getItem(pos).toString(), Toast.LENGTH_LONG).show();
                    month = adapter.getItem(pos).toString();
                    Log.i("month", month);
                    getResults(spreadsheetId, range, type);


                }
            });


            //

        }


        if (id == R.id.pm) {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_LONG).show();
            Intent profileIntent = new Intent(this, MerchantActivity.class);
            startActivity(profileIntent);

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG);

                } else {
                    getResults(spreadsheetId, range, type);
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResults(spreadsheetId, range, type);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResults(spreadsheetId, range, type);
                }
                break;
        }
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResults(String spreadsheetId, String range, String type) {

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(spreadsheetId, range, type);
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG);
        } else {
            new MakeRequestTask(mCredential).execute(spreadsheetId, range, type);
        }
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount(String spreadsheetId, String range, String type) {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResults(spreadsheetId, range, type);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */


    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                HomeActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<String, Void, List> {

        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List doInBackground(String... params) {
            try {
                String spreadsheetid = params[0];
                String range = params[1];
                String type = params[2];

                return getDataFromApi(spreadsheetid, range, type);
            } catch (Exception e) {
                mLastError = e;
                Log.i("test", Log.getStackTraceString(e));
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1-8Ii-SN-XduUDbaTOeI1tIpDz-Vlnnhmyv_DfDyaq9g/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private List getDataFromApi(String spreadsheetid, String range, String type) throws Exception {

            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetid, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            List<DataExpenses> dataExpensesList = new ArrayList<>();
            List<DataIncome> incomeList = new ArrayList<>();


            //

            if (values != null) {
                if (type.equalsIgnoreCase("expenses")) {
                    for (List row : values) {
                        DataExpenses dataExpenses = new DataExpenses();
                        try {
                            dataExpenses.setDate((String) row.get(0));
                            dataExpenses.setParticulars((String) row.get(1));
                            dataExpenses.setAmount((String) row.get(2));
                            dataExpenses.setRemarks((String) row.get(4));
                            dataExpensesList.add(dataExpenses);
                        } catch (IndexOutOfBoundsException e) {
                            dataExpensesList.add(dataExpenses);
                        } finally {
                            return dataExpensesList;
                        }
                    }
                }
                if (type.equalsIgnoreCase("income")) {
                    Log.i("monthInTask", month);
                    for (List row : values) {
                        DataIncome dataIncome = new DataIncome();
                        if (row.size() <= 2 || row.isEmpty()) continue;
                        String dateFromExcel = row.get(2).toString();
                        if (dateFromExcel == null || dateFromExcel == "") {
                            dateFromExcel = "01/" + Utility.getMonth(month) + "/2001";
                        }

                        if (month.equalsIgnoreCase(Utility.getMonthName(dateFromExcel))) {
                            try {
                                dataIncome.setDate(dateFromExcel);
                                dataIncome.setAmount((String) row.get(3));
                                dataIncome.setFlat((String) row.get(0));
                                dataIncome.setArea((String) row.get(1));
                            } catch (Exception e) {
                                dataIncome.setAmount((String) row.get(3));
                                dataIncome.setFlat((String) row.get(0));
                                dataIncome.setArea((String) row.get(1));
                            }
                            incomeList.add(dataIncome);
                        }
                    }
                    return incomeList;
                }
            }
            return null;
        }


        @Override
        protected void onPreExecute() {

            mProgress.show();
        }

        @Override
        protected void onPostExecute(List output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(HomeActivity.this, "No results returned.", Toast.LENGTH_LONG);

            } else {

                // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                TableLayout tableLayout = findViewById(R.id.tablelayout);


                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);


                for (Object list : output) {
                    if (list instanceof DataIncome) {
                        TableRow tableRow = new TableRow(HomeActivity.this);
                        tableRow.setLayoutParams(layoutParams);
                        tableRow.setPadding(8, 0, 8, 40);

                        TextView dateValue = createValueView(((DataIncome) list).getDate());
                        TextView flatValue = createValueView(((DataIncome) list).getFlat());
                        TextView amountValue = createValueView(((DataIncome) list).getAmount());
                        TextView areaValue = createValueView(((DataIncome) list).getArea());


                        tableRow.addView(dateValue);
                        tableRow.addView(flatValue);
                        tableRow.addView(amountValue);
                        tableRow.addView(areaValue);
                        tableLayout.addView(tableRow);
                    }
                }
                TableRow tableRow = new TableRow(HomeActivity.this);
                tableRow.setLayoutParams(layoutParams);
                tableRow.setPadding(8, 0, 8, 40);

                tableLayout.addView(tableRow);
            }
        }

        private TextView createHeaderView(String text) {
            TextView header = new TextView(HomeActivity.this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            header.setTextColor(Color.BLUE);
            header.setTypeface(null, Typeface.BOLD_ITALIC);
            header.setGravity(Gravity.CENTER);
            header.setLayoutParams(layoutParams);
            header.setText(text);

            return header;

        }

        private TextView createValueView(String value) {
            TextView valueView = new TextView(HomeActivity.this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            valueView.setBackgroundColor(Color.WHITE);
            valueView.setGravity(Gravity.CENTER);
            valueView.setLayoutParams(layoutParams);
            valueView.setText(value);
            return valueView;
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            HomeActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(), mLastError.getMessage(), Toast.LENGTH_LONG);
                }
            } else {
                // mOutputText.setText("Request cancelled.");
            }
        }
    }
}