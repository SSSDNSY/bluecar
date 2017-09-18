package com.car.demo;

import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.car.demo.R;;

/**
 *	测试蓝牙连接的类 
 * 		@author Penny
 *		2017年3月8日 20:57:47
 */
public class ConnDemo1 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conn);
		
		Button findbtn = (Button) findViewById(R.id.findBtn);
		findbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//1 获得BluetoothAdapter 对象
				BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter(); 
				//2判断当前设备中是否拥有蓝牙设备
				if(btadapter!=null){
					Toast.makeText(ConnDemo1.this, "本机拥有蓝牙,请点击", Toast.LENGTH_SHORT)
					.show();
					//3判断当前设备蓝牙设备是否已经打开
					if(!btadapter.isEnabled()){
					Intent opneBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivity(opneBt);
					Toast.makeText(ConnDemo1.this, "请连接蓝牙", Toast.LENGTH_SHORT)
					.show();
					}
					Set<BluetoothDevice> devices = btadapter.getBondedDevices();
					//4得到所有已经配对的蓝牙设备对象
					if(devices.size()>0){
						for(Iterator its = devices.iterator();its.hasNext();){
							BluetoothDevice  bd = (BluetoothDevice) its.next();
							Toast.makeText(ConnDemo1.this, 
									bd.getName()+"/"+bd.getAddress(), 
									Toast.LENGTH_SHORT)
									.show();;
						}
					}
					//没有蓝牙装置
				}else{
					Toast.makeText(ConnDemo1.this, "本机没有蓝牙装置，已退出", Toast.LENGTH_LONG)
					.show();
				}
				
			}
		});
		
	}
}
