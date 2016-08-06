package com.sam_chordas.android.stockhawk.ui;


import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoryColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int CURSOR_LOADER_ID_FOR_CHART = 0;

    public static String mSymbol;

    private LineChart mLineChart;

    public ChartFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        getLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_CHART, null, this);

        mLineChart = (LineChart) view.findViewById(R.id.chart);
        return view;
    }

    public static Fragment newInstance(String symbol)
    {

        mSymbol = symbol;
        return new ChartFragment();
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume()
    {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID_FOR_CHART, null, this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String whereClause = QuoteHistoryColumns.SYMBOL + " = ? ";
        String [] whereArgs = {mSymbol};

        return new android.support.v4.content.CursorLoader(
                getContext(),
                QuoteProvider.QuotesHistory.CONTENT_URI,
                new String[]{QuoteHistoryColumns._ID, QuoteHistoryColumns.SYMBOL,
                            QuoteHistoryColumns.LAST_CLOSING_PRICE, QuoteHistoryColumns.DATE},
                whereClause,
                whereArgs,
                null);
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if(data != null)
        {
            updateChart(data);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        //Nothing to do here. We aren't using any Adapters
    }

    public void updateChart(Cursor cursor)
    {
        //this will store yValues(Closing price)
        ArrayList<Entry> entries = new ArrayList<>();

        //this will store xValues(Date)
        ArrayList<String> xvalues = new ArrayList<>();

        cursor.moveToFirst();
        int i=0;
        while(cursor.moveToNext()){
            //out Yvalue will store closing price.
            double yValue_closingPrice = cursor.getDouble(cursor.getColumnIndex(QuoteHistoryColumns.LAST_CLOSING_PRICE));
            entries.add(new Entry((float) yValue_closingPrice, i++));

            //xValue will store date
            String date = Utils.convertDate(cursor.getString(cursor.getColumnIndex(QuoteHistoryColumns.DATE)));
            xvalues.add(date);
        }

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setLabelsToSkip(5);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.rgb(182,182,182));

        YAxis left = mLineChart.getAxisLeft();
        left.setEnabled(true);
        left.setLabelCount(10, true);
        left.setTextColor(Color.rgb(182,182,182));

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getLegend().setTextSize(16f);
        mLineChart.setDrawGridBackground(true);
        mLineChart.setGridBackgroundColor(Color.rgb(25,118,210));
        mLineChart.setDescriptionColor(Color.WHITE);
        mLineChart.setDescription(getString(R.string.one_year_stock_comparison));

        String name= getResources().getString(R.string.instructions_chart);
        LineDataSet dataSet = new LineDataSet(entries, name);
        LineData lineData = new LineData(xvalues, dataSet);

        mLineChart.animateX(2500);
        mLineChart.setData(lineData);
    }
}
