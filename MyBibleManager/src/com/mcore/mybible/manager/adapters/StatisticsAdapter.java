package com.mcore.mybible.manager.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mcore.mybible.common.dto.DayStatisticDTO;
import com.mcore.mybible.manager.R;

public class StatisticsAdapter extends ArrayAdapter<DayStatisticDTO> {

	public StatisticsAdapter(Context context, int resource,
			List<DayStatisticDTO> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater vi = LayoutInflater.from(getContext());
		View v = vi.inflate(R.layout.statistic_item, null);
		DayStatisticDTO p = getItem(position);
		TextView dt = (TextView) v.findViewById(R.id.text_day);
		dt.setText(p.getDay());
		TextView nu = (TextView) v.findViewById(R.id.text_new_users);
		nu.setText(String.valueOf(p.getNewuserscount()));
		TextView u = (TextView) v.findViewById(R.id.text_users);
		u.setText(String.valueOf(p.getUsercount()));
		TextView d = (TextView) v.findViewById(R.id.text_downloads);
		d.setText(String.valueOf(p.getDownloads()));
		return v;
	}

}
