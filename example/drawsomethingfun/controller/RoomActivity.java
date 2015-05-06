package com.example.drawsomethingfun.controller;

import com.example.drawsomethingfun.Constants;
import com.example.drawsomethingfun.R;
import com.example.drawsomethingfun.model.GameQuestion;
import com.example.drawsomethingfun.service.MyReceiver;
import com.example.drawsomethingfun.utils.NetworkManager;
import com.example.drawsomethingfun.utils.Utils;
import com.example.drawsomethingfun.utils.Utils.Callback;
import com.example.drawsomethingfun.view.DrawView;
import com.protobuftest.protobuf.GameProbuf.Game;
import com.protobuftest.protobuf.GameProbuf.Game.Answer;
import com.protobuftest.protobuf.GameProbuf.Game.Begin;
import com.protobuftest.protobuf.GameProbuf.Game.End;
import com.protobuftest.protobuf.GameProbuf.Game.MsgType;
import com.protobuftest.protobuf.GameProbuf.Game.Point;
import com.protobuftest.protobuf.GameProbuf.Game.Ready;
import com.protobuftest.protobuf.GameProbuf.Game.Result;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 
 * @author yanzhi
 *
 */

@SuppressLint({ "HandlerLeak", "NewApi" })
public class RoomActivity extends Activity implements OnClickListener {
	private Context context;
	private int roleType = 0;
	TextView roomNum;

