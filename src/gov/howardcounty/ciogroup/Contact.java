package gov.howardcounty.ciogroup;

import java.nio.ByteBuffer;
import java.util.Arrays;

import android.graphics.Bitmap;

public class Contact implements IContact {
	
	// Contains
	
	private String itsName;
	private String itsEmail;
	private String itsLocationFrom;
	private String itsBiography;
	private Bitmap itsBitmap;
	private int itsDbId;
	private String itsPhoneOffice;
	private String itsPhoneMobile;
	private String itsPhoneHome;
	private String itsPassword;
	
	// Constructor
	
	public Contact() {
		itsName = "";
		itsEmail = "";
		itsLocationFrom = "";
		itsBiography = "";
		itsBitmap = null;
		itsDbId = -1;
		itsPhoneOffice = "";
		itsPhoneMobile = "";
		itsPhoneHome = "";
		itsPassword = "";
	}
	
	// Getters
	
	public String getName() {
		return itsName;
	}
	
	public String getEmail() {
		return itsEmail;
	}
	
	public String getLocationFrom() {
		return itsLocationFrom;
	}
	
	public String getBiography() {
		return itsBiography;
	}
	
	public Bitmap getBitmap() {
		return itsBitmap;
	}
	
	public int getDbId() {
		return itsDbId;
	}
	
	public String getPhoneOffice() {
		return itsPhoneOffice;
	}
	
	public String getPhoneMobile() {
		return itsPhoneMobile;
	}
	
	public String getPhoneHome() {
		return itsPhoneHome;
	}
	
	public String getPassword() {
		return itsPassword;
	}
	
	// Setters
	
	public void setName(String name) {
		itsName = name;
	}
	
	public void setEmail(String email) {
		itsEmail = email;
	}
	
	public void setLocationFrom(String locationFrom) {
		itsLocationFrom = locationFrom;
	}
	
	public void setBiography(String biography) {
		itsBiography = biography;
	}

	public void setBitmap(Bitmap bitmap) {
		itsBitmap = bitmap;
	}
	
	public void setDbId(int dbId) {
		itsDbId = dbId;
	}

	public void setPhoneOffice(String phoneOffice) {
		itsPhoneOffice = phoneOffice;
	}
	
	public void setPhoneMobile(String phoneMobile) {
		itsPhoneMobile = phoneMobile;
	}
	
	public void setPhoneHome(String phoneHome) {
		itsPhoneHome = phoneHome;
	}
	
	public void setPassword(String password) {
		itsPassword = password;
	}
	
	// Contact testing
	
	@Override 
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final Contact other = (Contact) obj;
	    if ((this.itsName == null) ? (other.itsName != null) : !this.itsName.equals(other.itsName)) {
	        return false;
	    }
	    if ((this.itsEmail == null) ? (other.itsEmail != null) : !this.itsEmail.equals(other.itsEmail)) {
	        return false;
	    }
	    if ((this.itsLocationFrom == null) ? (other.itsLocationFrom != null) : !this.itsLocationFrom.equals(other.itsLocationFrom)) {
	        return false;
	    }
	    if ((this.itsBiography == null) ? (other.itsBiography != null) : !this.itsBiography.equals(other.itsBiography)) {
	        return false;
	    }
	    if ((this.itsBitmap == null) ? (other.itsBitmap != null) : !equals(this.itsBitmap, other.itsBitmap)) {
	        return false;
	    }
	    if ((this.itsPhoneOffice == null) ? (other.itsPhoneOffice != null) : !this.itsPhoneOffice.equals(other.itsPhoneOffice)) {
	        return false;
	    }
	    if ((this.itsPhoneMobile == null) ? (other.itsPhoneMobile != null) : !this.itsPhoneMobile.equals(other.itsPhoneMobile)) {
	        return false;
	    }
	    if ((this.itsPhoneHome == null) ? (other.itsPhoneHome != null) : !this.itsPhoneHome.equals(other.itsPhoneHome)) {
	        return false;
	    }
	    if ((this.itsPassword == null) ? (other.itsPassword != null) : !this.itsPassword.equals(other.itsPassword)) {
	        return false;
	    }
	    if (this.itsDbId != other.itsDbId) {
	        return false;
	    }
	    return true;
	}
	
	private boolean equals(Bitmap bitmap1, Bitmap bitmap2) {
	    ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
	    bitmap1.copyPixelsToBuffer(buffer1);

	    ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
	    bitmap2.copyPixelsToBuffer(buffer2);

	    return Arrays.equals(buffer1.array(), buffer2.array());
	}
}
