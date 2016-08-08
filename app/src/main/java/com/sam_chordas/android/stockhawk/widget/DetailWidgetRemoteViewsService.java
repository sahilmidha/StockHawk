package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.concurrent.ExecutionException;

/**
 * Created by sahilmidha on 09/08/16.
 * RemoteViewsService that controls the data being shown in the scrollable weather detail widget
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService
{
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

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

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        QUOTE_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail);

                int quoteId = data.getInt(INDEX_QUOTE_ID);
                String symbol = data.getString(INDEX_SYMBOL);
                String name = data.getString(INDEX_NAME);
                String bidPrice = data.getString(INDEX__BIDPRICE);
                String change = data.getString(INDEX_CHANGE);

                //set Data to the views
                views.setTextViewText(R.id.stock_symbol_widget, symbol);
                views.setTextViewText(R.id.stock_name_widget, name);
                views.setTextViewText(R.id.bid_price_widget, bidPrice);
                views.setTextViewText(R.id.change_widget, change);

                final Intent fillInIntent = new Intent();
                Uri uri = QuoteProvider.QuotesHistory.withSymbol(symbol);
                fillInIntent.setData(uri);
                views.setOnClickFillInIntent(R.id.basic_detail_layout, fillInIntent);
                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTE_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
