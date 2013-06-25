package gov.howardcounty.ciogroup;

import gov.howardcountymd.utils.gis.Gis;
import gov.howardcountymd.utils.http.HttpResult;
import gov.howardcountymd.utils.http.IHttpResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GisContacts extends Gis {

	// Constants
	private static int defaultBitmapQuality = 50;
	
	// Json keys (must match names in database columns)
	private static String jsonKey_name = "NAME";
	private static String jsonKey_locationFrom = "CITY_COUNTY";
	private static String jsonKey_email = "EMAIL";
	private static String jsonKey_biography = "BIOGRAPHY";
	private static String jsonKey_imgString = "IMG_STRING";
	private static String jsonKey_dbId = "ID";
	private static String jsonKey_phoneOffice = "PHONE_OFFICE";
	private static String jsonKey_phoneMobile = "PHONE_MOBILE";
	private static String jsonKey_phoneHome = "PHONE_HOME";
	private static String jsonKey_password = "PASSWORD";
	
	// http get param keys (must match names in .asp file)
	private static String httpKey_name = "na";
	private static String httpKey_locationFrom = "cc";
	private static String httpKey_email = "em";
	private static String httpKey_biography = "bi";
	private static String httpKey_imgString = "im";
	private static String httpKey_dbId = "id";
	private static String httpKey_phoneOffice = "po";
	private static String httpKey_phoneMobile = "pm";
	private static String httpKey_phoneHome = "ph";
	private static String httpKey_password = "pw";
	
	// Paths to .asp for http get
	private static String gisGetContactPathStr = "/iOSdigCom/clc_getContact.asp";
	private static String gisUpdateContactPathStr = "/iOSdigCom/clc_updateContact.asp";
	private static String gisGetContactsPathStr = "/iOSdigCom/clc_getContacts.asp";
	
	// Contains
	
	private VerifyContactListener itsVerifyContactListener;
	private RequestContactsListener itsRequestContactsListener;
	private UpdateContactListener itsUpdateContactListener;
	private RequestContactListener itsRequestContactListener;
	private IContact itsContact;
	private Bitmap itsDefaultBitmap; 
	// Bitmap quality
	
	private int itsBitmapQuality;
	
	// Constructor
	
	public GisContacts(Resources resource) {
		itsVerifyContactListener = null;
		itsRequestContactsListener = null;
		itsUpdateContactListener = null;
		itsRequestContactListener = null;
		
		// Default
		itsBitmapQuality = defaultBitmapQuality;
		itsDefaultBitmap = BitmapFactory.decodeResource(resource, R.drawable.portrait);
	}
	
	// Public
	
	public void setBitmapQuality(int quality) {
		itsBitmapQuality = quality; 
	}
	
	public void requestContact(int id, RequestContactListener listener) {
		// Save callback
		itsRequestContactListener = listener;
		
		// Setup param list with just id of contact
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("id", String.valueOf(id))); 
		
		// Define and instantiate listener for HttpAccess
		IHttpResult httpResult = new HttpResult() {
			@Override
			public void gotOutputStr(String outputStr) {
				if(itsRequestContactListener != null) {
					// Go from string -> json array -> categories, report results
					JSONArray jsonArray;
					try {
						jsonArray = new JSONArray(outputStr);
					} catch (JSONException e) {
						jsonArray = new JSONArray();
					}
					ArrayList<Contact> contacts = getContacts(jsonArray);
					// Should only have one contact here. If not signal problem
					if( contacts.size() == 1 ) {
						Contact contact = contacts.get(0);
						itsRequestContactListener.gotContact(contact);
					}
					else if( contacts.size() > 1 ) {
						Contact contact = contacts.get(0);
						itsRequestContactListener.gotContact(contact);
						System.out.println("GisContacts: More than contact returned when only 1 was expected. Returned 1st contact.");
					}
					else {
						itsRequestContactListener.gotContact(null);
						System.out.println("GisContacts: No contact matching id found. Returned null.");
					}
				}
			}
		};
		
		// Request update of contact.
		String gisGetIncidentsUrlStr = gisUrlStr + gisGetContactPathStr;
		itsHttpAccess.requestStringForHttpGet(gisGetIncidentsUrlStr, params, httpResult);
	}
	
	public void updateContact(IContact contact, UpdateContactListener updateContactListener) {
		// Save callback
		itsUpdateContactListener = updateContactListener;
		itsContact = contact;	// For verification
		
		// Setup params for http get which will deposit params into db
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(httpKey_dbId, String.valueOf(contact.getDbId()))); 
		params.add(new BasicNameValuePair(httpKey_name, contact.getName())); 
		params.add(new BasicNameValuePair(httpKey_locationFrom, contact.getLocationFrom())); 
		params.add(new BasicNameValuePair(httpKey_email, contact.getEmail())); 
		params.add(new BasicNameValuePair(httpKey_biography, contact.getBiography())); 
		params.add(new BasicNameValuePair(httpKey_phoneOffice, contact.getPhoneOffice())); 
		params.add(new BasicNameValuePair(httpKey_phoneMobile, contact.getPhoneMobile())); 
		params.add(new BasicNameValuePair(httpKey_phoneHome, contact.getPhoneHome())); 
		params.add(new BasicNameValuePair(httpKey_password, contact.getPassword())); 
		
		// Store bitmap as String if its not null or default. If either, then store "none"
		if( contact.getBitmap() != itsDefaultBitmap ) {
			String bitmapStr = Gis.getStringForBitmap(contact.getBitmap(), itsBitmapQuality);
			if(bitmapStr != null) {
				params.add(new BasicNameValuePair(httpKey_imgString, bitmapStr)); 
				// Update contact with decoded image so bitmaps match during verification
				Bitmap bitmapDecoded = Gis.getDecodedBitmapForBitmap(contact.getBitmap(), itsBitmapQuality);
				contact.setBitmap(bitmapDecoded);
			}
			else {
				params.add(new BasicNameValuePair(httpKey_imgString, "none")); 
			}
		}
		else {
			params.add(new BasicNameValuePair(httpKey_imgString, "none")); 
		}
		
		// Define and instantiate listener for HttpAccess
		IHttpResult httpResult = new HttpResult() {
			@Override
			public void gotOutputStr(String outputStr) {
				// Verify contact
				if(itsUpdateContactListener != null ) {
					if( outputStr != null ) {
						if( outputStr.trim().equals("OK") ) {
							VerifyContactListener verifyContactListener = new VerifyContactListener() {
								@Override
								public void verifyContactResult(boolean isVerified) {
									itsUpdateContactListener.updateContactResult(isVerified);
								}
							};
							verifyContact(itsContact, verifyContactListener);
						}
						else {
							itsUpdateContactListener.updateContactResult(false);
							System.out.println("GisContacts: Database did not report OK after update.");
						}
					}
					else {
						itsUpdateContactListener.updateContactResult(false);
						System.out.println("GisContacts: Null returned from database after update.");
					}
				}
			}
		};
		
		// Request update of contact.
		String gisGetIncidentsUrlStr = gisUrlStr + gisUpdateContactPathStr;
		itsHttpAccess.requestStringForHttpGet(gisGetIncidentsUrlStr, params, httpResult);
	}
	
	public void verifyContact(IContact contactIn, VerifyContactListener verifyContactListener) {
		// Save callback
		itsVerifyContactListener = verifyContactListener;
		itsContact = contactIn;

		RequestContactListener requestContactListener = new RequestContactListener() {
			@Override
			public void gotContact(Contact contact) {
				if( itsVerifyContactListener != null ) {
					boolean isVerified = itsContact.equals(contact);
					itsVerifyContactListener.verifyContactResult(isVerified);
				}
			}
		};
		this.requestContact(contactIn.getDbId(), requestContactListener);
	}
	
	public void requestContacts(RequestContactsListener listener) {
		// Keep callback
		itsRequestContactsListener = listener;
		
		// Setup params for http get. (No params here, just leave empty)
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		
		// Define and instantiate listener for HttpAccess
		IHttpResult httpResult = new HttpResult() {
			@Override
			public void gotOutputStr(String outputStr) {
				// Go from string -> json array -> categories, report results
				JSONArray jsonArray;
				try {
					jsonArray = new JSONArray(outputStr);
				} catch (JSONException e) {
					jsonArray = new JSONArray();
				}
				ArrayList<Contact> contacts = getContacts(jsonArray);
				itsRequestContactsListener.gotContacts(contacts);
			}
		};
		
		// Request string of http get
		String gisGetIncidentsUrlStr = gisUrlStr + gisGetContactsPathStr;
		itsHttpAccess.requestStringForHttpGet(gisGetIncidentsUrlStr, params, httpResult);
	}
	
	// Private
	
	private ArrayList<Contact> getContacts(JSONArray jsonArray) {
		// Create array of places from JSONArray
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		for(int i=0; i<jsonArray.length(); i++) { 
			// Get an item to query
			JSONObject jsonObj = null;
			try {
				jsonObj = jsonArray.getJSONObject(i);
				Contact contact = createContact(jsonObj);
				if( contact != null ) {
					contacts.add(contact);
				}
			} catch (JSONException e) {
				System.out.println("GisContacts: Unable to get jsonObj from jsonArray. Contact skipped.");
			}
		}	
		return contacts;
	}
	
	private Contact createContact(JSONObject jsonObj) {
		Contact contact = null;
		if( jsonObj != null ) {
			contact = new Contact();
			// Assume these strings always valid
			contact.setDbId(Gis.getIntWithKey(jsonKey_dbId, jsonObj));
			contact.setName(Gis.getStringWithKey(jsonKey_name, jsonObj));
			
			// Adjust result to null depending on string returned
			contact.setLocationFrom(translateJsonObj(jsonKey_locationFrom, jsonObj));
			contact.setEmail(translateJsonObj(jsonKey_email, jsonObj));
			contact.setBiography(translateJsonObj(jsonKey_biography, jsonObj));
			contact.setPassword(translateJsonObj(jsonKey_password, jsonObj));
			contact.setPhoneOffice(translateJsonObj(jsonKey_phoneOffice, jsonObj));
			contact.setPhoneMobile(translateJsonObj(jsonKey_phoneMobile, jsonObj));
			contact.setPhoneHome(translateJsonObj(jsonKey_phoneHome, jsonObj));
			
			// Set bitmap null if "none"
			String bitmapStr = translateJsonObj(jsonKey_imgString, jsonObj);
			if( bitmapStr.equalsIgnoreCase("") ) {
				contact.setBitmap(itsDefaultBitmap);
			}
			else {	
				Bitmap bitmap = Gis.getBitmapWithKey(jsonKey_imgString, jsonObj);
				if(  bitmap != null ) {
					contact.setBitmap(bitmap);
				}
				else {
					contact.setBitmap(itsDefaultBitmap);
				}
			}
		}
		return contact;
	}
	
	private String translateJsonObj(String jsonKey, JSONObject jsonObj) {
		String stringOut = null;
		String stringIn = Gis.getStringWithKey(jsonKey, jsonObj).trim();
		if( stringIn.equalsIgnoreCase("none") ) {
			stringOut = "";
		} else {
			stringOut = stringIn;
		}
		return stringOut;
	}
	
	// Callback for results
	
	public abstract static class VerifyContactListener {
		public abstract void verifyContactResult(boolean isVerified);
	}
	
	public abstract static class RequestContactListener {
		public abstract void gotContact(Contact contact);
	}
	
	public abstract static class UpdateContactListener {
		public abstract void updateContactResult(boolean isContactUpdated);
	}
	
	public abstract static class RequestContactsListener {
		public abstract void gotContacts(ArrayList<Contact> contacts);
	}
	
}
