package com.example.drawsomethingfun.utils;

import com.example.drawsomethingfun.R;

import android.R.interpolator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
/**
 * 
 * @author yanzhi
 *
 */

public class Utils {
	/**
	 * 检测wifi是否可用
	 * 
	 * @return true为网络可用，否则不可用
	 */
	public static boolean networkEnable(Context context) {
		WifiManager wifimanage=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);//获取WifiManager   
		//检查wifi是否开启 
		return wifimanage.isWifiEnabled();
	}
	
	public static String getIpAddress(Context context)
	{
		WifiManager wifimanage=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);//获取WifiManager  
		
		WifiInfo wifiinfo= wifimanage.getConnectionInfo();  
		  
		String ip= int2ip(wifiinfo.getIpAddress());
		return ip;
	}
	
	public static String int2ip(long ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
	}
	
	static private AlertDialog loadingDialog;

	public interface Callback
	{
		public void onRun();
	}
	/**
	 * 显示加载中对话框
	 * 
	 * @param context
	 */
	static public void showLoadingDialog(final Context context,final String title,final Callback callback) {
		if (loadingDialog == null) {
			View view = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_loading, null);
			TextView t = (TextView) view.findViewById(R.id.title);
			t.setText(title);
			Button bt = (Button)view.findViewById(R.id.cancel);
			bt.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					callback.onRun();
				}
			});
			loadingDialog = new AlertDialog.Builder(context).create();
			loadingDialog.setView(view);
			loadingDialog.setCancelable(false);
			loadingDialog.show();
		}
	}

	/**
	 * 关闭加载对话框
	 */
	static public void closeLoadingDialog() {
		if (loadingDialog != null) {
			loadingDialog = null;
		}
	}
	
	/**
	 * 加載佈局框
	 */
	public static void showTextLoadingDialog(Context context, int layout_id) {
		if (loadingDialog == null) {
			View view = ((Activity) context).getLayoutInflater().inflate(layout_id, null);
			loadingDialog = new AlertDialog.Builder(context,R.style.dialog).create();

			loadingDialog.show();
			loadingDialog.getWindow().setContentView(view);
			loadingDialog.setCancelable(true);
			loadingDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					// TODO Auto-generated method stub
					loadingDialog = null;
				}
			});
		}
	}
}
