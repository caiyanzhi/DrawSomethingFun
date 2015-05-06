package com.example.drawsomethingfun.model;
/**
 * 
 * @author yanzhi
 *
 */

public class Player {
	boolean isTurn=false;
	int winCnt=0;
	int timeSpend=0;
	boolean isReady = false;
	
	public boolean isReady() {
		return isReady;
	}
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	public boolean isTurn() {
		return isTurn;
	}
	public void setTurn(boolean isTurn) {
		this.isTurn = isTurn;
	}
	public int getWinCnt() {
		return winCnt;
	}
	public void setWinCnt(int winCnt) {
		this.winCnt = winCnt;
	}
	public int getTimeSpend() {
		return timeSpend;
	}
	public void setTimeSpend(int timeSpend) {
		this.timeSpend = timeSpend;
	}
}
