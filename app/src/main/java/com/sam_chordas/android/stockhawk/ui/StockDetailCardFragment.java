package com.sam_chordas.android.stockhawk.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockDetailCardFragment extends Fragment
{
    static Bundle mArgs;
    
    public String mCurrency;
    public String mLastTradeDate;
    public String mDayLow;
    public String mDayHigh;
    public String mYearLow;
    public String mYearHigh;
    public String mEarningsShare;
    public String mMarketcapitalization;
    
    public StockDetailCardFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stock_detail_card, container, false);
    }

    public static Fragment newInstance(Bundle args)
    {
        mArgs = args;
        return new StockDetailCardFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mCurrency = mArgs.getString(StockDetailFragment.CURRENCY);
        mLastTradeDate = mArgs.getString(StockDetailFragment.LAST_TRADE_DATE);
        mDayHigh = mArgs.getString(StockDetailFragment.DAY_HIGH);
        mDayLow = mArgs.getString(StockDetailFragment.DAY_LOW);
        mYearHigh = mArgs.getString(StockDetailFragment.YEAR_HIGH);
        mYearLow = mArgs.getString(StockDetailFragment.YEAR_LOW);
        mEarningsShare = mArgs.getString(StockDetailFragment.EARNINGS_SHARE);
        mMarketcapitalization = mArgs.getString(StockDetailFragment.MARKET_CAP);

        CustomListViewAdapter.Stocks stocks[] = new CustomListViewAdapter.Stocks[]
                {
                        new CustomListViewAdapter.Stocks(StockDetailFragment.CURRENCY, mCurrency),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.LAST_TRADE_DATE, mLastTradeDate),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.DAY_HIGH, mDayHigh),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.DAY_LOW, mDayLow),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.YEAR_HIGH, mYearHigh),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.YEAR_LOW, mYearLow),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.EARNINGS_SHARE, mEarningsShare),
                        new CustomListViewAdapter.Stocks(StockDetailFragment.MARKET_CAP, mMarketcapitalization),

                };


        ListView listView = (ListView) view.findViewById(R.id.card_detail_list_view);
        CustomListViewAdapter adapter = new CustomListViewAdapter(getContext(), R.layout.list_view_stock_details, stocks);
        listView.setAdapter(adapter);
    }
}
