package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

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
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE
    };
    // these indices must match the projection
    private static final int INDEX_QUOTE_ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX__BIDPRICE = 2;
    private static final int INDEX_CHANGE = 3;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;
            //This is called first
            @Override
            public void onCreate() {
                // Nothing to do
            }
            //Then onDataSetChanged() is called at second place.
            // We also call notifyAppWidgetViewDataChanged() via appWidgetManager to call below
            // onDataSetChanged() to requery data.
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
            //Then we prepare meta data for further execution. Below 4 methods are part of fetching metadata
            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            //Then finally we fetch view data in below 2 methods.

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                int quoteId = data.getInt(INDEX_QUOTE_ID);
                String symbol = data.getString(INDEX_SYMBOL);
                String bidPrice = data.getString(INDEX__BIDPRICE);
                String change = data.getString(INDEX_CHANGE);

                //set Data to the views
                views.setTextViewText(R.id.stock_symbol_widget, symbol);
                views.setTextViewText(R.id.bid_price_widget, bidPrice);
                views.setTextViewText(R.id.change_widget, change);

                final Intent fillInIntent = new Intent();
                Uri uri = QuoteProvider.QuotesHistory.withSymbol(symbol);
                fillInIntent.setData(uri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTE_ID);
                return position;
            }
            //This is called in the last.
            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }
        };
    }
}
