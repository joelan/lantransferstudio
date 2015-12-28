package com.test.threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.networkbroard.bean.Datapacket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenThread extends Thread {
	ServerSocket socket=null;
	public ListenThread(int port, Handler handler)
	{
		try {
			port=12345;
			socket=new ServerSocket(port);//监听本机的12345端口
			this.handler=handler;
		} catch (IOException e) {
			closethread();
			Log.d("aaa", "ListenThread ServerSocket init() has exception");
		}
		catch(Exception e)
		{

			e.printStackTrace();
		}

	}
	Handler handler;
	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {

				Message msg=new Message();

				final Socket soc=socket.accept();//等待消息
				InputStream is=soc.getInputStream();//获取消息
			

				if (is!=null) {

					/*
					之前的传输
					BufferedReader in=  new BufferedReader(new InputStreamReader(is,"UTF-8"));
					PrintWriter out = new PrintWriter(soc.getOutputStream());//输出信息
					String str="";
					str=in.readLine();*/


					ObjectInputStream ois = new ObjectInputStream(
							new BufferedInputStream(soc.getInputStream()));
					Object obj = ois.readObject();
					Datapacket packet= (Datapacket) obj;


					String str="";
					if(packet!=null)
					{
						str=packet.getCommand();
					}

					//连接成功。
					if(str.equals("succed"))
					{
						msg.what=2;
						msg.obj=packet;
						this.handler.sendMessage(msg);
					}
					//接收文件信息
					else if(str.equals("filemsg"))
					{
						Log.i("listener","filemsg tag");
						msg.what=3;
						msg.obj=packet;
						this.handler.sendMessage(msg);
					}
					else
					{
						//一般消息
						msg.what=1;
						msg.obj=packet;
						this.handler.sendMessage(msg);
					}

					Log.i("aaa", "removehost:"+soc.getInetAddress()+":"+soc.getPort());

					soc.close();
				}else
				{
					Log.d("aaa", "没有接收到数据");
				}



			}


			catch (IOException e) {
				Log.d("aaa", "ListenThread.run() -->final Socket soc=socket.accept();has exception");
				closethread();
			}
			catch(Exception e)
			{
				Log.d("listener", e.toString());
				closethread();
			}

		}
	}


	/**
	 * 关闭线程
	 */
	public void closethread()
	{
		this.interrupt();

		clocesocket();
	}

	/**
	 * 关闭连接
	 */
	public void clocesocket()
	{
		if(socket!=null)
		{
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}
	

}
