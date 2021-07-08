package de.cisoft.zeiterfassung.implementation.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.cisoft.framework.Entity;
import de.cisoft.framework.annotations.Field;
import de.cisoft.framework.annotations.ID;
import de.cisoft.framework.annotations.Name;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.icons.Icons;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import de.cisoft.zeiterfassung.ui.enums.Branding;

public class Task extends Entity {
	private Integer id;
	private String name;
	private String konto;
	private String iconName;
	private static Integer orange;
	private Bitmap bitmap;
	
	public Task() {
		super();
	}

	public Task(String[] fields, de.cisoft.framework.Field[] assignments) {
		super(fields, assignments);
	}

	public Task(String row, String separator,
			de.cisoft.framework.Field[] assignments) {
		super(row, separator, assignments);
	}

	@ID
	@Field(name="id", alias="id", type=Integer.class)
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Name
	@Field(name="name", alias="name", type=String.class) 
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	@Field(name="konto", alias="konto", type=String.class)
	public String getKonto() {
		return this.konto;
	}
	
	public void setKonto(String konto) {
		this.konto = konto;
	}

	@Field(name="iconName", alias="iconName", type=String.class) 
	public String getIconName() {
		return iconName;
	}
	
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	
	public String toString() {
		return konto+": "+name;
	}
	
	private Bitmap getBitmap() {
		if (bitmap == null) {
			bitmap = Icons.getInstance().getIcon(iconName);
		}
		return bitmap;
	}
	
	private int getOrange(View view) {
		if (orange == null) {
			orange = view.getResources().getColor(R.color.Orange1);
		}
		return orange;
	}

	
	//@SuppressWarnings("deprecation")
	@Override
	protected void fill(View view, int position) {
		TextView tvTitle = (TextView) view.findViewById(R.id.taskRowTitle);
		TextView tvSubtitle = (TextView) view.findViewById(R.id.taskRowSubtitle);
		ImageView imageTextBackground = (ImageView) view.findViewById(R.id.imageTextBackground);
		
		tvTitle.setText(this.getName());
		tvSubtitle.setText("Konto: "+this.getKonto());
		
		ImageView icon = (ImageView) view.findViewById(R.id.taskIcon);
		icon.setImageBitmap(getBitmap());
		
		if (Settings.getInstance().getBranding() == Branding.WUERZBURG) {
			((View) tvTitle.getParent()).setBackgroundColor(getOrange(view));
			imageTextBackground.setVisibility(View.GONE);
			
		}
		view.setId(id);
		/*
		boolean even = (position % 2)==0;
		
		if (MainActivity.BRANDING && MainActivity.ALTERNATING_VIEWS) {
			((View) tvTitle.getParent()).setBackgroundColor(Color.BLACK);
			tvTitle.setTextColor(getOrange(view));
			tvSubtitle.setTextColor(getOrange(view));
			
			((View) tvTitle.getParent()).setBackgroundColor(even ? Color.WHITE : getOrange(view));
			tvTitle.setTextColor(even ? Color.BLACK : Color.WHITE);
			tvSubtitle.setTextColor(even ? Color.BLACK : Color.WHITE);
			
		}
		*/
		/*
		TextView tvTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
		TextView tvObjectAction = (TextView) view.findViewById(R.id.tvObjectAction);
		tvObjectAction.setText((this.getProject()==null ?  "" : 
			("("+this.getProject().getKonto()+") "+this.getProject().getStrasse()+" - "))+
			(this.getTask() == null ? "=====" : this.getTask().getName()));
		tvTimestamp.setText(getTimestamp()==null ? "--.--.---- --:--" :getTimestamp().toLocaleString()+
				(action == null ? "" : (" ="+action.getKonto()+"=")));
		*/
	}
}
