package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailFragment;

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
            QuoteColumns.CURRENCY,
            QuoteColumns.MARKETCAPITALIZATION,
            QuoteColumns.YEARHIGH,
            QuoteColumns.YEARLOW,
            QuoteColumns.DAYHIGH,
            QuoteColumns.DAYLOW,
            QuoteColumns.LASTTRADEDATE,
            QuoteColumns.EARNINGSSHARE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE
    };
    // these indices must match the projection
    private static final int INDEX_QUOTE_ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_NAME = 2;
    private static final int INDEX_CURRENCY = 3;
    private static final int INDEX_MARKET_CAP = 4;
    private static final int INDEX_YEAR_HIGH = 5;
    private static final int INDEX_YEAR_LOW = 6;
    private static final int INDEX_DAY_HIGH = 7;
    private static final int INDEX_DAY_LOW = 8;
    private static final int INDEX_LAST_TRADE_DATE= 9;
    private static final int INDEX_EARNINGS_SHARE= 10;
    private static final int INDEX_BID_PRICE = 11;
    private static final int INDEX_PERCENT_CHANGE = 12;
    private static final int INDEX_CHANGE = 13;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;
            //This is called first
            @Override
            public void onCreate() {
                // Nothing to do
            }
            // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
            // on the collection view corresponding to this factory. You can do heaving lifting in
            // here, synchronously. For example, if you need to process an image, fetch something
            // from the network, etc., it is ok to do it here, synchronously. The widget will remain
            // in its current state while work is being done here, so you don't need to worry about
            // locking up the widget.
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
            // You can do heaving lifting in here, synchronously. For example, if you need to
            // process an image, fetch something from the network, etc., it is ok to do it here,
            // synchronously. A loading view will show up in lieu of the actual contents in the
            // interim.
            @Override
            public RemoteViews getViewAt(int position) {
                // position will always range from 0 to getCount() - 1.

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                // We construct a remote views item based on our widget item xml file, and set the
                // views data based on the position. (Note that we have moved cursor to that position)
                RemoteViews remoteViews = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                int quoteId = data.getInt(INDEX_QUOTE_ID);
                String symbol = data.getString(INDEX_SYMBOL);
                String bidPrice = data.getString(INDEX_BID_PRICE);
                String change = data.getString(INDEX_CHANGE);

                //set Data to the views
                remoteViews.setTextViewText(R.id.stock_symbol_widget, symbol);
                remoteViews.setTextViewText(R.id.bid_price_widget, bidPrice);
                remoteViews.setTextViewText(R.id.change_widget, change);

                Bundle extras = new Bundle();
                extras.putString(StockDetailFragment.SYMBOL_CLICKED,data.getString(INDEX_SYMBOL));
                extras.putString(StockDetailFragment.NAME,data.getString(INDEX_NAME));
                extras.putString(StockDetailFragment.CURRENCY,data.getString(INDEX_CURRENCY));
                extras.putString(StockDetailFragment.MARKET_CAP,data.getString(INDEX_MARKET_CAP));
                extras.putString(StockDetailFragment.YEAR_HIGH,data.getString(INDEX_YEAR_HIGH));
                extras.putString(StockDetailFragment.YEAR_LOW,data.getString(INDEX_YEAR_LOW));
                extras.putString(StockDetailFragment.DAY_HIGH,data.getString(INDEX_DAY_HIGH));
                extras.putString(StockDetailFragment.DAY_LOW,data.getString(INDEX_DAY_LOW));
                extras.putString(StockDetailFragment.LAST_TRADE_DATE,data.getString(INDEX_LAST_TRADE_DATE));
                extras.putString(StockDetailFragment.EARNINGS_SHARE,data.getString(INDEX_EARNINGS_SHARE));
                extras.putString(StockDetailFragment.BID_PRICE,data.getString(INDEX_BID_PRICE));
                extras.putString(StockDetailFragment.PERCENT_CHANGE,data.getString(INDEX_PERCENT_CHANGE));
                extras.putString(StockDetailFragment.CHANGE,data.getString(INDEX_CHANGE));

                // Next, we set a fill-intent which will be used to fill-in the pending intent template
                // which is set on the collection view in DetailWidgetProvider class.
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                // Note that we need to update the intent's data if we set an extra, since the extras will be
                // ignored otherwise.
                fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return remoteViews;
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
