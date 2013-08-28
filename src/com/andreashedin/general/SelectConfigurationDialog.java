package com.andreashedin.general;

import java.util.ArrayList;

import com.andreashedin.infowallpaper.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectConfigurationDialog extends Dialog implements android.view.View.OnClickListener, OnItemClickListener {

	public interface SelectConfigurationListener {
		void configurationSelected(String str, int from);
	}
	
	SelectConfigurationListener mListener;
	ArrayList<String> mList;
	int mFrom;
	
	public SelectConfigurationDialog(Context context, SelectConfigurationListener listener, ArrayList<String> list, int from) {
		super(context);
		
		mListener = listener;
		mList = list;
		mFrom = from;
	}

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_configuration);
        
        ListView list = (ListView)findViewById(R.id.ListView02);
        list.setOnItemClickListener(this);
        
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item, mList);
        list.setAdapter(listAdapter);
        
        setTitle(R.string.selectConfigurationTitle);
	}
	
	@Override
	public void onBackPressed() {
		cancel();
	}

	@Override
	public void onClick(View arg0) {
		cancel();
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
		String val = (String)list.getItemAtPosition(pos);
		mListener.configurationSelected(val, mFrom);
		dismiss();		
	}
}