package de.cisoft.zeiterfassung.implementation.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.net.Uri;
import android.os.Environment;
import android.view.View;

import de.cisoft.framework.Entity;
import de.cisoft.framework.EntityTable;
import de.cisoft.framework.Field;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.implementation.MetaEntity.Month;
import de.cisoft.zeiterfassung.implementation.enums.SaveMode;
import de.cisoft.zeiterfassung.implementation.helpers.ResendCallback;

public class Bookings extends EntityTable<Booking> {
	private static final String serviceName = "";
	private static final String titleLine = "id|timestamp|janitor|project|task|action|comment|systemid|sendts";
	private static final String FILE_NAME = "bookings.dat";
	private Booking lastBooking;
	private Booking preLastBooking;
	
	public Bookings() throws FileNotFoundException {
		super(Booking.class, getInputStream(FILE_NAME), titleLine);
	}

	public Uri getUri() {
		File bookingFile  = new File(Environment.getDataDirectory().getAbsolutePath(), FILE_NAME);
		return Uri.fromFile(bookingFile);
	}
	
	public Bookings(Webservice webservice) throws Exception {
		super(Booking.class, webservice, serviceName, titleLine);
	}
	
	public Bookings(Class<Booking> clazz) {
		super(clazz);
	}
	
	public Bookings(int i) {
		super(Booking.class, i);
	}

	protected Bookings getInstance() {
		return new Bookings(Booking.class);
	}	
	
	public Class<Booking> getEntityClass() {
		return Booking.class;
	}
	
	
	@Override
	protected String getDefaultFileName() {
		return (FILE_NAME);
	}
	
	protected String getServiceName() {
		return serviceName;
	}
	
	protected String getDefaultTitle() {
		return titleLine;
	}

	@Override
	public void add(Booking booking) {
		super.add(booking);
		this.preLastBooking = lastBooking;
		this.lastBooking = booking;
	}

	public void sendLastBooking(Webservice webservice) {
		if (lastBooking!=null) {
			send(webservice, lastBooking);
		}
	}
	
	private boolean working = false;

	int count = 0;

	public void resend(final Webservice webservice) {
		resend(webservice, null);
	}

	public void resend(final Webservice webservice, final ResendCallback callback) {
		if (working) {
			return;
		}
		working = true;
		final String fieldSeparator = getFieldSeparator();
		final Field[] assignments = getAssignments();
		count = 0;
		Thread thread = new Thread() {
			@Override
			public void run() {
				for (Booking booking : getValues()) {
					booking.send(webservice, fieldSeparator, assignments);
					if (callback != null) {
						callback.callback(++count, booking);
					}
				}
				working = false;
			}
		};
		thread.start();
	}
	
	public void send(Webservice webservice, Booking booking) {
		booking.sendLazy(webservice, getFieldSeparator(), getAssignments());
	}
	
	public void send(final Webservice webservice) {
		final String fieldSeparator = getFieldSeparator();
		final Field[] assignments = getAssignments();
		Thread thread = new Thread() {
			@Override
			public void run() {
				for (Booking booking : getValues()) {
					if (!booking.isSent()) {
						booking.send(webservice, fieldSeparator, assignments);
					}
				}
			}
		};
		thread.start();
	}
	
	public boolean hasNotSentBookings() {
		for (Booking booking : getValues() ) {
			if (!booking.isSent()) {
				return true;
			}
		}
		return false;
	}
	
	public Bookings select(Date from, Date to) {
		long fromMs = from == null ? 0 : from.getTime();
		long toMs = to == null ? Long.MAX_VALUE : to.getTime();
		Bookings selected = new Bookings(10);
		selected.fields = fields;
		selected.assignments = assignments;
		for (Booking booking : this.getValues()) {
			Date currentDate = booking.getTimestamp();
			long currentDateMs = currentDate.getTime();
			if (currentDateMs>=fromMs && currentDateMs<=toMs) {
				selected.add(booking);
			}
		}
		return selected;
	}
	
