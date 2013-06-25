package gov.howardcounty.ciogroup;

import android.content.SharedPreferences;

public class Settings {

	public static final String PREFS_NAME = "CioGroupPrefsFile";
	private SharedPreferences itsSharedPreferences;
	
	private static boolean isEmailRememberedDef = false;
	private static String emailRememberedDef = "";
	
	// Storage and defaults
	public boolean isEmailRemembered;
	public String emailRemembered = "";

	public Settings(SharedPreferences sharedPreferences) {
		itsSharedPreferences = sharedPreferences;
		this.loadSettings();
	}

	public void loadSettings() {
		this.isEmailRemembered = itsSharedPreferences.getBoolean("isEmailRemembered", isEmailRememberedDef);
		this.emailRemembered = itsSharedPreferences.getString("EmailRemembered", emailRememberedDef);
	}
	
	public void saveSettings() {
		SharedPreferences.Editor editor = itsSharedPreferences.edit();

		editor.putBoolean("isEmailRemembered", this.isEmailRemembered);
		editor.putString("EmailRemembered", this.emailRemembered);
		
		editor.commit();
	}
	
}
