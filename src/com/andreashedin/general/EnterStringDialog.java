package com.andreashedin.general;

import com.andreashedin.infowallpaper.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;

public class EnterStringDialog extends Dialog implements OnClickListener {

	public interface StringEnteredListener {
		void enteredString(String string);
	}
	
	StringEnteredListener mListener;
	String mDefaultString;
	String mTitle;
	EditText mInput;
	
	public EnterStringDialog(Context context, StringEnteredListener listener, String title, String defaultString) {
		super(context);
		
		mListener = listener;
		mTitle = title;
		mDefaultString = defaultString;
	}
	
	@Override
	public void onBackPressed() {
		cancel();
	}

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.enter_string);
        
        mInput = (EditText)findViewById(R.id.stringInput);
        mInput.setText(mDefaultString);
        
        Button b = (Button) findViewById(R.id.enterStringDone);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.enterStringCancel);
        b.setOnClickListener(this);
        
        setTitle(mTitle);
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.enterStringDone) {
			mListener.enteredString(mInput.getText().toString());
			dismiss();
		}
		else
			cancel();
	}
}