	@Override
	protected Class<? extends Entity[]> getArrayClass() {
		return Booking[].class;
	}

	private void determineLastPrelastBooking(boolean notNull) {
		//System.out.println("calculating last booking");
		int i = 0;
		//System.out.println("count="+i+", external count"+getExternalRowCount()+", internal count"+getRowCount());
		while (i<getCount()) {
			Booking booking = this.getItemAt(i++);
			if (booking == null && notNull) {
				continue;
			}
			if (lastBooking == null) {
				lastBooking = booking;
			} else {
				if (booking.getTimestamp().getTime() > lastBooking.getTimestamp().getTime()) {
					preLastBooking = lastBooking;
					lastBooking = booking;
				}
			}
		}
		enhanceEmptyBooking(lastBooking);
		enhanceEmptyBooking(preLastBooking);
	}
	
	private void enhanceEmptyBooking(Booking booking) {
		if (booking!=null && (booking.getTimestamp()==null)) {
			booking.setTimestamp(new Date());
		}
		if (booking!=null && booking.getAction()==null) {
			booking.setAction(Actions.ENDE);
		}	
	}
	
	public Booking getLastBooking() {
		//System.out.println("getting last booking");
		if (lastBooking == null) {
			determineLastPrelastBooking(false);
		}
		//System.out.println("===> returning lastBooking = "+lastBooking);
		return lastBooking;
	}

	public Booking getPreLastBooking() {
		if (preLastBooking == null) {
			determineLastPrelastBooking(false);
		}
		return preLastBooking;
	}
	
	public Booking getLastBookingWithProject() {
		int i = getCount()-1;
		while (i>=0) {
			Booking booking = getItemAt(i--);
			if (booking.getProject() != null) {
				return booking;
			}
		}
		return null;
	}
	
	public void sort(int criteria) {
		Comparator<Booking> comparator;
		if (criteria == 0) {
			comparator = null;
		} else {
			comparator = new BookingsComparator(criteria);
		}
		setComparator(comparator);
	}
	
	private static class BookingsComparator implements Comparator<Booking> {

		private int criteria;

		private BookingsComparator(int criteria) {
			this.criteria = criteria;
		}
		
		@Override
		public int compare(Booking arg0, Booking arg1) {
			switch (criteria) {
				case 1: return Integer.valueOf(arg0.getId()).compareTo(arg1.getId());
				case 2: return arg0.getSystemId().compareTo(arg1.getSystemId());
				case 3: return arg0.getTimestamp().compareTo(arg1.getTimestamp());
				case 4: return arg0.getSendTs().compareTo(arg1.getSendTs());
			}
			return 0;
		}
		
	}

	public Bookings getToSend() {
		Bookings bookings = new Bookings(10);
		bookings.fields = fields;
		bookings.assignments = assignments;
		for (Booking booking : getEntites()) {
			if (!booking.isSent()) {
				bookings.add(booking);
			}
		}
		return bookings;
	}

    public Booking getLastBookingNotNull() {
		determineLastPrelastBooking(true);
		return lastBooking;
	}
   
    public void save(SaveMode saveMode) throws IOException {
    	saveTo(saveMode.getFileName());
    }
    
    @Override
    public boolean isSaved() {
    	return false;
    }

    public void recalc() {
		HashMap<Integer, Month> months = new HashMap<Integer, Month>();
		Collection<Booking> entities = this.getEntites();
		entities.forEach(booking -> {
			Month month;
			int monthNumber = booking.getTimestamp().getMonth();
			if (months.containsKey(monthNumber)) {
				month = months.get(monthNumber);
			} else {
				month = new Month(monthNumber);
				months.put(monthNumber, month);
			}
			month.add(booking);
			booking.setMonth(month);
		});
		months.values().forEach(month -> {
			month.recalc();
		});
	}
	
}

