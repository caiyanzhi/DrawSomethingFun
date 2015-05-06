package com.example.drawsomethingfun.model;
/**
 * 
 * @author yanzhi
 *
 */

public class GameQuestion {
	String hind;
	String answer;
	int length;
	
	public GameQuestion(String hind, String answer, int length) {
		super();
		this.hind = hind;
		this.answer = answer;
		this.length = length;
	}
	public String getHind() {
		return hind;
	}
	public void setHind(String hind) {
		this.hind = hind;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}
