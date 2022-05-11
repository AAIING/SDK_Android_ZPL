package com.android.zpl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ZPL.IPort;
import ZPL.PublicFunction;
import ZPL.ZPLPrinterHelper;


public class Activity_Main extends Activity 
{
	private Context thisCon=null;
	private BluetoothAdapter mBluetoothAdapter;
	private PublicFunction PFun=null;
	private PublicAction PAct=null;
	
	private Button btnWIFI=null;
	private Button btnBT=null;
	private Button btnUSB=null;
	
	private Spinner spnPrinterList=null;
	private TextView txtTips=null;
	private Button btnOpenCashDrawer=null;
	private Button btnSampleReceipt=null;	
	private Button btn1DBarcodes=null;
	private Button btnQRCode=null;
	private Button btnPDF417=null;
	private Button btnCut=null;
	private Button btnPageMode=null;
	private Button btnImageManage=null;
	private Button btnGetRemainingPower=null;
	
	private EditText edtTimes=null;
	
	private ArrayAdapter arrPrinterList; 
	private String ConnectType="";
	private String PrinterName="";
	private String PortParam="";
	
	private UsbManager mUsbManager=null;	
	private UsbDevice device=null;
	private static final String ACTION_USB_PERMISSION = "com.HPRTSDKSample";
	private PendingIntent mPermissionIntent=null;
	private static IPort Printer=null;
	private ZPLPrinterHelper zplPrinterHelper;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE" };
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(BuildConfig.VERSION_NAME);
		try
		{
			thisCon=this.getApplicationContext();
			
			btnWIFI = (Button) findViewById(R.id.btnWIFI);
			btnUSB = (Button) findViewById(R.id.btnUSB);
			btnBT = (Button) findViewById(R.id.btnBT);
			
			//edtTimes = (EditText) findViewById(R.id.edtTimes);
			
			spnPrinterList = (Spinner) findViewById(R.id.spn_printer_list);	
			txtTips = (TextView) findViewById(R.id.txtTips);
			btnSampleReceipt = (Button) findViewById(R.id.btnSampleReceipt);
			btnOpenCashDrawer = (Button) findViewById(R.id.btnOpenCashDrawer);
			btn1DBarcodes = (Button) findViewById(R.id.btn1DBarcodes);
			btnQRCode = (Button) findViewById(R.id.btnQRCode);
			btnPDF417 = (Button) findViewById(R.id.btnPDF417);
			btnCut = (Button) findViewById(R.id.btnCut);
			btnPageMode = (Button) findViewById(R.id.btnPageMode);
			btnImageManage = (Button) findViewById(R.id.btnImageManage);

			mPermissionIntent = PendingIntent.getBroadcast(thisCon, 0, new Intent(ACTION_USB_PERMISSION), 0);
	        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			thisCon.registerReceiver(mUsbReceiver, filter);
			
			PFun=new PublicFunction(thisCon);
			PAct=new PublicAction(thisCon);
			InitSetting();
			InitCombox();
			this.spnPrinterList.setOnItemSelectedListener(new OnItemSelectedPrinter());
			//Enable Bluetooth
			EnableBluetooth();
			zplPrinterHelper = ZPLPrinterHelper.getZPL(thisCon);
		}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onCreate ")).append(e.getMessage()).toString());
		}
	}
	
	private void InitSetting()
	{
		String SettingValue="";
		SettingValue=PFun.ReadSharedPreferencesData("Codepage");
		if(SettingValue.equals(""))		
			PFun.WriteSharedPreferencesData("Codepage", "0,PC437(USA:Standard Europe)");			
		
		SettingValue=PFun.ReadSharedPreferencesData("Cut");
		if(SettingValue.equals(""))		
			PFun.WriteSharedPreferencesData("Cut", "0");	
			
		SettingValue=PFun.ReadSharedPreferencesData("Cashdrawer");
		if(SettingValue.equals(""))			
			PFun.WriteSharedPreferencesData("Cashdrawer", "0");
					
		SettingValue=PFun.ReadSharedPreferencesData("Buzzer");
		if(SettingValue.equals(""))			
			PFun.WriteSharedPreferencesData("Buzzer", "0");
					
		SettingValue=PFun.ReadSharedPreferencesData("Feeds");
		if(SettingValue.equals(""))			
			PFun.WriteSharedPreferencesData("Feeds", "0");				
	}
	
	//add printer list
	private void InitCombox()
	{
		try
		{
			arrPrinterList = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
			String strSDKType=thisCon.getString(R.string.sdk_type);
			if(strSDKType.equals("all"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_zpl, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("hprt"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_hprt, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mkt"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mkt, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mprint"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mprint, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("sycrown"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_sycrown, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mgpos"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mgpos, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("ds"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_ds, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("cst"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_cst, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("other"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_other, android.R.layout.simple_spinner_item);
			arrPrinterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			PrinterName=arrPrinterList.getItem(0).toString();
			spnPrinterList.setAdapter(arrPrinterList);
		}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> InitCombox ")).append(e.getMessage()).toString());
		}
	}
	
	private class OnItemSelectedPrinter implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{

			PrinterName=arrPrinterList.getItem(arg2).toString();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	
	//EnableBluetooth
	private boolean EnableBluetooth()
    {
        boolean bRet = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null)
        {
            if(mBluetoothAdapter.isEnabled())
                return true;
            mBluetoothAdapter.enable();
            try 
    		{
    			Thread.sleep(500);
    		} 
    		catch (InterruptedException e) 
    		{			
    			e.printStackTrace();
    		}
            if(!mBluetoothAdapter.isEnabled())
            {
                bRet = true;
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK");
            }
        } 
        else
        {
        	Log.d("HPRTSDKSample", (new StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }
	
	//call back by scan bluetooth printer
	@Override  
  	protected void onActivityResult(int requestCode, int resultCode, final Intent data)
  	{  
  		try
  		{  		
  			String strIsConnected;
	  		switch(resultCode)
	  		{
	  			case ZPLPrinterHelper.ACTIVITY_CONNECT_BT:
	  				String strBTAddress="";
	  				strIsConnected=data.getExtras().getString("is_connected");
	  	        	if (strIsConnected.equals("NO"))
	  	        	{
	  	        		txtTips.setText(thisCon.getString(R.string.activity_main_scan_error));	  	        		
  	                	return;
	  	        	}
	  	        	else
	  	        	{	  	        		
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connected));
	  					return;
	  	        	}		  	        	
	  			case ZPLPrinterHelper.ACTIVITY_CONNECT_WIFI:
					strIsConnected=data.getExtras().getString("is_connected");
					if (strIsConnected.equals("NO")) {
						txtTips.setText(thisCon.getString(R.string.activity_main_scan_error));
						return;
					}
					else {
						txtTips.setText(thisCon.getString(R.string.activity_main_connected));
						return;
					}
				case ZPLPrinterHelper.ACTIVITY_IMAGE_FILE:
					final ProgressDialog progressDialog = new ProgressDialog(this);
					progressDialog.setMessage(getString(R.string.activity_main_please_wait));
					progressDialog.show();
					new Thread(){
						@Override
						public void run() {
							super.run();
							try{
								String strImageFile=data.getExtras().getString("FilePath");
								Bitmap bmp = BitmapFactory.decodeFile(strImageFile);
								zplPrinterHelper.start();
								zplPrinterHelper.printBitmap("100", "100", bmp);
								zplPrinterHelper.end();
								progressDialog.dismiss();
							}catch (Exception e){
								progressDialog.dismiss();
							}
						}
					}.start();
	  				return;
	  			case ZPLPrinterHelper.ACTIVITY_PRNFILE:
	  				String strPRNFile=data.getExtras().getString("FilePath");
					zplPrinterHelper.PrintBinaryFile(strPRNFile);
	  				return;
  			}
  		}
  		catch(Exception e)
  		{
  			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onActivityResult ")).append(e.getMessage()).toString());
  		}
        super.onActivityResult(requestCode, resultCode, data);  
  	} 
	
	@SuppressLint("NewApi")
	public void onClickConnect(View view) 
	{		
    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
	    	if(zplPrinterHelper!=null)
			{
				zplPrinterHelper.PortClose();
			}
			
	    	if(view.getId()==R.id.btnBT)
	    	{
				if (Build.VERSION.SDK_INT >= 23) {
					//校验是否已具有模糊定位权限
					if (ContextCompat.checkSelfPermission(Activity_Main.this,
							android.Manifest.permission.ACCESS_COARSE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(Activity_Main.this,
								new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
								100);
					} else {
						//具有权限
						ConnectType="Bluetooth";
						Intent serverIntent = new Intent(thisCon,Activity_DeviceList.class);
						startActivityForResult(serverIntent, ZPLPrinterHelper.ACTIVITY_CONNECT_BT);
						return;
					}
				} else {
					//系统不高于6.0直接执行
					ConnectType="Bluetooth";
					Intent serverIntent = new Intent(thisCon,Activity_DeviceList.class);
					startActivityForResult(serverIntent, ZPLPrinterHelper.ACTIVITY_CONNECT_BT);
				}
	    	}
	    	else if(view.getId()==R.id.btnWIFI)
	    	{	    		
	    		ConnectType="WiFi";
	    		Intent serverIntent = new Intent(thisCon, Activity_Wifi.class);
				serverIntent.putExtra("PN", PrinterName); 
				startActivityForResult(serverIntent, ZPLPrinterHelper.ACTIVITY_CONNECT_WIFI);
				return;	
	    	}
	    	else if(view.getId()==R.id.btnUSB)
	    	{
	    		ConnectType="USB";							
//				HPRTPrinter=new ZPLPrinterHelper(thisCon,arrPrinterList.getItem(spnPrinterList.getSelectedItemPosition()).toString());
				//USB not need call "iniPort"				
				mUsbManager = (UsbManager) thisCon.getSystemService(Context.USB_SERVICE);				
		  		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();  		
		  		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		  		
		  		boolean HavePrinter=false;		  
		  		while(deviceIterator.hasNext())
		  		{
		  		    device = deviceIterator.next();
		  		    int count = device.getInterfaceCount();
		  		    for (int i = 0; i < count; i++) 
		  	        {
		  		    	UsbInterface intf = device.getInterface(i); 
		  	            if (intf.getInterfaceClass() == 7)
		  	            {
		  	            	HavePrinter=true;
		  	            	mUsbManager.requestPermission(device, mPermissionIntent);		  	            	
		  	            }
		  	        }
		  		}
		  		if(!HavePrinter)
		  			txtTips.setText(thisCon.getString(R.string.activity_main_connect_usb_printer));	
	    	}
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickConnect "+ConnectType)).append(e.getMessage()).toString());
		}
    }
		   			
	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context context, Intent intent) 
	    {
	    	try
	    	{
		        String action = intent.getAction();	       
		        if (ACTION_USB_PERMISSION.equals(action))
		        {
			        synchronized (this) 
			        {		        	
			            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
				        {			 
				        	if(zplPrinterHelper.PortOpen(device)!=0)
							{					
//				        		HPRTPrinter=null;
								txtTips.setText(thisCon.getString(R.string.activity_main_connecterr));												
			                	return;
							}
				        	else
				        		txtTips.setText(thisCon.getString(R.string.activity_main_connected));
				        		
				        }		
				        else
				        {			        	
				        	return;
				        }
			        }
			    }
		        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) 
		        {
		            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		            if (device != null) 
		            {	                	            	
		            	zplPrinterHelper.PortClose();
		            }
		        }	    
	    	} 
	    	catch (Exception e) 
	    	{
	    		Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> mUsbReceiver ")).append(e.getMessage()).toString());
	    	}
		}
	};
	
	
	public void onClickClose(View view) 
	{
    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
	    	if(zplPrinterHelper!=null)
			{					
	    		zplPrinterHelper.PortClose();
			}
			this.txtTips.setText(R.string.activity_main_tips);
			return;	
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickClose ")).append(e.getMessage()).toString());
		}
    }
	
	
	public void onClickDo(View view) 
	{
		if (!checkClick.isClickEvent()) return;
		
		if(!ZPLPrinterHelper.IsOpened()){
			Toast.makeText(thisCon, thisCon.getText(R.string.activity_main_tips), Toast.LENGTH_SHORT).show();				
			return;
		}
		    	    	
    	else if(view.getId()==R.id.btnSampleReceipt){
    		PrintSampleReceipt("1");
    	}
    	else if(view.getId()==R.id.btn1DBarcodes){
    		Intent myIntent = new Intent(this, Activity_1DBarcodes.class);    		
        	startActivityFromChild(this, myIntent, 0);
    	}
    	else if(view.getId()==R.id.btnTextFormat){
    		Intent myIntent = new Intent(this, Activity_TextFormat.class);
        	startActivityFromChild(this, myIntent, 0);
    	}
    	else if(view.getId()==R.id.btnPrintImageFile){
			if (Build.VERSION.SDK_INT >= 23) {
				//校验是否已具有模糊定位权限
				if (ContextCompat.checkSelfPermission(Activity_Main.this,
						android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(Activity_Main.this,
							PERMISSIONS_STORAGE,
							100);
				} else {
					//具有权限
					Intent myIntent = new Intent(this, Activity_PRNFile.class);
					myIntent.putExtra("Folder", android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
					myIntent.putExtra("FileFilter", "jpg,gif,png,");
					startActivityForResult(myIntent, ZPLPrinterHelper.ACTIVITY_IMAGE_FILE);
					return;
				}
			} else {
				//系统不高于6.0直接执行
				Intent myIntent = new Intent(this, Activity_PRNFile.class);
				myIntent.putExtra("Folder", android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
				myIntent.putExtra("FileFilter", "jpg,gif,png,");
				startActivityForResult(myIntent, ZPLPrinterHelper.ACTIVITY_IMAGE_FILE);
			}

		}
    	else if(view.getId()==R.id.btnPrintSN){
    		try{
				String printerSN = zplPrinterHelper.getPrinterSN();
				Toast.makeText(thisCon,printerSN,Toast.LENGTH_SHORT).show();
			}catch (Exception e){}
    	}
    	else if(view.getId()==R.id.btnQRCode){
    		Intent myIntent = new Intent(this, Activity_QRCode.class);
        	startActivityFromChild(this, myIntent, 0);
    	}    	
    	else if(view.getId()==R.id.btnPrintTestPage)
    	{
    		try {
    			zplPrinterHelper.selfTest();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickWIFI ")).append(e.getMessage()).toString());
			}
//			upPrint();
    	}else if(view.getId()==R.id.btnRFID){
    		try{
    			zplPrinterHelper.start();
    			zplPrinterHelper.writeRFID(2,1,"中文".getBytes("GB2312"));
				zplPrinterHelper.readRFID(2, 4, 1);
				zplPrinterHelper.end();
				byte[] bytes = zplPrinterHelper.ReadData(3);
				if (bytes!=null&&bytes.length>0){
					String hexStr = new String(bytes);
					byte[] hexByte = UtilityTooth.hexToByte(hexStr);
					Toast.makeText(thisCon,new String(hexByte,"GB2312"),Toast.LENGTH_SHORT).show();
				}
			}catch (Exception e){}
		}
    }
	
	
	
	
	private void PrintSampleReceipt(String numb)
	{
		try
		{
//		    ZPLPrinterHelper.printAreaSize("100", "80");
//		    ZPLPrinterHelper.CLS();
//			String[] ReceiptLines = getResources().getStringArray(R.array.activity_main_sample_2inch_receipt);
//			ZPLPrinterHelper.LanguageEncode="GBK";
//			hprtPrinterHelper.XA();
//			hprtPrinterHelper.PrintData("^CI14\r\n");
//			for(int i=0;i<ReceiptLines.length;i++){
//				hprtPrinterHelper.Text("10", ""+(i*30), "@", "N", 3, ReceiptLines[i]);
//			}
//			hprtPrinterHelper.XZ();
			InputStream afis =this.getResources().getAssets().open("zpl.txt");//打印模版放在assets文件夹里
			String path = new String(InputStreamToByte(afis ),"utf-8");//打印模版以utf-8无bom格式保存
//			String replace = path.replace("[numb]", numb);
			zplPrinterHelper.printData(path);
		}
		catch(Exception e)
		{
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
		}
	}
	private byte[] InputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}
	/*public static class PrinterProperty
	{
		public static String Barcode="";
		public static boolean Cut=false;
		public static int CutSpacing=0;
		public static int TearSpacing=0;
		public static int ConnectType=0;
		public static boolean Cashdrawer=false;
		public static boolean Buzzer=false;
		public static boolean Pagemode=false;
		public static String PagemodeArea="";
		public static boolean GetRemainingPower=false;
		public static boolean SampleReceipt=true;
		public static int StatusMode=0;
	}*/

	private void upPrint(){
		new Thread(){
			@Override
			public void run() {
				super.run();
				try{
					InputStream afis = Activity_Main.this.getResources().getAssets().open("id2ie2V1.0.47_Beta2.img");
					byte[] data = InputStreamToByte(afis);
					zplPrinterHelper.WriteData(UpPrintUtility.inToUpPrint());
					zplPrinterHelper.WriteData(UpPrintUtility.getTop(data));
					byte[] readData = zplPrinterHelper.ReadData(2);
					if (readData==null||readData.length!=1||readData[0]!=0){
						Log.d("Print", "readData one error timeout");
						return;
					}
					List<byte[]> bytesToList = UpPrintUtility.addBytesToList(UpPrintUtility.getBody(data));
					Log.d("Print", "总包数 "+bytesToList.size());
					for (int i = 0; i < bytesToList.size(); i++) {
						Log.d("Print", "当前包数 "+(i+1));
						zplPrinterHelper.WriteData(bytesToList.get(i));
						//需要加延时，不加打印机会死
						sleep(100);
					}
					byte[] readDataEnd = zplPrinterHelper.ReadData(8);
					if (readDataEnd==null||readDataEnd.length!=1||readDataEnd[0]!=0){
						Log.d("Print", "readDataEnd error timeout");
						return;
					}
					zplPrinterHelper.WriteData(UpPrintUtility.resetPrint());
					Log.d("Print", "发送完成");
				}catch (Exception e){
					Log.d("Print", "升级失败 "+e.getMessage().toString());
				}

			}
		}.start();
	}
}