	DrawView drawView = null;
	int isReady = 0; // 0表示可以准备，1表示取消准备，-1表示游戏已经开始
	private boolean isTurn = false;// 是否正在轮到画画
	MyReceiver myReceiver;
	NetworkManager networkmanager;
	private DrawGame game;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preroom);
		context = this;
		drawView = (DrawView) findViewById(R.id.drawView);
		drawView.init();
		roleType = getIntent().getIntExtra("ROLE", 0);
		networkmanager = NetworkManager.getInstance(getApplicationContext());
		
		setGame(new DrawGame(context));
		IntentFilter filter = new IntentFilter();
		myReceiver = new MyReceiver(context);
		filter.addAction(Constants.RESTARTGAME);
		filter.addAction(Constants.RECEIVE_FROM_CLIENT);
		filter.addAction(Constants.RECEIVEMSG);
		filter.addAction(Constants.CANCEL_LOADING);
		filter.addAction(Constants.SOCKET_DISCONNECT);
		registerReceiver(myReceiver, filter);

		initView();
		if (roleType == Constants.CLIENT) {
			Utils.showLoadingDialog(context, "寻找房间中...",new Callback() {
				
				@Override
				public void onRun() {
					// TODO Auto-generated method stub
					Utils.closeLoadingDialog();
					RoomActivity.this.finish();
				}
			});
			networkmanager.startReceiveIp();
		} else {
			Utils.showLoadingDialog(context, "等待对手进入...",new Callback() {
				
				@Override
				public void onRun() {
					// TODO Auto-generated method stub
					Utils.closeLoadingDialog();
					RoomActivity.this.finish();
				}
			});

			networkmanager.startReceiveConnect();
			networkmanager.startBroadCastIp();
		}
	}

	public void drawPoint(Point p) {
		drawView.receiceDraw(p);
	}

	public void initView() {
		((Button) findViewById(R.id.ready)).setText("准备");
		((Button) findViewById(R.id.ready)).setOnClickListener(this);
		((Button) findViewById(R.id.ready)).setBackground(getResources().getDrawable(R.drawable.layoutbk_active));
//		((Button) findViewById(R.id.ready)).setTextColor(getResources().getColor(R.color.white));
		isReady = 0;
		setButtonOfDrawOff();
		isRuning = false;
		findViewById(R.id.answerLayout).setVisibility(View.GONE);
		game.reset();
	}

	StartLoop startLoop;
	private boolean isRuning;

	public void startGameLoop() {
		readyButtonOff();
		TextView tv = (TextView) findViewById(R.id.hind);
		((EditText)findViewById(R.id.editanswer)).setText("");
		game.setLoopOn(true);
		drawView.clear();
		Toast.makeText(context, "新一轮开始啦", Toast.LENGTH_SHORT).show();
		if (roleType == Constants.CLIENT) {
			if (game.getPlay1().isTurn()) {
				tv.setText("题目:" + game.getQuestion().getAnswer());
				setButtonOfDrawOn();
				findViewById(R.id.answerLayout).setVisibility(View.GONE);
				drawView.setReceivable(false);
			} else {
				findViewById(R.id.answerLayout).setVisibility(View.VISIBLE);
				tv.setText(game.getQuestion().getHind() + "("
						+ game.getQuestion().getLength() + "字)");
				drawView.setReceivable(true);
				setButtonOfDrawOff();
			}
		} else {
			game.startLoop();
			if (game.getPlay2().isTurn()) {
				setTurn(true);
				tv.setText("题目:" + game.getQuestion().getAnswer());
				setButtonOfDrawOn();
				drawView.setReceivable(false);
			} else {
				setTurn(false);
				tv.setText(game.getQuestion().getHind() + "("
						+ game.getQuestion().getLength() + "字)");
				setButtonOfDrawOff();
				drawView.setReceivable(true);
			}
		}
		startLoop = new StartLoop();
		startLoop.execute();
	}

	private void setButtonOfDrawOff() {
		// TODO Auto-generated method stub
		findViewById(R.id.answerLayout).setVisibility(View.VISIBLE);
		findViewById(R.id.pen).setOnClickListener(null);
		findViewById(R.id.clear).setOnClickListener(null);
		((Button) findViewById(R.id.pen)).setBackground(getResources().getDrawable(R.drawable.layoutbk_disable));
		((Button) findViewById(R.id.clear)).setBackground(getResources().getDrawable(R.drawable.layoutbk_disable));
	}

	private void setButtonOfDrawOn() {
		// TODO Auto-generated method stub
		findViewById(R.id.answerLayout).setVisibility(View.GONE);
		findViewById(R.id.pen).setOnClickListener(this);
		findViewById(R.id.clear).setOnClickListener(this);
		((Button) findViewById(R.id.pen)).setBackground(getResources().getDrawable(R.drawable.layoutbk_active));
//		((Button) findViewById(R.id.pen)).setTextColor(getResources().getColor(R.color.white));
		((Button) findViewById(R.id.clear)).setBackground(getResources().getDrawable(R.drawable.layoutbk_active));
//		((Button) findViewById(R.id.clear)).setTextColor(getResources().getColor(R.color.white));
		
	}

	private class StartLoop extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			int i = game.GAMETIME;
			game.setRestTime(i);
			// 1s更新一次时间
			while (i >= 1 && game.isLoopOn()) {
				SystemClock.sleep(1000);
				i--;
				game.setRestTime(i);
				publishProgress(i);
			}
			game.setLoopOn(false);
			return i;
		}

		protected void onProgressUpdate(Integer... progress) {
			TextView tv = (TextView) findViewById(R.id.time);
			tv.setText(progress[0] + "s");
		}

		protected void onPostExecute(Integer result) {
			if (roleType == Constants.CLIENT)
				return; // 由服务器通知才开始下一轮
			else {
				game.sendResult();
				// 服务器开始下一轮
				if (game.getLoopCnt() != 6) {
					SystemClock.sleep(3000);
					startGameLoop();
				}
				else
				{
					isRuning = false;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
		networkmanager.release();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.pen:
			break;
		case R.id.ready:
			if (roleType == Constants.CLIENT) {
				// 客户端准备逻辑
				if (isReady == 0) {
					((Button) findViewById(R.id.ready)).setText("取消");
					isReady = 1;
					Ready ready = Ready.newBuilder().setReady(1).build();
					Game game = Game.newBuilder().setType(MsgType.READY)
							.setReady(ready).build();
					NetworkManager.sendMessage(game.toByteArray());
				} else {
					((Button) findViewById(R.id.ready)).setText("准备");
					isReady = 0;
					Ready ready = Ready.newBuilder().setReady(0).build();
					Game game = Game.newBuilder().setType(MsgType.READY)
							.setReady(ready).build();
					NetworkManager.sendMessage(game.toByteArray());
				}
			} else {
				// 服务器端准备逻辑
				if (isReady == 0) {
					((Button) findViewById(R.id.ready)).setText("取消");
					isReady = 1;
					getGame().getPlay2().setReady(true);
					if (game.ready()) {
						startGameLoop();
					}
				} else {
					((Button) findViewById(R.id.ready)).setText("准备");
					isReady = 0;
					getGame().getPlay2().setReady(false);
				}
			}
			break;
		case R.id.sendanswer:
			String ans = ((EditText) findViewById(R.id.editanswer)).getText()
					.toString();

			if (roleType == Constants.CLIENT) {
				if (game.isLoopOn()
						&& ans.equals(game.getQuestion().getAnswer())) {
					game.setLoopOn(false);
					Answer answer = Answer.newBuilder().setAns(ans).build();
					Game game = Game.newBuilder().setType(MsgType.ANSWER)
							.setAnswer(answer).build();
					NetworkManager.sendMessage(game.toByteArray());
				}
			} else {
				if (ans.equals(game.getQuestion().getAnswer())) {
					game.setLoopOn(false);
					startLoop.cancel(true);
					game.sendResult();
					// 服务器开始下一轮
					if (game.getLoopCnt() != 6) {
						SystemClock.sleep(3000);
						startGameLoop();
					}
					else
					{
						isRuning = false;
					}
				} else {
					Toast.makeText(context, "没猜对TT", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.clear:
			drawView.clear();
			Game game = Game.newBuilder().setType(MsgType.CLEARDRAW).build();
			NetworkManager.sendMessage(game.toByteArray());
			break;
		}
	}

	public DrawGame getGame() {
		return game;
	}

	public void setGame(DrawGame game) {
		this.game = game;
	}

	public void setQuestion(GameQuestion question) {
		game.setQuestion(question);
	}

	public boolean isTurn() {
		return isTurn;
	}

	public void setTurn(boolean isTurn) {
		drawView.setDrawable(isTurn);
	}

	public void setBegin(Begin begin) {
		// TODO Auto-generated method stub
		String hind = begin.getQestion().getHind();
		String a = begin.getQestion().getAnswer();
		int length = a.length();
		GameQuestion q = new GameQuestion(hind, a, length);
		if (begin.getIsDraw() == 1) {
			setTurn(true);
			game.getPlay1().setTurn(true);

		} else {
			setTurn(false);
			game.getPlay1().setTurn(false);
		}
		setQuestion(q);
		startGameLoop();
	}

	private void readyButtonOff() {
		// TODO Auto-generated method stub
		((Button) findViewById(R.id.ready)).setText("游戏中");
		((Button) findViewById(R.id.ready)).setOnClickListener(null);
		((Button) findViewById(R.id.ready)).setBackground(getResources().getDrawable(R.drawable.layoutbk_disable));
		isRuning = true;
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(isRuning == true)
		{
			
		}
		else{
			finish();
		}
	}

	public void answer(Answer answer) {
		// TODO Auto-generated method stub
		if (answer.getAns().equals(game.getQuestion().getAnswer())) {
			Toast.makeText(context, "对方猜中了", 200).show();
			game.setLoopOn(false);
		}
	}

	public void receiveResult(Result result) {
		// TODO Auto-generated method stub
		startLoop.cancel(true);
		game.setLoopOn(false);
		int isWin = result.getIsWin();
		if (result.getTimeSpend() < game.GAMETIME) {
			if (isWin == 1) {
				Toast.makeText(context, "這一輪您猜对了", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, "這一輪对方猜对了", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(context, "超时，即将开始下一轮", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void receiveEnd(End end)
	{
		if(end.getIsWin() == 1)
		{
			Toast.makeText(context, "這一局你贏了耶", Toast.LENGTH_SHORT).show();
		}
		else if(end.getIsWin() == -1)
		{
			Toast.makeText(context, "這一局你掛了耶", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(context, "這一局居然平了", Toast.LENGTH_SHORT).show();
		}
		
		startLoop.cancel(true);
		initView();
	}
	
	//接收清空画板消息
	public void clear() {
		// TODO Auto-generated method stub
		drawView.clear();
	}
}