package com.car.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 
 * @author Penny
 * 2017年3月13日 10:46:43
 * 蓝牙玩具小车项目；
 * Android端 APP 
 * 主要功能：
 * 1 连接小车蓝牙模块，
 * 2 并能对小车 进行 前后左右 方向控制。
 */
/**--可发送字段
case 0x31:  P1=0xfe;beep=0;break;  //接受到1，第一个LED亮         
case 0x32:  P1=0xfd;beep=0;break;  //接受到2，第二个LED亮        
case 0x33:  P1=0xfb;beep=0;break;  //接受到3，第三个LED亮        
case 0x34:  P1=0xf7;beep=0;break;  //接受到4，第四个LED亮       
case 0x35:  P1=0xef;beep=0;break;  //接受到5，第五个LED亮            
case 0x36:  P1=0xdf;beep=0;break;  //接受到6，第六个LED亮                   
case 0x37:  P1=0xbf;beep=0;break;  //接受到7，第七个LED亮
case 0x38:  P1=0x7f;beep=0;break;  //接受到8，第八个LED亮
*/
public class CarControl extends Activity{

	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	private final static String ADDRESS ="98:D3:35:00:D0:5C";						//蓝牙模块  mac number
    
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    
    private BluetoothAdapter _bluetooth = null;    //获取本地蓝牙适配器，即蓝牙设备
    private OutputStream os=null;
    
//    private final static Handler handler =new Handler();//消息延时处理!!!!此方法存在内存泄漏
//    static TimerTask task =null; 
	 TextView msg =  null;
    /**
     * 以上两种方式比较简单，但是需要很多的图片和布局文件，如果项目中的图片按钮比较多，那就很浪费资源。第三种方式使用矩阵颜色滤镜。 
	颜色过滤矩阵是一个4x5的矩阵，四行分别是红色通道值，绿色通道值，蓝色通道值和alpha通道值。五列分别是对应通道的红色值，绿色值，蓝色值，alpha值和偏移量。 
	RGB和Alpha的终值计算方法如下： 
	Red通道终值= a[0] * srcR + a[1] * srcG + a[2] * srcB + a[3] * srcA + a[4] 
	Green通道终值= a[5] * srcR + a[6] * srcG + a[7] * srcB + a[8] * srcA + a[9] 
	Blue通道终值= a[10] * srcR + a[11] * srcG + a[12] * srcB + a[13] * srcA + a[14] 
	Alpha通道终值= a[15] * srcR + a[16] * srcG + a[17] * srcB + a[18] * srcA + a[19] 
	备注： 
	srcR为原图Red通道值，srcG为原图Green通道值，srcB为原图Blue通道值，srcA为原图Alpha通道值。 
	每个通道的源值和终值都在0到255的范围内。即使计算结果大于255或小于0，值都将被限制在0到255的范围内。 
     */
    /**   
    * 按钮被按下   
    */ 
    private final static float[] BUTTON_PRESSED = new float[] {       
    	2.0f, 0, 0, 0, -50,       
       0, 2.0f, 0, 0, -50,       
       0, 0, 2.0f, 0, -50,       
       0, 0, 0, 0.4f, 0 };     
   /**   
    * 按钮恢复原状   
    */     
    private final static float[] BUTTON_RELEASED = new float[] {       
       1, 0, 0, 0, 0,       
       0, 1, 0, 0, 0,       
       0, 0, 1, 0, 0,       
       0, 0, 0, 1, 0 };   
	
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/**
		 * 界面按键获取
		 */
		setContentView(R.layout.ctl);
		ImageButton up = (ImageButton) findViewById(R.id.up);
		ImageButton down = (ImageButton) findViewById(R.id.down);
		ImageButton left = (ImageButton) findViewById(R.id.left);
		ImageButton right = (ImageButton) findViewById(R.id.right);
		ImageButton stop = (ImageButton) findViewById(R.id.stop);
		
		ImageButton conn = (ImageButton) findViewById(R.id.conn);
		ImageButton close = (ImageButton) findViewById(R.id.close);
		ImageButton btnA = (ImageButton) findViewById(R.id.a);
		ImageButton btnB = (ImageButton) findViewById(R.id.b);
		
		
		_bluetooth= BluetoothAdapter.getDefaultAdapter();
		
