package com.example.drawsomethingfun.controller;

import java.io.File;

import com.example.drawsomethingfun.Constants;
import com.example.drawsomethingfun.R;
import com.example.drawsomethingfun.service.MyReceiver;
import com.example.drawsomethingfun.utils.NetworkManager;
import com.example.drawsomethingfun.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 
 * @author yanzhi
 *
 */

public class HomeActivity extends Activity implements OnClickListener{
	private Context context;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.createRoom:
			if(Utils.networkEnable(context))
			{
				Intent i = new Intent(HomeActivity.this,RoomActivity.class);
				i.putExtra("ROLE", Constants.SEVER);
				startActivity(i);
			}
			else
			{
				Toast.makeText(context, "請先連接wifi", 200).show();
			}
			break;
		case R.id.enterRoom:
			
			if(Utils.networkEnable(context))
			{
				Intent i = new Intent(HomeActivity.this,RoomActivity.class);
				i.putExtra("ROLE", Constants.CLIENT);
				startActivity(i);
			}
			else
			{
				Toast.makeText(context, "請先連接wifi", 200).show();
			}
			break;
		case R.id.intruduction:
			startActivity(new Intent(HomeActivity.this, RuleActivity.class));
			break;
		case R.id.about:
			startActivity(new Intent(HomeActivity.this, AboutActivity.class));
			break;
		}
		
	}

}
