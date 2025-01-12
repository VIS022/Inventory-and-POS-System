package com.example.posandinventorysystem;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EarningsHistoryAdapter extends ArrayAdapter<EarningsHistoryItem> {

    private Context context;
    private List<EarningsHistoryItem> earningsHistoryList;

    public EarningsHistoryAdapter(Context context, List<EarningsHistoryItem> earningsHistoryList) {
        super(context, 0, earningsHistoryList);
        this.context = context;
        this.earningsHistoryList = earningsHistoryList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_earning_history, parent, false);
        }

        EarningsHistoryItem item = earningsHistoryList.get(position);

        TextView monthDateTextView = convertView.findViewById(R.id.monthDateTextView);
        TextView txtLblEarnings = convertView.findViewById(R.id.txtLblEarnings);
        TextView txtLblCapital = convertView.findViewById(R.id.txtLblCapital);
        TextView txtLblTotal = convertView.findViewById(R.id.txtLblTotal);

        monthDateTextView.setText(item.getMonthYear());
        txtLblEarnings.setText(item.getEarnings());
        txtLblCapital.setText(item.getCapital());
        txtLblTotal.setText(item.getTotal());

        return convertView;
    }

}

