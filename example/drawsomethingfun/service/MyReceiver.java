package com.example.drawsomethingfun.service;

import com.example.drawsomethingfun.Constants;
import com.example.drawsomethingfun.controller.RoomActivity;
import com.example.drawsomethingfun.model.GameQuestion;
import com.example.drawsomethingfun.utils.NetworkManager;
import com.example.drawsomethingfun.utils.Utils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.protobuftest.protobuf.GameProbuf.Game;
import com.protobuftest.protobuf.GameProbuf.Game.Answer;
import com.protobuftest.protobuf.GameProbuf.Game.Begin;
import com.protobuftest.protobuf.GameProbuf.Game.End;
import com.protobuftest.protobuf.GameProbuf.Game.MsgType;
import com.protobuftest.protobuf.GameProbuf.Game.Point;
import com.protobuftest.protobuf.GameProbuf.Game.Ready;
import com.protobuftest.protobuf.GameProbuf.Game.Result;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver{
	
	RoomActivity activity;
	public MyReceiver(Context context) {
		// TODO Auto-generated constructor stub
		activity= (RoomActivity)context;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		String action = intent.getAction();
		if(Constants.CANCEL_LOADING.equals(action))
		{
			Log.v("cyz","cyz cancel loading");
			Utils.closeLoadingDialog();
		}
		else if(action.equals(Constants.SOCKET_DISCONNECT))
		{
			Toast.makeText(context, "你或者对方居然断线了T^T", 200).show();
			activity.finish();
		}
		else if(action.equals(Constants.RESTARTGAME))
		{
			activity.initView();
		}
		else if(Constants.RECEIVEMSG.equals(action))
		{
			byte[] data = (byte[]) intent.getExtras().get("message");
			try {
				Game game = Game.parseFrom(data);
				MsgType type = game.getType();
//				Toast.makeText(context, "type = "+type.getNumber(), 200).show();
				switch(type.getNumber())
				{
				case MsgType.READY_VALUE:
					Ready ready = game.getReady();
					if(ready.getReady() == 1)
					{
						activity.getGame().getPlay1().setReady(true);
						if(activity.getGame().ready())
						{
							activity.startGameLoop();
						}
					}
					else
						activity.getGame().getPlay1().setReady(false);
					
					//Toast.makeText(context, "准备"+ready.getReady(), 200).show();
					break;
				case MsgType.BEGIN_VALUE:
					Begin begin = game.getBegin();
					//Toast.makeText(context, "begin " + begin.toString(), 200).show();
					
					activity.setBegin(begin);
					break;
				case MsgType.POINT_VALUE:
					Point p = game.getPoint();
					activity.drawPoint(p);
					break;
				case MsgType.ANSWER_VALUE:
					Answer answer = game.getAnswer();
					//Toast.makeText(context, "Answer " + answer.toString(), 200).show();
					activity.answer(answer);
					break;
				case MsgType.RESULT_VALUE:
					Result result = game.getResult();
					//Toast.makeText(context, "result " + result.toString(), 200).show();
					activity.receiveResult(result);
					break;
				case MsgType.END_VALUE:
					End end = game.getEnd();
					activity.receiveEnd(end);
				//	Toast.makeText(context, "end " + end.toString(), 200).show();
					break;
				case MsgType.CLEARDRAW_VALUE:
					activity.clear();
					break;
				}
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
