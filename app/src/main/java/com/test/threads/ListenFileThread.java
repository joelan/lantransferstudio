package com.test.threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.networkbroard.bean.Datapacket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenFileThread extends Thread {
	ServerSocket socket=null;
	int port;
	String filepath;
	long receiveleng=0;
	public ListenFileThread( Handler handler,String path)
	{
		receiveleng=0;
		try {
			port=12346;
			socket=new ServerSocket(port);//监听本机的12345端口
			this.handler=handler;
			this.filepath=path;
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



				final Socket soc=socket.accept();//等待消息
				//recemsg(soc);
                receiveFile(soc,this.filepath);




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
	 * 接收消息
	 * @param soc
	 */
	private void recemsg(Socket soc)
	{
		Message msg=new Message();

		try {
			InputStream is = soc.getInputStream();//获取消息
			if (is != null) {

					/*
					之前的传输
					BufferedReader in=  new BufferedReader(new InputStreamReader(is,"UTF-8"));
					PrintWriter out = new PrintWriter(soc.getOutputStream());//输出信息
					String str="";
					str=in.readLine();*/


				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(soc.getInputStream()));
				Object obj = ois.readObject();
				Datapacket packet = (Datapacket) obj;

				String str = "";
				if (packet != null) {
					str = packet.getCommand();
				}

				if (str.equals("succed")) {
					msg.what = 2;
					msg.obj = packet;
					this.handler.sendMessage(msg);
				} else {
					msg.what = 1;
					msg.obj = packet;
					this.handler.sendMessage(msg);
				}

				Log.i("aaa", "removehost:" + soc.getInetAddress() + ":" + soc.getPort());

				soc.close();
			} else {
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


	/**
	 * 接收文件方法
	 * @param socket
	 * @throws IOException
	 */
	public  void receiveFile(Socket socket,String filepath) throws IOException {

		byte[] inputByte = null;
		int length = 0;
		DataInputStream dis = null;
		FileOutputStream fos = null;
		//String filePath = "D:/temp/"+GetDate.getDate()+"SJ"+new Random().nextInt(10000)+".zip";
		try {
			try {
				dis = new DataInputStream(socket.getInputStream());
				/*File f = new File("D:/temp");
				if(!f.exists()){
					f.mkdir();
				}*/
                /*
                 * 文件存储位置
                 */
				fos = new FileOutputStream(new File(filepath));
				inputByte = new byte[1024];
				//System.out.println("开始接收数据...");
				while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
					fos.write(inputByte, 0, length);
					receiveleng=receiveleng+length;
					Message msg=new Message();
					msg.what =4;
					msg.obj = receiveleng;
					this.handler.sendMessage(msg);

					fos.flush();
				}
				//System.out.println("完成接收："+filepath);

			} finally {
				if (fos != null)
					fos.close();
				if (dis != null)
					dis.close();
				if (socket != null)
					socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
