package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService
{
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean mIsUpdate;
    private boolean mUpdateCall;

    private List<String> mSymbolList = new ArrayList<String>();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STOCK_STATUS_OK, STOCK_STATUS_SERVER_DOWN, STOCK_STATUS_SERVER_INVALID, STOCK_STATUS_UNKNOWN, STOCK_STATUS_INVALID})
    public @interface StockStatus
    {
    }

    public static final int STOCK_STATUS_OK = 0;
    public static final int STOCK_STATUS_SERVER_DOWN = 1;
    public static final int STOCK_STATUS_SERVER_INVALID = 2;
    public static final int STOCK_STATUS_UNKNOWN = 3;
    public static final int STOCK_STATUS_INVALID = 4;

    public StockTaskService()
    {
    }

    public StockTaskService(Context context)
    {
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams params)
    {
        Cursor initQueryCursor;
        if (mContext == null)
        {
            mContext = this;
        }
        if(params.getTag().equals("periodic")){
            //make an update call
            mUpdateCall = true;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try
        {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            //urlStringBuilder.append("http://google.com?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        if (params.getTag().equals("init") || params.getTag().equals("periodic"))
        {
            mIsUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
            if (initQueryCursor.getCount() == 0 || initQueryCursor == null)
            {
                // Init task. Populates DB with quotes for the symbols seen below
                try
                {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
            else if (initQueryCursor != null)
            {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++)
                {
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try
                {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (params.getTag().equals("add"))
        {
            mIsUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            try
            {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        ArrayList arrayList;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null)
        {
            urlString = urlStringBuilder.toString();
            try
            {
                Object object = fetchData(urlString);
                if (object instanceof String)
                {
                    getResponse = (String) object;
                    arrayList = quoteJsonToContentVals(getResponse);
                    if (null != arrayList
                            && arrayList.size() != 0)
                    {
                        result = GcmNetworkManager.RESULT_SUCCESS;
                        try
                        {
                            ContentValues contentValues = new ContentValues();
                            // update ISCURRENT to 0 (false) so new data is current
                            if (mIsUpdate)
                            {
                                contentValues.put(QuoteColumns.ISCURRENT, 0);
                                mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                        null, null);
                                contentValues.put(QuoteHistoryColumns.ISCURRENT, 0);
                                mContext.getContentResolver().update(QuoteProvider.QuotesHistory.CONTENT_URI, contentValues,
                                        null, null);
                            }
                            mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                    arrayList);
                            setStockStatus(mContext, STOCK_STATUS_OK);
                        }
                        catch (RemoteException | OperationApplicationException e)
                        {
                            Log.e(LOG_TAG, "Error applying batch insert", e);
                            e.printStackTrace();

                        }
                    }
                }
                //Save Quotes History
                saveQuotesHistory(mSymbolList);
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
                ;
                // If the code didn't successfully get the stock data, there's no point in attempting
                // to parse it.
                setStockStatus(mContext, STOCK_STATUS_SERVER_DOWN);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, "String to JSON failed: " + e);
                e.printStackTrace();
                setStockStatus(mContext, STOCK_STATUS_SERVER_INVALID);
            }
        }

        return result;
    }

    public Object fetchData(String url) throws IOException
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
        {
            // Stream was empty.  No point in parsing.
            setStockStatus(mContext, STOCK_STATUS_SERVER_INVALID);
            return STOCK_STATUS_SERVER_DOWN;
        }
        else
        {
            return response.body().string();
        }
    }

    public ArrayList quoteJsonToContentVals(String JSON) throws JSONException
    {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;

        jsonObject = new JSONObject(JSON);
        if (jsonObject != null && jsonObject.length() != 0)
        {
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1)
            {
                jsonObject = jsonObject.getJSONObject("results")
                        .getJSONObject("quote");
                String name = jsonObject.getString("Name");
                if (null != name
                        && !name.equalsIgnoreCase("null"))
                {
                    //preparing List of stocks that would go in DB
                    mSymbolList.add(jsonObject.getString("symbol"));
                    batchOperations.add(buildBatchOperation(jsonObject));
                }
                else
                {
                    setStockStatus(mContext, STOCK_STATUS_INVALID);
                }
            }
            else
            {
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0)
                {
                    for (int i = 0; i < resultsArray.length(); i++)
                    {
                        jsonObject = resultsArray.getJSONObject(i);
                        //preparing List of stocks that would go in DB
                        mSymbolList.add(jsonObject.getString("symbol"));
                        batchOperations.add(buildBatchOperation(jsonObject));
                    }
                }
            }
        }

        return batchOperations;
    }

    public ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws JSONException
    {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);

        String change = jsonObject.getString("Change");
        builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
        builder.withValue(QuoteColumns.BIDPRICE, Utils.truncateBidPrice(jsonObject.getString("Bid")));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, Utils.truncateChange(
                jsonObject.getString("ChangeinPercent"), true));
        builder.withValue(QuoteColumns.CHANGE, Utils.truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-')
        {
            builder.withValue(QuoteColumns.ISUP, 0);
        }
        else
        {
            builder.withValue(QuoteColumns.ISUP, 1);
        }
        builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
        builder.withValue(QuoteColumns.CURRENCY, jsonObject.getString("Currency"));
        builder.withValue(QuoteColumns.LASTTRADEDATE, jsonObject.getString("LastTradeDate"));
        builder.withValue(QuoteColumns.DAYLOW, jsonObject.getString("DaysLow"));
        builder.withValue(QuoteColumns.DAYHIGH, jsonObject.getString("DaysHigh"));
        builder.withValue(QuoteColumns.YEARLOW, jsonObject.getString("YearLow"));
        builder.withValue(QuoteColumns.YEARHIGH, jsonObject.getString("YearHigh"));
        builder.withValue(QuoteColumns.EARNINGSSHARE, jsonObject.getString("EarningsShare"));
        builder.withValue(QuoteColumns.MARKETCAPITALIZATION, jsonObject.getString("MarketCapitalization"));

        return builder.build();
    }

    /**
     * Sets the stock status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     *
     * @param c           Context to get the PreferenceManager from.
     * @param stockStatus The IntDef value to set
     */
    static private void setStockStatus(Context c, @StockStatus int stockStatus)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.stock_status_key), stockStatus);
        spe.commit();
    }

    private void saveQuotesHistory(List<String> quotes) throws IOException, JSONException
    {

        for (String quote : quotes)
        {
            // Load historical data for the quote
            fetchHistoricalData(quote);
        }
    }

    private void fetchHistoricalData(String symbol) throws IOException, JSONException
    {

        ArrayList arrayList;
        // Load historic stock data
        final String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
        final String END_URL = "/chartdata;type=quote;range=1y/json";

        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append(BASE_URL + symbol + END_URL);

        String responseStr = executeNetworkRequest(new URL(urlStringBuilder.toString()));
        /*if (response != 200)
        {
            // Stream was empty.  No point in parsing.
            setStockStatus(mContext, STOCK_STATUS_SERVER_INVALID);
        }
        else
        {*/
            //to parse the json data..
            String JSON_SERIES = "series";
            String JSON_DATE = "Date";
            String JSON_CLOSE = "close";

            ContentValues contentValues = new ContentValues();
            /**
             * The retrieved data is in JSONP format.
             * so we need to strip off the outer function,
             * and then parse the json data inside that.
             */
            String json = responseStr.substring(responseStr.indexOf("(") + 1, responseStr.lastIndexOf(")"));
            JSONObject mainObject = new JSONObject(json);
            JSONArray series_data = mainObject.getJSONArray(JSON_SERIES);
            for (int i = 0; i < series_data.length(); i += 10)
            {
                JSONObject singleObject = series_data.getJSONObject(i);
                contentValues.put(QuoteHistoryColumns.DATE, singleObject.getString(JSON_DATE));
                contentValues.put(QuoteHistoryColumns.LAST_CLOSING_PRICE, singleObject.getString(JSON_CLOSE));
                contentValues.put(QuoteHistoryColumns.ISCURRENT,1);
                if (mUpdateCall)
                {//this is a periodic update
                    mContext.getContentResolver().update(QuoteProvider.QuotesHistory.CONTENT_URI, contentValues,
                            QuoteHistoryColumns.SYMBOL, new String[]{symbol});
                }
                else
                {//this is adding new symbol (either on add or on initialisation)
                    contentValues.put(QuoteHistoryColumns.SYMBOL, symbol);
                    mContext.getContentResolver().insert(QuoteProvider.QuotesHistory.CONTENT_URI, contentValues);
                }
            //}
        }
    }

    private String executeNetworkRequest(URL url)
    {
        final int REQUEST_READ_TIME_OUT = 10000;
        final int REQUEST_CONNECTION_TIME_OUT = 15000;

        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(REQUEST_READ_TIME_OUT);
            connection.setConnectTimeout(REQUEST_CONNECTION_TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //connection.setRequestProperty("Accept", "application/json");

            connection.setUseCaches(false);
            //connection.setDoInput(false);
            //connection.setDoOutput(true);
            Log.v("DataFetcher", url.toString());
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK)
            {
                throw new Exception();
            }

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null)
            {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;// we can create custom classes to handle multiple type of scenerios.
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }
}
