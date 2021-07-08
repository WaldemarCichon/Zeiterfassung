package de.cisoft.zeiterfassung.ui;

//import java.util.Date;
//import java.util.Vector;

//import org.ksoap2clone.SoapEnvelope;
//import org.ksoap2clone.serialization.SoapObject;
//import org.ksoap2clone.serialization.SoapSerializationEnvelope;
//import org.ksoap2clone.transport.HttpTransportSE;

import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.R;
import android.app.Activity;
//import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class K2SoapView extends Activity implements OnClickListener {
	private Button btSend;
	private TextView txtCity;
	private TextView txtResult;
	
	public K2SoapView() {
		MyLog.i("K2SoapView", "In constructor");
	}
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.i("K2soapview", "Vor setContentView");
        setContentView(R.layout.activity_k2_soap_view);
        MyLog.i("K2soapview", "nach setContentView");
        btSend = (Button) this.findViewById(R.id.sendButton);
        btSend.setOnClickListener(this);
        txtCity = (TextView) this.findViewById(R.id.txtCity);
        txtResult = (TextView) this.findViewById(R.id.txtResult);
        createView();
        Webservice.init("http://217.6.190.164:10080/mpze_neu/Service.asmx", "http://gfi-informatik.de/MPZE");
    }

    
    public void onClick(View v) {
  	btSend.setText("Sent");
  	MyLog.i("Main","before start activity");
  	try {
  		String s = Webservice.getInstance().call("Version"); 
  		String s1 = Webservice.getInstance().call(txtCity.getText().toString());
  		s+="\n"+s1;
  		txtResult.setText(s);
  	} catch (Exception e) {
  		e.printStackTrace();
  		txtResult.setText(e.getMessage());
	}
  	
  }
    
//*************************************************************************

    

    
    
    
//**************************************************************************
	private void createView() {
		// TODO Auto-generated method stub
		
	}
}
