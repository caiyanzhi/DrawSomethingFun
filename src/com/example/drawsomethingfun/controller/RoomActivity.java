package com.example.drawsomethingfun.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.drawsomethingfun.Constants;
import com.example.drawsomethingfun.R;
import com.example.drawsomethingfun.model.GameQuestion;
import com.example.drawsomethingfun.service.MyReceiver;
import com.example.drawsomethingfun.utils.NetworkManager;
import com.example.drawsomethingfun.utils.Utils;
import com.example.drawsomethingfun.utils.Utils.Callback;
import com.example.drawsomethingfun.view.DrawView;
import com.example.drawsomethingfun.view.PaintDialog;
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
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author yanzhi
 * 对方断线直接退出，无考虑时间同步问题
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
	Paint mPaint = new Paint();
	
	//监听画笔改变
	public class PaintChangeListener implements
			PaintDialog.OnPaintChangedListener {
		public void onPaintChanged(Paint paint) {
			drawView.setPaint(paint);
			paint = mPaint;
		}
	}
	
	//选择画笔
	private void choosePaint() {
		PaintDialog dialog = new PaintDialog(RoomActivity.this);
		dialog.initDialog(dialog.getContext(), drawView.getPaint());
		dialog.setOnPaintChangedListener(new PaintChangeListener());
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preroom);
		context = this;
		drawView = (DrawView) findViewById(R.id.drawView);
		drawView.init();
		//获取房间角色:服务器or客户端
		roleType = getIntent().getIntExtra("ROLE", 0);
		
		networkmanager = NetworkManager.getInstance(getApplicationContext());
		//初始化游戏
		setGame(new DrawGame(context));
		
		//监听消息广播
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
			//客户端开启接收服务器ip的线程
			Utils.showLoadingDialog(context, "寻找房间中...", new Callback() {

				@Override
				public void onRun() {
					// TODO Auto-generated method stub
					Utils.closeLoadingDialog();
					RoomActivity.this.finish();
				}
			});
			networkmanager.startReceiveIp();
		} else {
			//服务器开启广播ip的线程和接收客户端连接的线程
			Utils.showLoadingDialog(context, "等待对手进入...", new Callback() {

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

	//接收画方的点并显示在画版上面
	public void drawPoint(Point p) {
		if(p.getLoopCnt() == game.getLoopCnt())
			drawView.receiceDraw(p);
	}

	public void initView() {
		mPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        
		((Button) findViewById(R.id.ready)).setText("准备");
		((Button) findViewById(R.id.ready)).setOnClickListener(this);
		((Button) findViewById(R.id.ready)).setBackground(getResources()
				.getDrawable(R.drawable.layoutbk_active));
		// ((Button)
		// findViewById(R.id.ready)).setTextColor(getResources().getColor(R.color.white));
		isReady = 0;
		setButtonOfDrawOff();
		isRuning = false;
		findViewById(R.id.answerLayout).setVisibility(View.GONE);
		game.reset();
	}
	
	StartLoop startLoop;
	private boolean isRuning;
	
	//开始每一轮游戏，一局共6轮，每人画三轮
	@SuppressWarnings("rawtypes")
	public void startGameLoop() {
		readyButtonOff();
		TextView tv = (TextView) findViewById(R.id.hind);
		((EditText) findViewById(R.id.editanswer)).setText("");
		game.setLoopOn(true);
		drawView.setPaint(mPaint);
		drawView.clear();
		Toast.makeText(context, "新一轮开始啦", Toast.LENGTH_SHORT).show();
		if (roleType == Constants.CLIENT) {
			//客户端， 只考虑play1
			game.setLoopCnt(game.getLoopCnt()+1);
			if (game.getPlay1().isTurn()) {
				tv.setText("题目:" + game.getQuestion().getAnswer());
				setButtonOfDrawOn();
				findViewById(R.id.answerLayout).setVisibility(View.GONE);
			} else {
				findViewById(R.id.answerLayout).setVisibility(View.VISIBLE);
				tv.setText(game.getQuestion().getHind() + "("
						+ game.getQuestion().getLength() + "字)");
				setButtonOfDrawOff();
			}
		} else {
			//服务端， 只考虑play2，并且每轮都要发送开始消息给play1
			game.startLoop();
			if (game.getPlay2().isTurn()) {
				setTurn(true);
				tv.setText("题目:" + game.getQuestion().getAnswer());
				setButtonOfDrawOn();
			} else {
				setTurn(false);
				tv.setText(game.getQuestion().getHind() + "("
						+ game.getQuestion().getLength() + "字)");
				setButtonOfDrawOff();
			}
		}
		drawView.setLoopCnt(game.getLoopCnt());
		//开启计时的操作
		
		startLoop = new StartLoop();
		startLoop.executeOnExecutor(Executors.newCachedThreadPool());
	}

	//下方按钮在非画画时的变化
	private void setButtonOfDrawOff() {
		// TODO Auto-generated method stub
		findViewById(R.id.answerLayout).setVisibility(View.VISIBLE);
		findViewById(R.id.pen).setOnClickListener(null);
		findViewById(R.id.clear).setOnClickListener(null);
		((Button) findViewById(R.id.pen)).setBackground(getResources()
				.getDrawable(R.drawable.layoutbk_disable));
		((Button) findViewById(R.id.clear)).setBackground(getResources()
				.getDrawable(R.drawable.layoutbk_disable));
	}

	//下方按钮在画画时的变化
	private void setButtonOfDrawOn() {
		// TODO Auto-generated method stub
		findViewById(R.id.answerLayout).setVisibility(View.GONE);
		findViewById(R.id.pen).setOnClickListener(this);
		findViewById(R.id.clear).setOnClickListener(this);
		((Button) findViewById(R.id.pen)).setBackground(getResources()
				.getDrawable(R.drawable.layoutbk_active));
		// ((Button)
		// findViewById(R.id.pen)).setTextColor(getResources().getColor(R.color.white));
		((Button) findViewById(R.id.clear)).setBackground(getResources()
				.getDrawable(R.drawable.layoutbk_active));
		// ((Button)
		// findViewById(R.id.clear)).setTextColor(getResources().getColor(R.color.white));

	}

	//在每一轮的游戏中时间的变化处理和结束后的操作
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
			//定时更新时间
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
				} else {
					isRuning = false;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//释放资源
		unregisterReceiver(myReceiver);
		networkmanager.release();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.pen:
			choosePaint();
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
			
			if(ans.equals(""))
			{
				return;
			}
			
			((EditText) findViewById(R.id.editanswer)).setText("");
			
			//客户端发送答案的逻辑
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
				
				//服务器端发送答案的逻辑
				if (ans.equals(game.getQuestion().getAnswer())) {
					game.setLoopOn(false);
					startLoop.cancel(true);
					game.sendResult();
					// 服务器开始下一轮
					if (game.getLoopCnt() != 6) {
						SystemClock.sleep(3000);
						startGameLoop();
					} else {
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
	
	//设置每一轮的问题
	public void setQuestion(GameQuestion question) {
		game.setQuestion(question);
	}

	public boolean isTurn() {
		return isTurn;
	}

	public void setTurn(boolean isTurn) {
		drawView.setDrawable(isTurn);
	}

	//客户端接收每一轮游戏开始的消息
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
		startGameLoop();//开始计时
	}

	private void readyButtonOff() {
		// TODO Auto-generated method stub
		((Button) findViewById(R.id.ready)).setText("游戏中");
		((Button) findViewById(R.id.ready)).setOnClickListener(null);
		((Button) findViewById(R.id.ready)).setBackground(getResources()
				.getDrawable(R.drawable.layoutbk_disable));
		isRuning = true;
	}

	//游戏过程中不允许推出TT
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (isRuning == true) {

		} else {
			finish();
		}
	}

	public void answer(Answer answer) {
		// TODO Auto-generated method stub
		//若答案正确，则终止计时
		if (answer.getAns().equals(game.getQuestion().getAnswer())) {
			game.setLoopOn(false);
			if(startLoop!=null)
			{
				startLoop.cancel(true);
			}
			// 服务器开始下一轮
			if (game.getLoopCnt() != 6) {
				SystemClock.sleep(3000);
				startGameLoop();
			} else {
				isRuning = false;
			}
		}
	}

	//客户端端接收每一轮的游戏结果
	public void receiveResult(Result result) {
		// TODO Auto-generated method stub
		if(startLoop!=null&&startLoop.isCancelled())
		{
			startLoop.cancel(true);
		}
		
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

	//客户端接收最后游戏结果
	public void receiveEnd(End end) {
		if (end.getIsWin() == 1) {
			Toast.makeText(context, "這一局你贏了耶", Toast.LENGTH_SHORT).show();
		} else if (end.getIsWin() == -1) {
			Toast.makeText(context, "這一局你掛了耶", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "這一局居然平了", Toast.LENGTH_SHORT).show();
		}

		startLoop.cancel(true);
		initView();//重新初始化房间
	}

	// 接收清空画板消息
	public void clear() {
		// TODO Auto-generated method stub
		drawView.clear();
	}
}