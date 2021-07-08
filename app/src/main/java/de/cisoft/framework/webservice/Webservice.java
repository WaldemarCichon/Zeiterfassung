package de.cisoft.framework.webservice;

import java.io.IOException;
import java.util.Vector;

import org.ksoap2clone.SoapEnvelope;
import org.ksoap2clone.SoapFault;
import org.ksoap2clone.serialization.SoapObject;
import org.ksoap2clone.serialization.SoapSerializationEnvelope;
import org.ksoap2clone.transport.HttpTransportSE;

public class Webservice {
	private String address;
	private String namespace;
	HttpTransportSE transport;

	private static final String MPZE_CISOFT_NAMESPACE = "http://gfi-informatik.de/MPZE";
	public static final String MPZE_CISOFT = "http://mpze.cichons.de/service.asmx";
	private static Webservice instance;
	private static final Webservice cisoftInstance = new Webservice(
			MPZE_CISOFT, MPZE_CISOFT_NAMESPACE);

	private Webservice(String address, String namespace) {
		this.address = address;
		this.namespace = namespace;
		transport = new HttpTransportSE(this.address); // "http://217.6.190.164:10080/mpze_neu/Service.asmx");
		prepareHttpConnection();
		/*
		try {
			transport.getServiceConnection().setChunkedStreamingMode();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		*/
	}
	
	
	public static void init(String address, String namespace) {
		instance = new Webservice(address, namespace);
	}

	public static Webservice getInstance() {
		return instance;
	}

	public static Webservice getCiSoftInstance() {
		return cisoftInstance;
	}

	private void prepareHttpConnection() {
		try {
			transport.getServiceConnection().setRequestProperty("Connection", "close");
			System.setProperty("http.keepAlive", "false");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getStringValue(SoapSerializationEnvelope envelope)
			throws SoapFault {
		Vector<?> v = (Vector<?>) envelope.getResponse();
		if (v == null) {
			return null;
		}
		Object o = v.get(1);
		if (o == null) {
			return null;
		}
		String s;
		if (o instanceof String) {
			s = (String) o;
		} else {
			s = o.toString();
		}
		return s;

	}

	public String call(String serviceName) throws Exception {
		SoapObject request = new SoapObject(namespace, serviceName);// "http://gfi-informatik.de/MPZE",
																	// "Version");
		// request.addProperty("PlaceName", city);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER12);
		envelope.setOutputSoapObject(request);
		// It seems that it is a .NET Web service because it doesn't work
		// without next line
		envelope.dotNet = true;

		prepareHttpConnection();
		transport.call(namespace, envelope);

		return getStringValue(envelope);
	}

	public String call(String serviceName, String paramName, String paramValue)
			throws Exception {
		SoapObject request = new SoapObject(namespace, serviceName);// "http://gfi-informatik.de/MPZE",
																	// "Version");
		CharSequence cs = paramValue;
		request.addProperty(paramName, cs);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER12);
		envelope.setOutputSoapObject(request);
		// It seems that it is a .NET Web service because it doesn't work
		// without next line
		envelope.dotNet = false;

		prepareHttpConnection();
		transport.call(namespace, envelope);
		
		

		return getStringValue(envelope);
	}

	public String call(String serviceName, Object... alternatingNameValue)
			throws Exception {

		if (alternatingNameValue.length % 2 > 0) {
			throw new RuntimeException("Upaired count of param-value pairs");
		}
		SoapObject request = new SoapObject(namespace, serviceName);// "http://gfi-informatik.de/MPZE",
																	// "Version");

		for (int i = 0; i < alternatingNameValue.length;) {
			if (!(alternatingNameValue[i] instanceof String)) {
				throw new RuntimeException("Parameter name must be allways an String");
			}
			String paramName = (String)alternatingNameValue[i++];
			Object paramValue = alternatingNameValue[i++];
			if (paramValue instanceof String) {
				paramValue = (CharSequence) paramValue;
			}

			request.addProperty(paramName, paramValue);
		}

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER12);
		envelope.setOutputSoapObject(request);
		// It seems that it is a .NET Web service because it doesn't work
		// without next line
		envelope.dotNet = false;

		prepareHttpConnection();
		transport.call(namespace, envelope);

		return getStringValue(envelope);
	}
}
