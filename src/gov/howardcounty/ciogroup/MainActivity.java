package gov.howardcounty.ciogroup;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import gov.howardcountymd.utils.misc.EmailOps;

public class MainActivity extends Activity {
	
	// Contains
	private ListView itsContactsListView;
	private ArrayAdapterContacts itsArrayAdapterContacts;
	private GisContacts itsGisContacts;
	private ArrayList<Contact> itsContactsToAdd;
	private ProgressDialog itsProgressDialog;
	private ContactKnown itsContactKnownSelected;
	private ContactKnown itsContactKnownUser;
	private Bitmap itsImageSelected;
	private Context itsContext;
	private Contact itsUpdatedContact;
	private int itsContactKnownUserPos;
	
	// UI
	private Button itsLoginButton;
	private Button itsLogoutButton;
	private Button itsRefreshButton;
	private TextView itsLoginText;
	private String itsMessageText;
	
	// Edit dialog UI
	private ContactKnown itsEditContactKnown;
	private EditText itsEditName;
	private EditText itsEditLocationFrom;
	private EditText itsEditEmailAddress;
	private EditText itsEditBiography;
	private ImageButton itsImageButton;
	private EditText itsEditPhoneOffice;
	private EditText itsEditPhoneMobile;
	private EditText itsEditPhoneHome;
	
	// Photo selection
	private static final int SELECT_PHOTO = 100;		// Arbitrary for callback to this activity
	private static final int REQUIRED_SIZE = 150;		// Bitmap max dimension for screen
	private static final int MAX_BITMAP_STR_LEN = 3000;	// Max allowable bitmap string length
	private static final int MIN_BITMAP_QUALITY = 50;	// Max allowable compression (0-100)
	
	// Saved settings
	private Settings itsSettings;
	public static String PrefsFilename = "CioGroupPrefsFile";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Contains settings that are saved
		SharedPreferences sharedPreferences = this.getSharedPreferences(PrefsFilename, 0);
		itsSettings = new Settings(sharedPreferences);
		
