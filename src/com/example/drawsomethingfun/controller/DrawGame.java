package com.example.drawsomethingfun.controller;

import java.util.Random;

import com.example.drawsomethingfun.Constants;
import com.example.drawsomethingfun.R;
import com.example.drawsomethingfun.model.GameQuestion;
import com.example.drawsomethingfun.model.Player;
import com.example.drawsomethingfun.utils.NetworkManager;
import com.example.drawsomethingfun.utils.Utils;
import com.protobuftest.protobuf.GameProbuf.Game;
import com.protobuftest.protobuf.GameProbuf.Game.Begin;
import com.protobuftest.protobuf.GameProbuf.Game.End;
import com.protobuftest.protobuf.GameProbuf.Game.MsgType;
import com.protobuftest.protobuf.GameProbuf.Game.Question;
import com.protobuftest.protobuf.GameProbuf.Game.Result;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
/**
 * 
 * @author yanzhi
 * 改类主要由服务器玩家控制，主要负责游戏发送消息的逻辑和处理消息的逻辑
 * 而客户端玩家则在roomActivity中控制处理消息的逻辑，部分逻辑在此类中控制
 */

public class DrawGame {

	private GameQuestion question;
	public final int GAMETIME = 60;
	private int loopCnt = 0;
	private int restTime;
	private Player play1;// 客户端玩家
	private Player play2;// 服务端玩家
	Context context;
	private boolean isLoopOn = false;
	public DrawGame(Context context) {
		this.context = context;
		reset();
	}

	public void startLoop() {
		if (getLoopCnt() != 6) {
			if (play1.isReady() && getPlay2().isReady()) {
				setQuestion(getQuestion(context));
				Question q = Question.newBuilder()
						.setAnswer(getQuestion().getAnswer())
						.setSize(String.valueOf(getQuestion().getLength()))
						.setHind(getQuestion().getHind()).build();
				setLoopCnt(getLoopCnt() + 1);
				if(getLoopCnt() % 2 == 0)
				{
					play1.setTurn(true);
					play2.setTurn(false);
				}
				else
				{
					play1.setTurn(false);
					play2.setTurn(true);
				}
				setRestTime(GAMETIME);
				
				Begin begin = Begin.newBuilder()
						.setIsDraw(play1.isTurn() ? 1 : 0).setLoopCnt(getLoopCnt())
						.setQestion(q).setTtl(getRestTime()).build();
				Game game = Game.newBuilder().setType(MsgType.BEGIN).setBegin(begin).build();
				NetworkManager.sendMessage(game.toByteArray());
			}
			setLoopOn(true);
		}
	}
	
	public void sendResult()
	{
		isLoopOn = false;
		if(getLoopCnt() == 6)
		{
			//发送最终结果，先处理服务器端逻辑
			int isPlay1Win = 0;
			
			if(play1.getWinCnt() > getPlay2().getWinCnt() || (play1.getWinCnt() == getPlay2().getWinCnt() && play1.getTimeSpend() < getPlay2().getTimeSpend()))
			{
				isPlay1Win = 1;
				Toast.makeText(context, "這一局你掛了耶", 200).show();
			}
			else if(play1.getWinCnt() == getPlay2().getWinCnt() && play1.getTimeSpend() == getPlay2().getTimeSpend())
			{
				isPlay1Win = 0;
				Toast.makeText(context, "這一局居然平了", 200).show();
			}
			else
			{
				isPlay1Win = -1;
				Toast.makeText(context, "這一局你贏了耶", 200).show();
			}
			
			Intent i = new Intent();
			i.setAction(Constants.RESTARTGAME);
			context.sendBroadcast(i);
			//向客户端发送消息
			End end = End.newBuilder().setIsWin(isPlay1Win).setTimeSpend(play1.getTimeSpend()).setWinCount(play1.getWinCnt())
					.setHertimeSpend(getPlay2().getTimeSpend()).setHerwinCount(getPlay2().getWinCnt()).build();
			Game game = Game.newBuilder().setType(MsgType.END).setEnd(end).build();
			NetworkManager.sendMessage(game.toByteArray());
		}
		else if(getLoopCnt() != 0)
		{
			int isPlay1Win = 0;
			int timeSpend = 0;
			String answer = "";
			//发送每轮结果，先处理服务器端逻辑
			if(getRestTime()>0)
			{

				timeSpend = GAMETIME - getRestTime();
				if(play1.isTurn())
				{
					isPlay1Win = -1;
					Toast.makeText(context, "恭喜猜出,即将开始下一轮", 200).show();
					play2.setTimeSpend(timeSpend);
				}
				else
				{
					isPlay1Win = 1;
					Toast.makeText(context, "这一轮被猜出来啦,即将开始下一轮", 200).show();
					play1.setTimeSpend(timeSpend);
				}
				answer = getQuestion().getAnswer();
			}
			else
			{

				timeSpend = GAMETIME ;
				//超时
				if(play1.isTurn())
				{
					isPlay1Win = 1;
					Toast.makeText(context, "很遗憾没猜对,即将开始下一轮", 200).show();
					play2.setTimeSpend(timeSpend);
				}
				else
				{
					isPlay1Win = -1;
					Toast.makeText(context, "对方没猜对,即将开始下一轮", 200).show();
					play1.setTimeSpend(timeSpend);
				}
				answer = getQuestion().getAnswer();
			}
			//向客户端发送消息
			Result result = Result.newBuilder().setIsWin(isPlay1Win).setTimeSpend(timeSpend).setAnswer(answer).build();
			Game game = Game.newBuilder().setType(MsgType.RESULT).setResult(result).build();
			NetworkManager.sendMessage(game.toByteArray());
		}
	}

	// 重新开始
	public void reset() {
		setQuestion(null);
		play1 = new Player();
		setPlay2(new Player());

		play1.setReady(false);
		getPlay2().setReady(false);
		setLoopCnt(0);
	}

	public Player getPlay1() {
		return play1;
	}

	public void setPlay1(Player play1) {
		this.play1 = play1;
	}

	private GameQuestion getQuestion(Context context) {
		String[] answer = context.getResources().getStringArray(R.array.answer);
		String[] hind = context.getResources().getStringArray(R.array.hind);

		int size = answer.length;
		Random rand = new Random();
		int index = rand.nextInt(size);
		GameQuestion question = new GameQuestion(hind[index], answer[index],
				answer[index].length());
		return question;
	}

	public Player getPlay2() {
		return play2;
	}

	public void setPlay2(Player play2) {
		this.play2 = play2;
	}

	public boolean ready() {
		// TODO Auto-generated method stub
		return (play2.isReady() && play1.isReady());
	}

	public boolean isLoopOn() {
		// TODO Auto-generated method stub
		return isLoopOn;
	}

	public int getLoopCnt() {
		return loopCnt;
	}

	public void setLoopCnt(int loopCnt) {
		this.loopCnt = loopCnt;
	}

	public GameQuestion getQuestion() {
		return question;
	}

	public void setQuestion(GameQuestion question) {
		this.question = question;
	}

	public int getRestTime() {
		return restTime;
	}

	public void setRestTime(int restTime) {
		this.restTime = restTime;
	}

	public void setLoopOn(boolean isLoopOn) {
		this.isLoopOn = isLoopOn;
	}

}
