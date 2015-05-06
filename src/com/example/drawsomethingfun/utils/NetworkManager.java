package com.example.drawsomethingfun.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.example.drawsomethingfun.Constants;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * 
 * @author yanzhi
 *
 */

public class NetworkManager {
	WifiManager.MulticastLock lock;
	Context context;
	ServerSocket serivce = null;
	static Socket socket = null;

	private boolean isSendingBroadcast = true;
	private boolean isFinishBroadcast = false;
	private boolean isFinish = false;

	public NetworkManager(Context context) {
		this.context = context;
		WifiManager manager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		lock = manager.createMulticastLock("test wifi");
	}

	static NetworkManager instance;

	static public NetworkManager getInstance(Context context) {
		if (instance == null) {
			instance = new NetworkManager(context);
		}
		return instance;
	}

	/*
	 * 釋放線程和資源
	 */
	public void release() {
		context = null;
		isFinish = true;
		instance = null;
		isSendingBroadcast = false;
		isFinishBroadcast = true;
		if (ms != null) {
			ms.close();
			ms = null;
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket = null;
		}
		if (serivce != null) {
			try {
				serivce.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			serivce = null;
		}
	}

	private class SocketSeverThread implements Runnable {
		@Override
		public void run() {
			{
				// 等待客户端连接
				while (true) {
					if (serivce == null) {
						try {
							serivce = new ServerSocket(30001);
							Socket socket = serivce.accept();
							Intent intent = new Intent();
							intent.setAction(Constants.CANCEL_LOADING);
							context.sendBroadcast(intent);
							System.out.println("conneted");
							// 停止廣播線程
							isFinishBroadcast = true;
							new Thread(new ServieRead(socket)).start();

							serivce.close();
							serivce = null;
							Log.v("cyz", "cyz release port");
							break;
						} catch (IOException e) {
							e.printStackTrace();
							Log.e("cyz", "cyz accept timeout");
						}
					}
					if (isFinish) {
						Log.v("cyz", "cyz release port");
						break;
					}
				}
			}
		}
	}

	private class ServieRead implements Runnable {
		public ServieRead(Socket socket) {
			NetworkManager.socket = socket;
		}

		@Override
		public void run() {
			Intent intent = new Intent();
			while (true) {
				if (socket != null) {
					try {
						InputStream input = NetworkManager.socket
								.getInputStream();
						byte[] bytes = new byte[1024];
						int result = input.read(bytes, 0, 4);
						if (result == -1) {
							Log.v("cyz", "cyz read stream end");
							input.close();
							intent.setAction(Constants.SOCKET_DISCONNECT);
							context.sendBroadcast(intent);
							break;
						}

						int len = byteArrayToInt(bytes);
						Log.v("cyz", "cyz" + len);

						byte[] temp = new byte[len];
						input.read(temp, 0, len);

						intent.setAction(Constants.RECEIVEMSG);
						intent.putExtra("message", temp);
						context.sendBroadcast(intent);
						// BufferedReader bff = new BufferedReader(
						// new InputStreamReader(input));
						// //获取客户端的信息
						// String line = "cyz\n";
						// while ((line = bff.readLine()) != null) {
						// Log.v("receive from client","cyz"+line);
						// Intent intent = new Intent();
						//
						// intent.setAction(Constants.RECEIVE_FROM_CLIENT);
						// intent.putExtra("data", line);
						// context.sendBroadcast(intent);
						// if(isFinish)
						// {
						// if(socket !=null)
						// {
						// try {
						// socket.close();
						// } catch (IOException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						// socket = null;
						// }
						// break;
						// }
						// }
						// bff.close();
						//
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (isFinish) {
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						socket = null;
					}

					break;
				}
			}
		}
	}

	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		// 由高位到低位
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}

	public static int byteArrayToInt(byte[] bytes) {
		int value = 0;
		// 由高位到低位
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;// 往高位游
		}
		return value;
	}

