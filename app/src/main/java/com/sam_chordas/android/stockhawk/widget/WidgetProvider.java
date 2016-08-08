package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by sahilmidha on 07/08/16.
 */
public class WidgetProvider extends AppWidgetProvider
{
    private String LOG_TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.d(LOG_TAG, "onUpdate Started");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context, WidgetProviderService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOG_TAG, "onReceive Started");
        super.onReceive(context, intent);
        if (MyStocksActivity.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.d(LOG_TAG, "onReceive Started - now starting service");
            context.startService(new Intent(context, WidgetProviderService.class));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        Log.d(LOG_TAG, "onAppWidgetOptionsChanged Started");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        context.startService(new Intent(context, WidgetProviderService.class));
    }
}