		// Setup login button listener
		itsLoginButton = (Button) findViewById(R.id.cioGroup_loginButton);
		itsLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				requestLogin();
			}
		});
		
		// Setup logout button listener
		itsLogoutButton = (Button) findViewById(R.id.cioGroup_logoutButton);
		itsLogoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logout();
			}
		});
		
		// Setup refresh button listener
		itsRefreshButton = (Button) findViewById(R.id.cioGroup_refreshButton);
		itsRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshContacts();
			}
		});
		
		// Init
		itsContext = this;
		itsLoginText = (TextView) findViewById(R.id.cioGroup_loginText);
		
		// Start in logged out state
		logout();
		
		// Get contacts from database
		refreshContacts();
	}
	
	private void requestLogin() {
		// Adjust visibility
		itsLoginButton.setVisibility(View.GONE);
		itsRefreshButton.setVisibility(View.GONE);
		itsLoginText.setVisibility(View.GONE);
		
		final Dialog loginDialog = new Dialog(this);
		loginDialog.setContentView(R.layout.cio_group_login_dialog);
		loginDialog.setTitle("Log in");

		OnCancelListener onCancelListener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// Adjust visibility
				resetButtonVisibility();
			}
		};
		loginDialog.setOnCancelListener(onCancelListener);
		
		// Force dialog to full width...
        android.view.WindowManager.LayoutParams params = loginDialog.getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        loginDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);	
		
		// If email is remembered, set it
		CheckBox rememberMeCheckBox = (CheckBox) loginDialog.findViewById(R.id.cioGroup_loginDialog_rememberMeCheckBox);
		rememberMeCheckBox.setChecked(itsSettings.isEmailRemembered);
		if( itsSettings.isEmailRemembered ) {
			EditText editTextLogin = (EditText) loginDialog.findViewById(R.id.cioGroup_loginDialog_loginText);
			editTextLogin.setText(itsSettings.emailRemembered);
		}
		
		// OK button
		Button loginOKButton = (Button) loginDialog.findViewById(R.id.cioGroup_loginDialog_okButton);
		loginOKButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editTextLogin = (EditText) loginDialog.findViewById(R.id.cioGroup_loginDialog_loginText);
				String loginEmail = editTextLogin.getText().toString();
				
				// If email is remembered, keep it, otherwise clear it
				CheckBox rememberMeCheckBox = (CheckBox) loginDialog.findViewById(R.id.cioGroup_loginDialog_rememberMeCheckBox);
				itsSettings.isEmailRemembered = rememberMeCheckBox.isChecked();
				if( itsSettings.isEmailRemembered ) {
					itsSettings.emailRemembered = loginEmail;
				} 
				else {
					itsSettings.emailRemembered = "";
				}
				itsSettings.saveSettings();
				
				// Try to login
				login(loginEmail);
				loginDialog.dismiss();
			}
		});
		
		// Cancel button
		Button loginCancelButton = (Button) loginDialog.findViewById(R.id.cioGroup_loginDialog_cancelButton);
		loginCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loginDialog.dismiss();
				// Adjust visibility
				resetButtonVisibility();
			}
		});
		
		loginDialog.show();
	}
	
	private void login(String loginEmail) {
		// Test that email address is of valid pattern
		if( !EmailOps.isEmailValid(loginEmail) ) {
			showOkDialogWithText("The address entered does not appear to be of proper form. (Example: joe@acme.com)");
		}
		else {
			// Find user in contacts and set button on row to be editable
			String loginEmailTrim = loginEmail.trim();
			for( int i=0; i<itsArrayAdapterContacts.getCount(); i++) {
				ContactKnown contactKnown = itsArrayAdapterContacts.getItem(i);
				String contactEmailTrim = contactKnown.getEmail().trim();
				if(loginEmailTrim.equalsIgnoreCase(contactEmailTrim)) {			
					itsContactKnownUser = contactKnown;
					itsContactKnownUser.setIsMe(true);
					itsContactKnownUserPos = i;
					notifyDataSetChanged();
					itsLoginText.setText("Welcome, " + itsContactKnownUser.getName().trim() + ".");
					// Adjust visibility
					resetButtonVisibility();
					// Keep position, scroll to user name
					itsContactsListView. smoothScrollToPosition(i);
					break;
				}
			}
			if( itsContactKnownUser == null ){
				itsLoginText.setText("Unknown user. Guest access only.");
				// Adjust visibility
				resetButtonVisibility();
			}
		}
	}
	
	private void logout() {
		// Update so user isn't known, refresh
		if( itsContactKnownUser != null ) {
			itsContactKnownUser.setIsMe(false);
			notifyDataSetChanged();
		}

		// No longer needed
		itsContactKnownUser = null;
		itsImageSelected = null;
		
		// Logout state text
		itsLoginText.setText("Login enables contact email and contact info edit.");
		
		// Setup button visibility
		resetButtonVisibility();
	}
	
	private void refreshContacts() {	
		// (Re)init array adapter for list view
		itsArrayAdapterContacts = new ArrayAdapterContacts(this, R.layout.cio_group_contacts_row_view);
		ArrayAdapterContacts.EditContactListener editContactListener = new ArrayAdapterContacts.EditContactListener() {
			@Override
			public void editContact(ContactKnown contactKnown) {
				editMyContactPg1(contactKnown);
			}
		};
		itsArrayAdapterContacts.setEditContactListener(editContactListener);
		
		// Must be done on UI thread
		final Activity hl = this;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Set adapter for the list view
				itsContactsListView = (ListView) findViewById(R.id.cioGroup_contactListView);
				itsContactsListView.setAdapter(itsArrayAdapterContacts);
				OnItemClickListener onClickListener = new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						ContactKnown contactKnown = (ContactKnown) itsContactsListView.getItemAtPosition(position);
						viewContact(contactKnown);
					}
				};
				itsContactsListView.setOnItemClickListener(onClickListener);
			}
		};
		hl.runOnUiThread(runnable);
		
		// Get contacts from GIS Online database
		itsGisContacts = new GisContacts(getResources());
		GisContacts.RequestContactsListener requestContactsListener = new GisContacts.RequestContactsListener() {
			@Override
			public void gotContacts(ArrayList<Contact> contacts) {
				itsProgressDialog.dismiss();
				addContacts(contacts);
			}
		};
		itsGisContacts.requestContacts(requestContactsListener);
		
		// Start progress dialog
		itsProgressDialog = ProgressDialog.show(this,
				"Please wait.", "Retrieving contacts.", true);
	}
	
	private void editMyContactPg1(ContactKnown editContactKnown) {
		// Keep contact
		itsEditContactKnown = editContactKnown;
		
		// Adjust visibility, assuming logged in
		itsLoginText.setVisibility(View.GONE);
		itsLogoutButton.setVisibility(View.GONE);
		itsRefreshButton.setVisibility(View.GONE);
		
		// Setup dialog
		final Dialog editContactDialog = new Dialog(this);
		editContactDialog.setContentView(R.layout.cio_group_edit_contact_view);
		editContactDialog.setTitle("Edit your contact info: 1/3");
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// Adjust visibility
				resetButtonVisibility();
			}
		};
		editContactDialog.setOnCancelListener(onCancelListener);
		
		// Force dialog to full width...
        android.view.WindowManager.LayoutParams params = editContactDialog.getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        editContactDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);	
		
		// Populate fields
		itsImageButton = (ImageButton) editContactDialog.findViewById(R.id.cioGroup_editContactView_imageButton);
        if(itsEditContactKnown.getBitmap() != null) {
        	itsImageButton.setImageBitmap(itsEditContactKnown.getBitmap());
        }
        else {
        	itsImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.portrait));
        }
		
		itsEditName = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView_editName);
		itsEditName.setText(itsEditContactKnown.getName());
		
		itsEditLocationFrom = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView_editLocationFrom);
		itsEditLocationFrom.setText(itsEditContactKnown.getLocationFrom());
		
		// Add listener to imageButton
		OnClickListener imageButtonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Request picture selection from image gallery
			    Intent intent = new Intent();
			    intent.setType("image/*");
			    intent.setAction(Intent.ACTION_GET_CONTENT);
			    intent.addCategory(Intent.CATEGORY_OPENABLE);
			    startActivityForResult(intent, SELECT_PHOTO);
			}
		};
		itsImageButton.setOnClickListener(imageButtonListener);

		// Done button
		Button nextButton = (Button) editContactDialog.findViewById(R.id.cioGroup_editContactView_nextButton);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Copy into temporary contact
				itsUpdatedContact = new Contact();
				itsUpdatedContact.setDbId(itsEditContactKnown.getDbId());	// This doesn't change
				itsUpdatedContact.setName(itsEditName.getText().toString());
				itsUpdatedContact.setLocationFrom(itsEditLocationFrom.getText().toString());
				
				// If selected, use it, otherwise use what's there prev
				if(itsImageSelected != null) {
					itsUpdatedContact.setBitmap(itsImageSelected);
					itsImageSelected = null;
				}
				else {
					itsUpdatedContact.setBitmap(itsEditContactKnown.getBitmap());
				}
				
				// Dismiss dialog, start on next page
				editContactDialog.dismiss();
				editMyContactPg2();
			}
		});
		
		// Cancel button
		Button cancelButton = (Button) editContactDialog.findViewById(R.id.cioGroup_editContactView_cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editContactDialog.dismiss();
				// Adjust visibility
				resetButtonVisibility();
			}
		});
    	
		editContactDialog.show();
	}
	
	private void editMyContactPg2() {
		// Setup dialog
		final Dialog editContactDialog = new Dialog(this);
		editContactDialog.setContentView(R.layout.cio_group_edit_contact_view2);
		editContactDialog.setTitle("Edit your contact info: 2/3");
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// Adjust visibility
				resetButtonVisibility();
			}
		};
		editContactDialog.setOnCancelListener(onCancelListener);
		
		// Force dialog to full width...
        android.view.WindowManager.LayoutParams params = editContactDialog.getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        editContactDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);	
		
		// Populate fields

		itsEditEmailAddress = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView2_editEmailAddress);
		itsEditEmailAddress.setText(itsEditContactKnown.getEmail());

		itsEditPhoneOffice = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView2_editPhoneOffice);
		itsEditPhoneOffice.setText(itsEditContactKnown.getPhoneOffice());

		itsEditPhoneMobile = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView2_editPhoneMobile);
		itsEditPhoneMobile.setText(itsEditContactKnown.getPhoneMobile());

		itsEditPhoneHome = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView2_editPhoneHome);
		itsEditPhoneHome.setText(itsEditContactKnown.getPhoneHome());

		// Done button
		Button nextButton = (Button) editContactDialog.findViewById(R.id.cioGroup_editContactView2_nextButton);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( !EmailOps.isEmailValid(itsEditEmailAddress.getText().toString()) ) {
					showOkDialogWithText("The address entered does not appear to be of proper form. (Example: sales@acme.com)");
				}
				else {
					// Copy into temporary contact
					itsUpdatedContact.setEmail(itsEditEmailAddress.getText().toString());
					itsUpdatedContact.setPhoneOffice(itsEditPhoneOffice.getText().toString());
					itsUpdatedContact.setPhoneMobile(itsEditPhoneMobile.getText().toString());
					itsUpdatedContact.setPhoneHome(itsEditPhoneHome.getText().toString());
			
					// Dismiss dialog, start on next page
					editContactDialog.dismiss();
					editMyContactPg3();
				}
			}
		});
		
		// Cancel button
		Button cancelButton = (Button) editContactDialog.findViewById(R.id.cioGroup_editContactView2_cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editContactDialog.dismiss();
				// Adjust visibility
				resetButtonVisibility();
			}
		});
    	
		editContactDialog.show();
	}
	
	private void editMyContactPg3() {
		// Setup dialog
		final Dialog editContactDialog = new Dialog(this);
		editContactDialog.setContentView(R.layout.cio_group_edit_contact_view3);
		editContactDialog.setTitle("Edit your contact info: 3/3");
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// Adjust visibility
				resetButtonVisibility();
			}
		};
		editContactDialog.setOnCancelListener(onCancelListener);
		
		// Force dialog to full width...
        android.view.WindowManager.LayoutParams params = editContactDialog.getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        editContactDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);	
		
		// Populate fields
		
		itsEditBiography = (EditText) editContactDialog.findViewById(R.id.cioGroup_editContactView3_editBiography);
		itsEditBiography.setText(itsEditContactKnown.getBiography());

		// Done button
		Button doneButton = (Button) editContactDialog.findViewById(R.id.cioGroup_editContactView3_doneButton);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( !EmailOps.isEmailValid(itsEditEmailAddress.getText().toString()) ) {
					showOkDialogWithText("The address entered does not appear to be of proper form. (Example: sales@acme.com)");
				}
				else {
					// Copy into temporary contact
					itsUpdatedContact.setBiography(itsEditBiography.getText().toString());
					
					// Update the database
					GisContacts.UpdateContactListener updateContactListener = new GisContacts.UpdateContactListener() {
						@Override
						public void updateContactResult(boolean isContactUpdated) {
							// Need to know if name has been changed (significantly)
							String nameTrim = itsEditContactKnown.getName().trim();
							String updatedNameTrim = itsUpdatedContact.getName().trim();
							boolean isNameChanged = !nameTrim.equalsIgnoreCase(updatedNameTrim);
							
							// Ok to update contact on list (better to keep than discard if not updated in db)
							itsEditContactKnown.setName(itsUpdatedContact.getName());
							itsEditContactKnown.setLocationFrom(itsUpdatedContact.getLocationFrom());
							itsEditContactKnown.setEmail(itsUpdatedContact.getEmail());
							itsEditContactKnown.setBiography(itsUpdatedContact.getBiography());
							itsEditContactKnown.setBitmap(itsUpdatedContact.getBitmap());
							
							// Dismiss dialog, reset buttons
							itsProgressDialog.dismiss();
							resetButtonVisibility();
							
							// If contact is updated, prefer to just update list, but if name changes need to refresh all contacts
							if(isContactUpdated) {
								if( isNameChanged ) {
									// Refresh entire contact list so contact is reordered properly in list
									refreshContacts();
								}
								else {
									// Contact still in proper place in list. Just update the list and scroll back to it.
									notifyDataSetChanged();
								}
							}
							else {
								showOkDialogWithText("Unable to save changes to database right now. Trying saving changes again later.");
								// Contact may not be in proper place, but don't want to overwrite user changes
								notifyDataSetChanged();
							}	
						}
					};
					
					int bitmapMaxQuality = getBitmapMaxQuality(itsUpdatedContact.getBitmap());
					itsGisContacts.setBitmapQuality(bitmapMaxQuality);
					itsGisContacts.updateContact(itsUpdatedContact, updateContactListener);
			
					// Dismiss dialog
					editContactDialog.dismiss();
					
					// Start progress dialog
					itsProgressDialog = ProgressDialog.show(itsContext,
							"Please wait.", "Updating contact info.", true);
				}
			}
		});
		
		// Cancel button
		Button cancelButton = (Button) editContactDialog.findViewById(R.id.cioGroup_editContactView3_cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editContactDialog.dismiss();
				// Adjust visibility
				resetButtonVisibility();
			}
		});
    	
		editContactDialog.show();
	}
	

	private void showOkDialogWithText(String messageText) {		
		// Must be done on UI thread
		final Activity hl = this;
		itsMessageText = messageText;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Builder builder = new AlertDialog.Builder(itsContext);
				builder.setMessage(itsMessageText);
				builder.setCancelable(true);
				builder.setPositiveButton("OK", null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		};
		hl.runOnUiThread(runnable);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) { 
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImageUri = imageReturnedIntent.getData();
	            try {
					Bitmap bitmap = decodeUri(selectedImageUri);
					itsImageButton.setImageBitmap(bitmap);
					itsImageSelected = bitmap;
				} catch (FileNotFoundException e) {
					System.out.println("ActivityResult presented unknown resultCode. Ignored.");
				}
	        }
	    }
	}
	
	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        int scale = 1;
        
        // First get to good screen size (safe mem requirements too)
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);    
		int width_tmp = o.outWidth;
		int height_tmp = o.outHeight;
		
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE
				|| height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
        
        // Now get under string length limit at max compression at min quality
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        String bitmapStr = GisContacts.getStringForBitmap(bitmap, MIN_BITMAP_QUALITY);
        while( (bitmapStr.length() > MAX_BITMAP_STR_LEN) ) {
        	// Should be scaled by 2
			scale *= 2;
		    o2.inSampleSize = scale;
		    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
		    bitmapStr = GisContacts.getStringForBitmap(bitmap, MIN_BITMAP_QUALITY);
        }
           
        return bitmap;
    }
	
	private int getBitmapMaxQuality(Bitmap bitmap) {
        // Find maximum quality that bitmap string won't exceed limit  
		int bitmapMaxQuality;
        int delta = 10;
        for( bitmapMaxQuality = MIN_BITMAP_QUALITY + delta; bitmapMaxQuality<=100; bitmapMaxQuality+=delta) {
		    String bitmapStr = GisContacts.getStringForBitmap(bitmap, bitmapMaxQuality);
		    if(bitmapStr.length() > MAX_BITMAP_STR_LEN) {
		    	bitmapMaxQuality -= delta;
		    	break;
		    }
        }
        return bitmapMaxQuality;
	}
	
	private void resetButtonVisibility() {
		// Must be done on UI thread
		final Activity hl = this;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Adjust visibility
				itsLoginText.setVisibility(View.VISIBLE);
				itsRefreshButton.setVisibility(View.VISIBLE);
				if( itsContactKnownUser != null ) {
					itsLoginButton.setVisibility(View.GONE);
					itsLogoutButton.setVisibility(View.VISIBLE);
				}
				else {
					itsLoginButton.setVisibility(View.VISIBLE);
					itsLogoutButton.setVisibility(View.GONE);
				}
			}
		};
		hl.runOnUiThread(runnable);
	}
	
	private void notifyDataSetChanged() {
		// Must be done on UI thread
		final Activity hl = this;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				itsArrayAdapterContacts.notifyDataSetChanged();
				// Scroll to user if known
				if( itsContactKnownUser != null ) {
					itsContactsListView.smoothScrollToPosition(itsContactKnownUserPos);
					// In case user changed name
					itsLoginText.setText("Welcome, " + itsContactKnownUser.getName().trim() + ".");
				}
			}
		};
		hl.runOnUiThread(runnable);
	}
	
	private void addContacts(ArrayList<Contact> contactsToAdd) {
		// Must be done on UI thread
		if( contactsToAdd != null ) {
			itsContactsToAdd = contactsToAdd;
			final Activity hl = this;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					for( int i=0; i<itsContactsToAdd.size(); i++ ) {
						Contact contact = itsContactsToAdd.get(i);
						// Decorate with ContactKnown
						ContactKnown contactKnown = new ContactKnown(contact);
						itsArrayAdapterContacts.add(contactKnown);
						// If user is logged in (not null) match by email and reset known contact
						if( itsContactKnownUser != null ) {
							String contactEmailTrim = contactKnown.getEmail().trim();
							String contactEmailUserTrim = itsContactKnownUser.getEmail().trim();
							if(contactEmailUserTrim.equalsIgnoreCase(contactEmailTrim)) {			
								contactKnown.setIsMe(true);
								itsContactKnownUser = contactKnown;
								itsContactKnownUserPos = i;
							}
						}
					}
					notifyDataSetChanged();
				}
			};
			hl.runOnUiThread(runnable);
		}
	}
	
	private void viewContact(ContactKnown contactKnown) {
		itsContactKnownSelected = contactKnown;
		
		// Adjust visibility, may or may not be logged in
		itsLoginText.setVisibility(View.GONE);
		itsLoginButton.setVisibility(View.GONE);
		itsLogoutButton.setVisibility(View.GONE);
		itsRefreshButton.setVisibility(View.GONE);
		
		// Custom dialog for actions to take
		final Dialog contactDialog = new Dialog(this);
		contactDialog.setTitle("Contact Information");
		contactDialog.setContentView(R.layout.cio_group_contact_view);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// Adjust visibility
				resetButtonVisibility();
			}
		};
		contactDialog.setOnCancelListener(onCancelListener);

		// Force dialog to full width...
        android.view.WindowManager.LayoutParams params = contactDialog.getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        contactDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);	
		
		// Send email button handling
		Button emailButton = (Button) contactDialog.findViewById(R.id.cioGroup_contactView_sendEmailButton);
		emailButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open for email sending
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{itsContactKnownSelected.getEmail()});
				i.putExtra(Intent.EXTRA_SUBJECT, "Enter subject");
				i.putExtra(Intent.EXTRA_TEXT   , "Enter email body");
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
					System.out.println("There are no email clients installed.");
				}
				
				// Dismiss dialog
				contactDialog.dismiss();
				
				// Adjust visibility
				resetButtonVisibility();
			}
		});
		
		// Done button handling
		Button doneButton = (Button) contactDialog.findViewById(R.id.cioGroup_contactView_doneButton);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				contactDialog.dismiss();
				// Adjust visibility
				resetButtonVisibility();
			}
		});

		// Locate fields to populate
        ImageView imageView = (ImageView) contactDialog.findViewById(R.id.cioGroup_contactView_imageView);
        TextView nameTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_nameTextView);
        TextView locationFromTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_fromLocationTextView);
        TextView emailTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_emailTextView);
        TextView biographyTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_biographyTextView);
        TextView phoneOfficeTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_officeTextView);
        TextView phoneMobileTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_mobileTextView);
        TextView phoneHomeTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_homeTextView);
        
        // Headers
        TextView phoneOfficeHdrTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_officeHdrTextView);
        TextView phoneMobileHdrTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_mobileHdrTextView);
        TextView phoneHomeHdrTextView = (TextView) contactDialog.findViewById(R.id.cioGroup_contactView_homeHdrTextView);
        
        // Populate
        nameTextView.setText(contactKnown.getName());
        imageView.setImageBitmap(contactKnown.getBitmap());
        locationFromTextView.setText(contactKnown.getLocationFrom());
        biographyTextView.setText(contactKnown.getBiography());
        phoneOfficeTextView.setText(contactKnown.getPhoneOffice());
        phoneMobileTextView.setText(contactKnown.getPhoneMobile());
        phoneHomeTextView.setText(contactKnown.getPhoneHome());
       
        // Turn off headers if info isn't avail... looks better
        if(contactKnown.getPhoneOffice().equalsIgnoreCase("")) {
        	phoneOfficeHdrTextView.setVisibility(View.GONE);
        }
        
        if(contactKnown.getPhoneMobile().equalsIgnoreCase("")) {
        	phoneMobileHdrTextView.setVisibility(View.GONE);
        }
        
        if(contactKnown.getPhoneOffice().equalsIgnoreCase("")) {
        	phoneHomeHdrTextView.setVisibility(View.GONE);
        }

        // Show email and email button only if user is known
        if( itsContactKnownUser != null ) {
        	emailTextView.setText(contactKnown.getEmail());
        }
        else {
        	emailButton.setVisibility(View.GONE);
        	emailTextView.setVisibility(View.GONE);
        	phoneOfficeTextView.setVisibility(View.GONE);
        	phoneMobileTextView.setVisibility(View.GONE);
        	phoneHomeTextView.setVisibility(View.GONE);
        	phoneOfficeHdrTextView.setVisibility(View.GONE);
        	phoneMobileHdrTextView.setVisibility(View.GONE);
        	phoneHomeHdrTextView.setVisibility(View.GONE);
        }
        
		// Ready, now show it.
		contactDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
