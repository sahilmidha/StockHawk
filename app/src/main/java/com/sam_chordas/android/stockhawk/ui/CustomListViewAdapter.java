package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sahilmidha on 26/07/16.
 */
public class CustomListViewAdapter extends ArrayAdapter
{

    Context context;
    int layoutResourceId;
    Stocks data[] = null;


    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param data  The objects to represent in the ListView.
     */
    public CustomListViewAdapter(Context context, int resource, Stocks[] data)
    {
        super(context, resource, data);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = data;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView)row.findViewById(R.id.stock_attribute_name);
            holder.value = (TextView)row.findViewById(R.id.stock_attribute_value);

            holder.value.setTextColor(Color.rgb(90,90,90));
            holder.name.setTextColor(Color.rgb(90,90,90));

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        Stocks stocks = data[position];

        holder.name.setText(stocks.attrName);
        holder.value.setText(stocks.attrValue);

        return row;
    }

    static class ViewHolder
    {
        TextView name;
        TextView value;
    }

    public static class Stocks{
        public String attrName;
        public String attrValue;

        public Stocks(String name, String value) {
            super();
            this.attrName = name;
            this.attrValue = value;
        }
    }
}
