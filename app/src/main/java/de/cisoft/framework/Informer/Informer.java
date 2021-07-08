package de.cisoft.framework.Informer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

public class Informer {
	
	private static final String YEAR = "year";

	private static final String MONTH = "month";

	private static final String MAIL_ADDRESS = "mailAddress";

	private static final String ADD_BOOKING_COUNT  = "AddBookingCount";
	
	private Webservice webservice;
	private String mailAddress;
	private static Informer instance;

	private Informer() {
		webservice = Webservice.getCiSoftInstance();
		mailAddress = Settings.getInstance().getUserMailAddress();
	}
	
	public static Informer getInstance() {
		if (instance == null) {
			instance = new Informer();
		}
		return instance;
	}
	
	public void inform() {
		new Thread() {
			@Override
			public void run() {
				try {
					Calendar c = new GregorianCalendar();
					int month = c.get(Calendar.MONTH)+1;
					int year = c.get(Calendar.YEAR);
					webservice.call(ADD_BOOKING_COUNT, MAIL_ADDRESS, mailAddress, MONTH, month, YEAR, year, "count", 1 );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

}
