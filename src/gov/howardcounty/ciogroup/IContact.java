package gov.howardcounty.ciogroup;

import android.graphics.Bitmap;

public interface IContact {
	
	// Getters
	
	String getName();
	String getEmail();
	String getLocationFrom();
	String getBiography();
	Bitmap getBitmap();
	int getDbId();
	String getPhoneOffice();
	String getPhoneMobile();
	String getPhoneHome();
	String getPassword();
	
	// Setters
	
	public void setName(String name);
	public void setEmail(String email);
	public void setLocationFrom(String locationFrom);
	public void setBiography(String biography);
	public void setBitmap(Bitmap bitmap);
	public void setDbId(int dbId);
	public void setPhoneOffice(String phoneOffice);
	public void setPhoneMobile(String phoneMobile);
	public void setPhoneHome(String phoneHome);
	public void setPassword(String password);
	
}
