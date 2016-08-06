package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener, RecyclerViewItemClickListener.OnItemClickListener
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;
    private ProgressDialog mProgressdialog;
    private RecyclerView mRecyclerView;
    private TextView mTextViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = this;

        isConnected = Utils.isNetworkAvailable(mContext);
        setContentView(R.layout.activity_my_stocks);
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTextViewEmpty = (TextView) findViewById(R.id.empty_recycler_view);

        if (savedInstanceState == null)
        {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected)
            {
                mRecyclerView.setVisibility(View.VISIBLE);
                mTextViewEmpty.setVisibility(View.GONE);
                startService(mServiceIntent);
            }
            else
            {
                mRecyclerView.setVisibility(View.GONE);
                mTextViewEmpty.setVisibility(View.VISIBLE);
            }
        }
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, this));
        // You do it right before you set the adapter.
        //listview.setEmptyView(emptyView);

        mRecyclerView.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isConnected)
                {
                    MaterialDialog dialog = new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .autoDismiss(false)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback()
                            {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input)
                                {
                                    if (input.length() != 0)
                                    {
                                        dialog.dismiss();
                                        //This is very imp. Else your db will treat Goog and goog differnt stocks. though they are same.
                                        String s = input.toString().toUpperCase().trim();
                                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                                        // in the DB and proceed accordingly
                                        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                                new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                                new String[]{s}, null);
                                        if (c.getCount() != 0)
                                        {
                                            Snackbar.make(mRecyclerView, getString(R.string.symbol_already_saved), Snackbar.LENGTH_SHORT)
                                                    .show();

                                            return;
                                        }
                                        else
                                        {
                                            // Add the stock to DB
                                            mServiceIntent.putExtra("tag", "add");
                                            mServiceIntent.putExtra("symbol", s);
                                            startService(mServiceIntent);
                                            showProgressDialog(mContext);
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(mContext, R.string.enter_symbol, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .show();
                }
                else
                {
                    networkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mTitle = getTitle();
        if (isConnected)
        {
            long period = 60L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        isConnected = Utils.isNetworkAvailable(mContext);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void networkToast()
    {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        if (id == R.id.action_change_units)
        {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,
                        QuoteColumns.NAME, QuoteColumns.CURRENCY, QuoteColumns.LASTTRADEDATE,
                        QuoteColumns.DAYLOW, QuoteColumns.DAYHIGH, QuoteColumns.YEARLOW,
                        QuoteColumns.YEARHIGH, QuoteColumns.EARNINGSSHARE, QuoteColumns.MARKETCAPITALIZATION},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (mProgressdialog != null)
        {
            mProgressdialog.dismiss();
        }
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        //Below call will handle view in case server is down/or user entered wrong input
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(getString(R.string.stock_status_key)))
        {
            updateEmptyView();
        }

    }

    /*
        Updates the empty list view with contextually relevant information that the user can
        use to determine why they aren't seeing weather.
     */
    private void updateEmptyView()
    {
        if (mCursorAdapter.getItemCount() == 0)
        {

            mRecyclerView.setVisibility(View.GONE);
            mTextViewEmpty.setVisibility(View.VISIBLE);

            TextView tv = (TextView) findViewById(R.id.empty_recycler_view);
            if (null != tv)
            {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_stock;
                @StockTaskService.StockStatus int stockStatus = Utils.getStockStatus(this);
                switch (stockStatus)
                {
                    case StockTaskService.STOCK_STATUS_SERVER_DOWN:
                        message = R.string.empty_stock_server_down;
                        break;
                    case StockTaskService.STOCK_STATUS_SERVER_INVALID:
                        message = R.string.empty_stock_server_error;
                        break;
                    case StockTaskService.STOCK_STATUS_INVALID:
                        message = R.string.empty_stock_invalid;
                        break;
                    default:
                        if (!Utils.isNetworkAvailable(this))
                        {
                            message = R.string.empty_stock_no_network;
                        }
                }
                if (mProgressdialog != null)
                {
                    mProgressdialog.dismiss();
                    Utils.resetStockStatus(mContext);
                }
                tv.setText(message);
            }
        }
        else
        {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTextViewEmpty.setVisibility(View.GONE);
            if (mProgressdialog != null)
            {
                mProgressdialog.dismiss();
                if (Utils.getStockStatus(mContext) == StockTaskService.STOCK_STATUS_INVALID)
                {
                    Toast.makeText(mContext, R.string.empty_stock_invalid, Toast.LENGTH_SHORT).show();
                    Utils.resetStockStatus(mContext);
                }
                return;
            }
        }

    }

    public void showProgressDialog(Context c)
    {
        mProgressdialog = new ProgressDialog(c);
        mProgressdialog.setMessage(getResources().getString(R.string.loading));
        mProgressdialog.setCancelable(false);
        mProgressdialog.show();
    }

    @Override
    public void onItemClick(View v, int position)
    {
        mCursor.moveToPosition(position);
        Intent intent = new Intent(v.getContext(), StockDetailActivity.class);
        intent.putExtra(StockDetailFragment.SYMBOL_CLICKED, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
        intent.putExtra(StockDetailFragment.NAME, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.NAME)));
        intent.putExtra(StockDetailFragment.CURRENCY, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CURRENCY)));
        intent.putExtra(StockDetailFragment.DAY_HIGH, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.DAYHIGH)));
        intent.putExtra(StockDetailFragment.DAY_LOW, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.DAYLOW)));
        intent.putExtra(StockDetailFragment.EARNINGS_SHARE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.EARNINGSSHARE)));
        intent.putExtra(StockDetailFragment.LAST_TRADE_DATE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.LASTTRADEDATE)));
        intent.putExtra(StockDetailFragment.MARKET_CAP, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.MARKETCAPITALIZATION)));
        intent.putExtra(StockDetailFragment.YEAR_HIGH, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.YEARHIGH)));
        intent.putExtra(StockDetailFragment.YEAR_LOW, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.YEARLOW)));
        intent.putExtra(StockDetailFragment.BID_PRICE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
        intent.putExtra(StockDetailFragment.PERCENT_CHANGE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        intent.putExtra(StockDetailFragment.CHANGE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
        startActivity(intent);
    }
}
