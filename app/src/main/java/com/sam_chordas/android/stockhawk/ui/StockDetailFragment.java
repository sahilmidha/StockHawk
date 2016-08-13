package com.sam_chordas.android.stockhawk.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;

/**
 * A simple {@link Fragment} subclass. This will contain detailed information for a stock symbol.
 */
public class StockDetailFragment extends Fragment
{
    public static String LOG_TAG = StockDetailFragment.class.getSimpleName();
    public static final String SYMBOL_CLICKED = "Symbol";
    public static final String NAME = "Name";
    public static final String PERCENT_CHANGE = "Percent Change";
    public static final String BID_PRICE = "Bid Price";
    public static final String CHANGE = "Change";
    public static final String CURRENCY = "Currency";
    public static final String LAST_TRADE_DATE = "Last trade date";
    public static final String DAY_LOW = "Day low";
    public static final String DAY_HIGH = "Day high";
    public static final String YEAR_LOW = "Year low";
    public static final String YEAR_HIGH = "Year high";
    public static final String EARNINGS_SHARE = "Earnings Share";
    public static final String MARKET_CAP = "Market Capitalisation";

    public String mSymbol;
    public String mName;
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

        name.setContentDescription(getString(R.string.symbol_name) + mName);
        symbol.setContentDescription(getString(R.string.symbol) + mSymbol);
        bidPrice.setContentDescription(getString(R.string.bid_price) + mBidPrice);
        stockChange.setContentDescription(getString(R.string.change) + bothChanges);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.materialup_tabs);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.materialup_viewpager);

        NestedScrollView scrollView = (NestedScrollView) rootView.findViewById(R.id.myscroll);
        /*
        This is important to set to keep viewpager inside of a scrollview, else data in your viewpager won't be visible
        ScrollViews are used to have only one children (e.g. vertical LinearLayout), and ScrollView's default height is based on
        the content inside LinearLayout. Let's say LinearLayout has three 30dp height views inside, then ScrollView's height now is 90dp.
        If you put the paremeter (android:fillViewport="true") then it's gonna make the height match/fill the parent.
         */
        scrollView.setFillViewport(true);
        viewPager.setAdapter(new TabsAdapter(getActivity().getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    class TabsAdapter extends FragmentPagerAdapter
    {
        public TabsAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public Fragment getItem(int i)
        {
            switch (i)
            {
                case 0:
                    return ChartFragment.newInstance(mSymbol);
                case 1:
                    return StockDetailCardFragment.newInstance(getArguments());
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "Chart";
                case 1:
                    return "More";
            }
            return "";
        }
    }
}
