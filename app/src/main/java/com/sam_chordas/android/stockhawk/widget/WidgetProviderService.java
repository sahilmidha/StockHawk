package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by sahilmidha on 07/08/16.
 */
public class WidgetProviderService extends IntentService
{
    private String LOG_TAG = WidgetProviderService.class.getSimpleName();

    private static final String[] QUOTE_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.NAME,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE
    };
    // these indices must match the projection
    private static final int INDEX_QUOTE_ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_NAME = 2;
    private static final int INDEX__BIDPRICE = 3;
    private static final int INDEX_CHANGE = 4;

    public WidgetProviderService() { super("WidgetProviderService"); }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(LOG_TAG, "onHandleIntent Launched");
        // Retrieve all of the widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));

        // Get stock's data from the ContentProvider
        Cursor data = getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                QUOTE_COLUMNS,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the data from the Cursor
        int quoteId = data.getInt(INDEX_QUOTE_ID);
        String symbol = data.getString(INDEX_SYMBOL);
        String name = data.getString(INDEX_NAME);
        String bidPrice = data.getString(INDEX__BIDPRICE);
        String change = data.getString(INDEX_CHANGE);
        //finally close the cursor
        data.close();


        for (int appWidgetId : appWidgetIds)
        {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget;
            } else {
                layoutId = R.layout.widget_small;
            }

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            //set Data to the views
            views.setTextViewText(R.id.stock_symbol_widget, symbol);
            views.setTextViewText(R.id.stock_name_widget, name);
            views.setTextViewText(R.id.bid_price_widget, bidPrice);
            views.setTextViewText(R.id.change_widget, change);

            // Create an Intent to launch Activity
            Intent launchIntent = new Intent(this, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.basic_layout, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget_info
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_default_width);
    }
}
