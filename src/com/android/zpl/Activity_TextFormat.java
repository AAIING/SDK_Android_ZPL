package com.android.zpl;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import ZPL.PublicFunction;
import ZPL.ZPLPrinterHelper;


public class Activity_TextFormat  extends Activity 
{	
	private Context thisCon=null;
	private PublicFunction PFun=null;
	private EditText txtText=null;
	private EditText txtformat_x=null;
	private EditText txtformat_y=null;
	private Spinner spnformat_font=null;
	private Spinner spnformat_rotation=null;
	private ArrayAdapter arrformat_font;
	private ArrayAdapter arrformatrotation;
	private Spinner spnformat_x_multiplication=null;
	private ArrayAdapter arrformat_x_multiplication;
	private Spinner spnformat_y_multiplication=null;
	private ArrayAdapter arrformat_y_multiplication;
	private int formatfont=0;
	private int x_multiplication=1;
	private String y_multiplication="0";
	private String qrcoderotation="N";
	private ZPLPrinterHelper hprtPrinterHelper;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);	   
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_text_format);			
		thisCon=this.getApplicationContext();
		
		txtText = (EditText) findViewById(R.id.txtText);
		txtformat_x = (EditText) findViewById(R.id.txtformat_x);
		txtformat_y = (EditText) findViewById(R.id.txtformat_y);
		
		spnformat_font = (Spinner) findViewById(R.id.spnformat_font);	
		String[] sList = getResources().getStringArray(R.array.activity_text_bold);
		arrformat_font = new ArrayAdapter<String>(Activity_TextFormat.this,android.R.layout.simple_spinner_item, sList);
		arrformat_font.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		spnformat_font.setAdapter(arrformat_font);
		spnformat_font.setOnItemSelectedListener(new OnItemSelectedformatfont());
		
		spnformat_rotation = (Spinner) findViewById(R.id.spnformat_rotation);			
		arrformatrotation = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);				
		arrformatrotation=ArrayAdapter.createFromResource(this, R.array.activity_1dbarcodes_hri_rotation, android.R.layout.simple_spinner_item);
		arrformatrotation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		spnformat_rotation.setAdapter(arrformatrotation);	
		spnformat_rotation.setOnItemSelectedListener(new OnItemSelectedformatrotation());
		
		sList = "1,2,3,4,5,6".split(",");
		spnformat_x_multiplication = (Spinner) findViewById(R.id.spnformat_x_multiplication);			
		arrformat_x_multiplication = new ArrayAdapter<String>(Activity_TextFormat.this,android.R.layout.simple_spinner_item, sList);				
		arrformat_x_multiplication.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		spnformat_x_multiplication.setAdapter(arrformat_x_multiplication);	
		spnformat_x_multiplication.setOnItemSelectedListener(new OnItemSelectedformat_x_multiplication());
		
		spnformat_y_multiplication = (Spinner) findViewById(R.id.spnformat_y_multiplication);			
		arrformat_y_multiplication = new ArrayAdapter<String>(Activity_TextFormat.this,android.R.layout.simple_spinner_item, sList);				
		arrformat_y_multiplication.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		spnformat_y_multiplication.setAdapter(arrformat_y_multiplication);	
		spnformat_y_multiplication.setOnItemSelectedListener(new OnItemSelectedformat_y_multiplication());
		hprtPrinterHelper = ZPLPrinterHelper.getZPL(thisCon);
	}
	
	private class OnItemSelectedformatrotation implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{			
			switch (arg2) {
			case 0:
				qrcoderotation="N";
				break;
			case 1:
				qrcoderotation="R";
				break;
			case 2:
				qrcoderotation="I";
				break;
			case 3:
				qrcoderotation="B";
				break;

			default:
				break;
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	private class OnItemSelectedformatfont implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{			
			formatfont=arg2;
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	private class OnItemSelectedformat_x_multiplication implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{			
			x_multiplication=arg2+1;
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	private class OnItemSelectedformat_y_multiplication implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{			
			y_multiplication=spnformat_y_multiplication.getSelectedItem().toString();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	
	public void onClickPrint(View view) 
	{
    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
    		String sText=txtText.getText().toString().trim();
	    	if(sText.length()==0)
	    	{
	    		Toast.makeText(thisCon, getString(R.string.activity_1dbarcodes_no_data), Toast.LENGTH_SHORT).show();
	    		return;
	    	}
			int x = UtilityTooth.chackEdtextArea(thisCon, txtformat_x, 0, 9999, getString(R.string.activity_parameter_error));
			if(x==-1){
				return;
			}
			int y = UtilityTooth.chackEdtextArea(thisCon, txtformat_y, 0, 9999, getString(R.string.activity_parameter_error));
			if(y==-1){
				return;
			}
	    	hprtPrinterHelper.start();
	    	hprtPrinterHelper.printData("^CI14\r\n");
			hprtPrinterHelper.printText(""+x, ""+y, formatfont, qrcoderotation, x_multiplication, txtText.getText().toString());
			hprtPrinterHelper.end();
    	}
		catch (Exception e) 
		{			
			Log.d("HPRTSDKSample", (new StringBuilder("Activity_TextFormat --> onClickPrint ")).append(e.getMessage()).toString());
		}
    }
}
