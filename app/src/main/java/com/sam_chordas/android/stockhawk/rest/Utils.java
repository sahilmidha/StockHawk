package com.sam_chordas.android.stockhawk.rest;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }



  public static boolean isNetworkAvailable(Context c){
    ConnectivityManager cm =
            (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting();
  }

  /**
   *
   * @param c Context used to get the SharedPreferences
   * @return the stock status integer type
   */
  @SuppressWarnings("ResourceType")
  static public @StockTaskService.StockStatus
  int getStockStatus(Context c){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    return sp.getInt(c.getString(R.string.stock_status_key), StockTaskService.STOCK_STATUS_UNKNOWN);
  }

  /**
   * This runs on UI thread only. Note spe.apply() works for main thread.
   * Resets the stock status.  (Sets it to StockTaskService.STOCK_STATUS_UNKNOWN)
   * @param c Context used to get the SharedPreferences
   */
  static public void resetStockStatus(Context c){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    SharedPreferences.Editor spe = sp.edit();
    spe.putInt(c.getString(R.string.stock_status_key), StockTaskService.STOCK_STATUS_UNKNOWN);
    spe.apply();
  }

  public static String convertDate(String inputDate){
    StringBuilder outputFormattedDate = new StringBuilder();
    outputFormattedDate.append(inputDate.substring(6))
            .append("/")
            .append(inputDate.substring(4,6))
            .append("/")
            .append(inputDate.substring(2, 4));
    return outputFormattedDate.toString();
  }

}