	static public void sendMessage(final byte[] bytes) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.v("cyz", "cyz send");
				if (socket != null) {
					try {
						// PhoneNumber pn =
						// PhoneNumber.newBuilder().setNumber("123455")
						// .setType(PhoneType.MOBILE).build();
						// PhoneNumber pn1 =
						// PhoneNumber.newBuilder().setNumber("9999999")
						// .setType(PhoneType.HOME).build();
						//
						// Person p =
						// Person.newBuilder().setName("cyz").setId(123).addPhone(pn).addPhone(pn1).build();
						//
						// socket.setTcpNoDelay(true);
						// com.protobuftest.protobuf.PersonProbuf.Header header
						// =
						// com.protobuftest.protobuf.PersonProbuf.Header.newBuilder().setLength(11).build();
						// AddressBook addrbook =
						// AddressBook.newBuilder().setHeader(header).addPerson(p).build();
						// byte[] bytes = addrbook.toByteArray();
						//
						int len = bytes.length;
						// socket.getOutputStream().flush();
						OutputStream output = socket.getOutputStream();

						OutputStreamWriter osWrite = new OutputStreamWriter(
								output);
						// AddressBook a = AddressBook.parseFrom(bytes);
						// Log.v("cyz","cyz+mess"+a.getHeader().getLength());

						// BufferedWriter bufWrite = new
						// BufferedWriter(osWrite);
						output.write(intToByteArray(len), 0, 4);
						output.write(bytes, 0, len);
						output.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private class SocketClientThread implements Runnable {
		String ip = "";

		public SocketClientThread(String ip) {
			this.ip = ip;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Socket socket = new Socket();
			try {
				Log.v("cyz", "cyz connect");

				socket.connect(new InetSocketAddress(ip, 30001), 5000);
				Intent intent = new Intent();
				intent.setAction(Constants.CANCEL_LOADING);
				context.sendBroadcast(intent);
				NetworkManager.socket = socket;
				Log.v("cyz", "cyz connect server");
				// 客户端接收消息
				while (true) {
					InputStream input = socket.getInputStream();
					byte[] bytes = new byte[1024];
					int result = input.read(bytes, 0, 4);
					if (result == -1) {
						Log.v("cyz", "cyz read stream end");
						input.close();

						intent.setAction(Constants.SOCKET_DISCONNECT);
						context.sendBroadcast(intent);
						break;
					}

					int len = byteArrayToInt(bytes);
					Log.v("cyz", "cyz" + len);

					byte[] temp = new byte[len];
					input.read(temp, 0, len);
					intent.setAction(Constants.RECEIVEMSG);
					intent.putExtra("message", temp);
					context.sendBroadcast(intent);
					// AddressBook addrbook = AddressBook.parseFrom(temp);
					// String msg =
					// "cyz count = "+addrbook.getPersonCount()+" phone = "+addrbook.getPerson(0).getName();
					// Log.v("","cyz"+msg);
					if (isFinish) {
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/* 发送广播端的socket */
	private DatagramSocket ms;
	// 发送的数据包，局网内的所有地址都可以收到该数据包
	DatagramPacket dataPacket = null;

	// 廣播ip
	public void startBroadCastIp() {
		new Thread(new BroadcastIpThread()).start();
	}

	// 接收ip廣播
	public void startReceiveIp() {
		new Thread(new ReceiveIpThread()).start();
	}

	// 開啟接收連接的線程
	public void startReceiveConnect() {
		new Thread(new SocketSeverThread()).start();
	}

	private class BroadcastIpThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub

			while (true) {
				if (isFinishBroadcast || !isSendingBroadcast)
					break;

				if (ms == null) {
					try {
						ms = new DatagramSocket();
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (isSendingBroadcast && ms != null) {
					// 1s发送一次广播帧
					try {
						if (dataPacket == null) {

							// 将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
							byte[] data = Utils.getIpAddress(context)
									.getBytes();
							// 224.0.0.1为广播地址
							InetAddress address = InetAddress
									.getByName("255.255.255.255");
							// 这个地方可以输出判断该地址是不是广播类型的地址
							System.out.println(address.isMulticastAddress());
							dataPacket = new DatagramPacket(data, data.length,
									address, 31122);
						}
						Log.v("", "cyz broadcast");
						lock.acquire();
						ms.send(dataPacket);
						lock.release();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch blocku
					e.printStackTrace();
				}
			}
		}
	}

	private class ReceiveIpThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub

			while (true) {
				if (isFinishBroadcast)
					break;
				if (ms == null) {
					// ms = new DatagramSocket();
					try {
						ms = new DatagramSocket(31122);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (isSendingBroadcast && ms != null) {
					try {
						Log.v("", "cyz receive");
						byte buf[] = new byte[4096];
						DatagramPacket dp = new DatagramPacket(buf, 4096);
						lock.acquire();
						ms.setSoTimeout(5000);
						ms.receive(dp);
						lock.release();
						final String strMsg = new String(dp.getData()).trim();

						Log.v("", "cyz receive broadcast " + strMsg);
						// 開啟線程連接服務器
						new Thread(new SocketClientThread(strMsg)).start();
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
