package de.cisoft.zeiterfassung.implementation.entity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.Visibility;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.cisoft.framework.Entity;
import de.cisoft.framework.annotations.Field;
import de.cisoft.framework.annotations.ID;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.icons.Icons;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import de.cisoft.zeiterfassung.ui.enums.Branding;



public class Project extends Entity {
		
	private int id;
	private String name;
	private String strasse;
	private String ort;
	private String plz;
	private String land;
	private String konto;
	private int counter;
	private String description;
	private String iconName;
	private Bitmap bitmap;
	private static Integer orange;
	private static Boolean withIcons;
	
	public Project() {
		super();
	}
	
	public Project(String line, String seperator, de.cisoft.framework.Field[] assignments) {
		super(line, seperator, assignments);
	}
	
	@ID
	@Field(name="id", alias="id", type=Integer.class)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Field(name="name", alias="name", type=String.class)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		this.description = null;
	}
	@Field(name="strasse", alias="strasse", type=String.class)
	public String getStrasse() {
		return strasse;
	}
	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}
	@Field(name="ort", alias="ort", type=String.class)
	public String getOrt() {
		return ort;
	}
	public void setOrt(String ort) {
		this.ort = ort;
		this.ort = "Würzburg";
	}
	
	@Field(name="land", alias="land", type=String.class)
	public String getLand() {
		return land;
	}
	
	public void setLand(String land) {
		this.land = land;
	}
	
	@Field(name="plz", alias="plz", type=String.class) 
	public String getPlz() {
		return plz;
	}
	
	public void setPlz(String plz) {
		this.plz = plz;
	}
	
	@Field(name="konto", alias="konto", type=String.class) 
	public String getKonto() {
		return this.konto;
	}
	
	public void setKonto(String konto) {
		this.konto = konto;
		this.description = null;
	}
	
	@Field(name="counter", alias="counter", type=Integer.class)
	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	
	public Integer getCounter() {
		return this.counter;
	}
	
	@Field(name="iconName", alias="iconName", type=String.class)
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	
	public String getIconName() {
		return iconName;
	}
	
	public String getDescription() {
		if (description == null) {
			description = name+" ("+konto+")";
		}
		return description;
	}
	
	public String toString() {
		return getDescription(); //+" - "+strasse+" - "+ort;
	}
	
	private Bitmap getBitmap() {
		if (bitmap == null) {
			bitmap = Icons.getInstance().getIcon(iconName);
		}
		return bitmap;
	}
	
	private static boolean withIcons() {
		if (withIcons == null) {
			withIcons = Settings.getInstance().isTaskChoiceIcons();
		}
		
		return withIcons;
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
		
		if (!withIcons()) {
			TextView tvDisplay = null;
			if (view instanceof TextView) {
				tvDisplay = (TextView) view;
			} else {
				tvDisplay = (TextView) view.findViewById(R.id.lvProjects);
			}
			
			if (tvDisplay==null) {
				return;
			}
			
			tvDisplay.setText(getDescription());
			return;
		}
		TextView tvTitle = (TextView) view.findViewById(R.id.projectRowTitle);
		TextView tvSubtitle = (TextView) view.findViewById(R.id.projectRowSubtitle);
		ImageView imageTextBackground = (ImageView) view.findViewById(R.id.imageTextBackground);
		tvTitle.setText(this.getName());
		tvSubtitle.setText(/*getStrasse()+" - "+*/getOrt());
		ImageView icon = (ImageView) view.findViewById(R.id.projectIcon);
		icon.setImageBitmap(getBitmap());
		/*
		boolean even = (position % 2)==0;
		
		if (MainActivity.BRANDING && MainActivity.ALTERNATING_VIEWS) {
			((View) tvTitle.getParent()).setBackgroundColor(even ? Color.WHITE : getOrange(view));
			tvTitle.setTextColor(even ? Color.BLACK : Color.WHITE);
			tvSubtitle.setTextColor(even ? Color.BLACK : Color.WHITE);
		}
		*/
		
		if (Settings.getInstance().getBranding() == Branding.WUERZBURG) {
			((View) tvTitle.getParent()).setBackgroundColor(getOrange(view));
			imageTextBackground.setVisibility(View.GONE);
			
		}
		view.setId(id);
		
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
