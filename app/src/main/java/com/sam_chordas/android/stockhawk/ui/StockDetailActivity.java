package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.sam_chordas.android.stockhawk.R;

public class StockDetailActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_stock_detail);

        getSupportActionBar().setTitle(getString(R.string.stock_detail_title));
        if (savedInstanceState == null) {

            Intent intent = getIntent();

            Bundle arguments = new Bundle();
            arguments.putString(StockDetailFragment.SYMBOL_CLICKED,intent.getStringExtra(StockDetailFragment.SYMBOL_CLICKED));
            arguments.putString(StockDetailFragment.NAME,intent.getStringExtra(StockDetailFragment.NAME));
            arguments.putString(StockDetailFragment.CURRENCY,intent.getStringExtra(StockDetailFragment.CURRENCY));
            arguments.putString(StockDetailFragment.MARKET_CAP,intent.getStringExtra(StockDetailFragment.MARKET_CAP));
            arguments.putString(StockDetailFragment.YEAR_HIGH,intent.getStringExtra(StockDetailFragment.YEAR_HIGH));
            arguments.putString(StockDetailFragment.YEAR_LOW,intent.getStringExtra(StockDetailFragment.YEAR_LOW));
            arguments.putString(StockDetailFragment.DAY_HIGH,intent.getStringExtra(StockDetailFragment.DAY_HIGH));
            arguments.putString(StockDetailFragment.DAY_LOW,intent.getStringExtra(StockDetailFragment.DAY_LOW));
            arguments.putString(StockDetailFragment.LAST_TRADE_DATE,intent.getStringExtra(StockDetailFragment.LAST_TRADE_DATE));
            arguments.putString(StockDetailFragment.EARNINGS_SHARE,intent.getStringExtra(StockDetailFragment.EARNINGS_SHARE));
            arguments.putString(StockDetailFragment.BID_PRICE,intent.getStringExtra(StockDetailFragment.BID_PRICE));
            arguments.putString(StockDetailFragment.PERCENT_CHANGE,intent.getStringExtra(StockDetailFragment.PERCENT_CHANGE));
            arguments.putString(StockDetailFragment.CHANGE,intent.getStringExtra(StockDetailFragment.CHANGE));

            StockDetailFragment fragment = new StockDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_container, fragment)
                    .commit();
        }
    }

    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }
}
