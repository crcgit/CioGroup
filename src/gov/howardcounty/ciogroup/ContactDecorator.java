package gov.howardcounty.ciogroup;

import android.graphics.Bitmap;

public class ContactDecorator implements IContact {

	private IContact itsContact;
	
	// Constructor
	
	public ContactDecorator() {
		itsContact = new Contact();
	}
	
	public ContactDecorator(IContact contact) {
		itsContact = contact;
	}
	
	// Getters
	
	@Override
	public String getName() {
		return itsContact.getName();
	}

	@Override
	public String getEmail() {
		return itsContact.getEmail();
	}

	@Override
	public String getLocationFrom() {
		return itsContact.getLocationFrom();
	}

	@Override
	public String getBiography() {
		return itsContact.getBiography();
	}

	@Override
	public Bitmap getBitmap() {
		return itsContact.getBitmap();
	}

	@Override
	public int getDbId() {
		return itsContact.getDbId();
	}
	
	@Override
	public String getPhoneOffice() {
		return itsContact.getPhoneOffice();
	}

	@Override
	public String getPhoneMobile() {
		return itsContact.getPhoneMobile();
	}

	@Override
	public String getPhoneHome() {
		return itsContact.getPhoneHome();
	}
	
	@Override
	public String getPassword() {
		return itsContact.getPassword();
	}
	
	// Setters

	@Override
	public void setName(String name) {
		itsContact.setName(name);
	}

	@Override
	public void setEmail(String email) {
		itsContact.setEmail(email);
	}

	@Override
	public void setLocationFrom(String locationFrom) {
		itsContact.setLocationFrom(locationFrom);
	}

	@Override
	public void setBiography(String biography) {
		itsContact.setBiography(biography);
	}

	@Override
	public void setBitmap(Bitmap bitmap) {
		itsContact.setBitmap(bitmap);
	}

	@Override
	public void setDbId(int dbId) {
		itsContact.setDbId(dbId);
	}

	@Override
	public void setPhoneOffice(String phoneOffice) {
		itsContact.setPhoneOffice(phoneOffice);
	}

	@Override
	public void setPhoneMobile(String phoneMobile) {
		itsContact.setPhoneMobile(phoneMobile);
	}

	@Override
	public void setPhoneHome(String phoneHome) {
		itsContact.setPhoneHome(phoneHome);
	}

	@Override
	public void setPassword(String password) {
		itsContact.setPassword(password);
	}

}
