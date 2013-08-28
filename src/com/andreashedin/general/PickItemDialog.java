package com.andreashedin.general;

import java.util.ArrayList;

import com.andreashedin.infowallpaper.DisplayValuePair;
import com.andreashedin.infowallpaper.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PickItemDialog extends Dialog implements OnItemClickListener {
	
	public interface OnItemPickedListener {
		void itemPicked(int id, int flag);
	}
	
	private OnItemPickedListener mListener;
	private ArrayList<DisplayValuePair<Integer>> mItemList;
	private int mFlag;
	private ListView mList;
	
	public PickItemDialog(Context context, OnItemPickedListener listener, ArrayList<DisplayValuePair<Integer>> items, int flag) {
		super(context);
	
		mFlag = flag;
		mItemList = items;
		
		mListener = listener;
	}
	
	@Override
	public void onBackPressed() {
		cancel();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.pick_item); 
        
        mList = (ListView) findViewById(R.id.ListView01);
        mList.setOnItemClickListener(this);//.setOnItemSelectedListener(this);
        
        mList.setAdapter(new ArrayAdapter<DisplayValuePair<Integer>>(getContext(), R.layout.list_item, mItemList));
        
        setTitle(R.string.pickItemTitle);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
		Integer val = ((DisplayValuePair<Integer>)mList.getItemAtPosition(pos)).getValue();
		mListener.itemPicked(val.intValue(), mFlag);
		dismiss();
	}
}
