package com.example.drawsomethingfun.controller;
import java.lang.annotation.Annotation;
import java.util.Timer;
import java.util.TimerTask;

import com.example.drawsomethingfun.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.test.UiThreadTest;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;


/**
 * 
 * @author yanzhi
 *
 */

public class GuideActivity extends Activity{
	public void onCreate(Bundle savedInstanceState){
	  super.onCreate(savedInstanceState);

	  setContentView(R.layout.activity_guide);
	  //设置欢迎界面显示一秒后在显示登录和注册的入口
	  Timer timer = new Timer();
	  
	  TimerTask task = new TimerTask() {
		  @Override
		   public void run() {
			  runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					startActivity(new Intent(GuideActivity.this,HomeActivity.class));
					finish();
				}
			  });
		   }
	  };
	// Schedule a task for single execution after a specified delay.
	   timer.schedule(task, 1000 * 1);

  }
}
