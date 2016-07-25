package com.sam_chordas.android.stockhawk.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass. This will contain detailed information for a stock symbol.
 *
 */
public class StockDetailFragment extends Fragment
{
    public static String LOG_TAG = StockDetailFragment.class.getSimpleName();
    public static final String SYMBOL_CLICKED = "symbol";
    public static final String NAME = "name";
    public static final String CURRENCY = "cur";
    public static final String LAST_TRADE_DATE = "trade_dt";
    public static final String DAY_LOW = "day_low";
    public static final String DAY_HIGH = "day_high";
    public static final String YEAR_LOW = "year_low";
    public static final String YEAR_HIGH = "year_high";
    public static final String EARNINGS_SHARE = "ear_share";
    public static final String MARKET_CAP = "mkt_cap";
    public static final String PERCENT_CHANGE = "percent_change";
    public static final String BID_PRICE = "bid_price";
    public static final String CHANGE = "change";

    public String mSymbol;
    public String mName;
    public String mCurrency;
    public String mLastTradeDate;
    public String mDayLow;
    public String mDayHigh;
    public String mYearLow;
    public String mYearHigh;
    public String mEarningsShare;
    public String mMarketcapitalization;
    public String mBidPrice;
    public String mPercentChange;
    public String mChange;

    private int mMaxScrollSize;


    public StockDetailFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stock_detail, container, false);

        if (getArguments().containsKey(SYMBOL_CLICKED))
        {
            mSymbol = getArguments().getString(SYMBOL_CLICKED);
            mName = getArguments().getString(NAME);
            mCurrency = getArguments().getString(CURRENCY);
            mLastTradeDate = getArguments().getString(LAST_TRADE_DATE);
            mDayHigh = getArguments().getString(DAY_HIGH);
            mDayLow = getArguments().getString(DAY_LOW);
            mYearHigh = getArguments().getString(YEAR_HIGH);
            mYearLow = getArguments().getString(YEAR_LOW);
            mEarningsShare = getArguments().getString(EARNINGS_SHARE);
            mMarketcapitalization = getArguments().getString(MARKET_CAP);
            mBidPrice = getArguments().getString(BID_PRICE);
            mPercentChange = getArguments().getString(PERCENT_CHANGE);
            mChange = getArguments().getString(CHANGE);


        }
        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(rootView, savedInstanceState);

        TextView name = (TextView) rootView.findViewById(R.id.stock_name);
        TextView symbol = (TextView) rootView.findViewById(R.id.stock_symbol);
        TextView bidPrice = (TextView) rootView.findViewById(R.id.stock_bidprice);
        TextView stockChange = (TextView) rootView.findViewById(R.id.stock_change);

        String bothChanges = mChange + " (" + mPercentChange + ")";

        name.setText(mName);
        symbol.setText(mSymbol);
        bidPrice.setText(mBidPrice);
        stockChange.setText(bothChanges);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.materialup_tabs);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.materialup_viewpager);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.materialup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //onBackPressed();
            }
        });


        viewPager.setAdapter(new TabsAdapter(getActivity().getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    class TabsAdapter extends FragmentPagerAdapter
    {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case 0: return ChartFragment.newInstance();
                case 1: return ChartFragment.newInstance();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return "Tab 1";
                case 1: return "Tab 2";
            }
            return "";
        }
    }
}