		  if (_bluetooth != null){
//	        Toast.makeText(this,"已发现:\n"+ _bluetooth.getName()+"\n"+_bluetooth.getAddress()+"\n设备！", Toast.LENGTH_SHORT).show();
			  delayMsg("已发现:"+ _bluetooth.getName()+","+_bluetooth.getAddress()+"设备！", 3000);//3秒后执行清空信息提示
	        }else{
	        	if(!_bluetooth.isEnabled())
	        		return;
	        }
		// 设置设备可以被搜索  
	       new Thread(){
	    	   public void run(){
	    		   if(!_bluetooth.isEnabled()){
	        		_bluetooth.enable();
	    		   }
	    	   }   	   
	       }.start();
	       
	       delayMsg("请点击蓝牙连接按钮！", 3000);//3秒后执行清空信息提示
	       /**
		    *连接按键
	        */
	     conn.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_DOWN) { 
					    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
					    v.setBackgroundDrawable(v.getBackground()); 
					   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
					    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
					    v.setBackgroundDrawable(v.getBackground()); 
					   }
				//-----------------------准备连接-------------------
				if(_bluetooth!=null&&_socket==null){
					  _device = _bluetooth.getRemoteDevice(ADDRESS); //由Mac 地址获取小车蓝牙设备
					  try {
						_socket = _device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
					  } catch (IOException e) {
//						Toast.makeText(CarControl.this, "蓝牙模块获取Socket失败！", Toast.LENGTH_SHORT).show();
//						  msg.setText("蓝牙模块获取Socket失败！");
						  delayMsg("蓝牙模块获取Socket失败！", 3000);
					}
				  	}
				//---------------连接------------------------------
				try {
					if(_socket!=null&&!_socket.isConnected())
					_socket.connect();
					if(_socket.isConnected()&&os==null)
					 os= _socket.getOutputStream();
//					Toast.makeText(CarControl.this, "socket成功连接", Toast.LENGTH_SHORT).show();
					delayMsg("socket成功连接", 3000);
				} catch (IOException e) {
					Toast.makeText(CarControl.this, "socket连接失败，程序已退出！", Toast.LENGTH_SHORT).show();
//					delayMsg("socket连接失败！程序已退出", 3000);
					try {
						if(_socket!=null)
						_socket.close();
						_socket=null;
						if(_bluetooth!=null)
							_bluetooth.disable();
							_bluetooth=null;
						finish();
					} catch (IOException e1) {
//						Toast.makeText(CarControl.this, "socket关闭失败！程序已退出", Toast.LENGTH_SHORT).show();
//						delayMsg("socket连接失败！程序已退出", 3000);
					}
				}
				return false;
			}
		});
	     /**
	      * 关闭按键
	      */
	     close.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				finish();
				if(_bluetooth!=null){
					if(_bluetooth.isDiscovering()){
						_bluetooth.cancelDiscovery();
					}
						if(_bluetooth.isEnabled()){
						_bluetooth.disable();
						_bluetooth=null;
						}
				}
				if(_socket!=null){
					try {
						_socket.close();
						_socket=null;
					} catch (IOException e) {
					}finally{
						if(os!=null){
							try {
								os.close();
							} catch (IOException e) {
							}
						}
					}
				}
				return false;
			}
		});  
	     /**
	      * 上下左右  1234
	      */
		//向上方向键 id=1
		up.setOnTouchListener(new OnTouchListener()  {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
			try {
				if(_socket!=null&&_socket.isConnected()&&os!=null){
				
				byte[] code1= new byte[]{0x33};
				os.write(code1);
//				Toast.makeText(CarControl.this, "前进指令（1）！", Toast.LENGTH_SHORT).show();
				delayMsg("↑前进↑", 1000);
				}else{
//					Toast.makeText(CarControl.this, "没有连接上小车蓝牙", Toast.LENGTH_SHORT).show();
//					msg.setText("没有连接上小车蓝牙!");
					delayMsg("没有连接上小车蓝牙！", 2000);
					
				}
			} catch (IOException e) {
//				Toast.makeText(CarControl.this, "前进指令（1）发送失败！", Toast.LENGTH_SHORT).show();
				delayMsg("前进指令（1）发送失败！", 2000);
			}
			return false;
			}
		});
		//向下方向键id=3
		down.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				try {
					if(_socket!=null&&_socket.isConnected()&&os!=null){
					
					byte[] code3= new byte[]{0x34};
					os.write(code3);
//					Toast.makeText(CarControl.this, "后退指令（3）！", Toast.LENGTH_SHORT).show();
					delayMsg("↓后退↓", 1000);
					}else{
//						Toast.makeText(CarControl.this, "没有连接上小车蓝牙", Toast.LENGTH_SHORT).show();
						delayMsg("没有连接上小车蓝牙！", 2000);
					}
				} catch (IOException e) {
//					Toast.makeText(CarControl.this, "后退指令（3）发送失败！", Toast.LENGTH_SHORT).show();
					delayMsg("后退指令（3）发送失败！", 2000);
				}
				return false;
			}
		});
		//向左方向键 id=4
		left.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				try {
					if(_socket!=null&&_socket.isConnected()&&os!=null){
					
					byte[] code4= new byte[]{0x31};
					os.write(code4);
//					Toast.makeText(CarControl.this, "左转指令（4）！", Toast.LENGTH_SHORT).show();
					delayMsg("←左转←", 1000);
					}else{
//						Toast.makeText(CarControl.this, "没有连接上小车蓝牙", Toast.LENGTH_SHORT).show();
						delayMsg("没有连接上小车蓝牙！", 2000);
					}
				} catch (IOException e) {
//					Toast.makeText(CarControl.this, "左转指令（4）发送失败！", Toast.LENGTH_SHORT).show();
					delayMsg("左转指令（4）发送失败！", 2000);
				}
				return false;
			}
		});
		//向右方向键 id=2
		right.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				try {
					if(_socket!=null&&_socket.isConnected()&&os!=null){
					
					byte[] code2= new byte[]{0x32};
					os.write(code2);
//					Toast.makeText(CarControl.this, "右转指令（2）！", Toast.LENGTH_SHORT).show();
					delayMsg("→右转→", 1000);
					}else{
//						Toast.makeText(CarControl.this, "没有连接上小车蓝牙!", Toast.LENGTH_SHORT).show();
						delayMsg("没有连接上小车蓝牙！", 2000);
					}
				} catch (IOException e) {
//					Toast.makeText(CarControl.this, "右转指令（2）发送失败！", Toast.LENGTH_SHORT).show();
					delayMsg("右转指令（2）发送失败！", 2000);
				}
				return false;
			}
		});
		//stop 
		stop.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				try {
					if(_socket!=null&&_socket.isConnected()&&os!=null){
					
					byte[] code2= new byte[]{0x35};
					os.write(code2);
//					Toast.makeText(CarControl.this, "右转指令（2）！", Toast.LENGTH_SHORT).show();
					delayMsg("A", 1000);
					}else{
//						Toast.makeText(CarControl.this, "没有连接上小车蓝牙!", Toast.LENGTH_SHORT).show();
						delayMsg("没有连接上小车蓝牙！", 2000);
					}
				} catch (IOException e) {
//					Toast.makeText(CarControl.this, "右转指令（2）发送失败！", Toast.LENGTH_SHORT).show();
					delayMsg("A发送失败！", 2000);
				}
				return false;
			}
		});
		/***
		 * A-B 功能键
		 */
		//A
		btnA.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				try {
					if(_socket!=null&&_socket.isConnected()&&os!=null){
					
					byte[] code2= new byte[]{0x36};
					os.write(code2);
//					Toast.makeText(CarControl.this, "右转指令（2）！", Toast.LENGTH_SHORT).show();
					delayMsg("A", 1000);
					}else{
//						Toast.makeText(CarControl.this, "没有连接上小车蓝牙!", Toast.LENGTH_SHORT).show();
						delayMsg("没有连接上小车蓝牙！", 2000);
					}
				} catch (IOException e) {
//					Toast.makeText(CarControl.this, "右转指令（2）发送失败！", Toast.LENGTH_SHORT).show();
					delayMsg("A发送失败！", 2000);
				}
				return false;
			}
		});
		//B
		btnB.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }else if(event.getAction() == MotionEvent.ACTION_UP) { 
				    v.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_RELEASED)); 
				    v.setBackgroundDrawable(v.getBackground()); 
				   }
				try {
					if(_socket!=null&&_socket.isConnected()&&os!=null){
					
					byte[] code2= new byte[]{0x37};
					os.write(code2);
//					Toast.makeText(CarControl.this, "右转指令（2）！", Toast.LENGTH_SHORT).show();
					delayMsg("B", 1000);
					}else{
//						Toast.makeText(CarControl.this, "没有连接上小车蓝牙!", Toast.LENGTH_SHORT).show();
						delayMsg("没有连接上小车蓝牙！", 2000);
					}
				} catch (IOException e) {
//					Toast.makeText(CarControl.this, "右转指令（2）发送失败！", Toast.LENGTH_SHORT).show();
					delayMsg("B发送失败！", 2000);
				}
				return false;
			}
		});
	}

	private void delayMsg(final String msgtext,int times){
		msg=(TextView) findViewById(R.id.ctlmsg);
				msg.setText(msgtext);
	}
}
