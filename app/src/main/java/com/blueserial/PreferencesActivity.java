/*
 * Released under MIT License http://opensource.org/licenses/MIT
 * Copyright (c) 2013 Plasty Grove
 * Refer to file LICENSE or URL above for full text 
 */

package com.blueserial;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityHelper.initialize(this);
		addPreferencesFromResource(R.xml.preferences); // Using this for compatibility with Android 2.2 devices
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);

		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
			ActivityHelper.initialize(this);
		}

		if (pref instanceof EditTextPreference) {
			EditTextPreference editPref = (EditTextPreference) pref;
			pref.setSummary(editPref.getText());
		}
	}

	@Override
	protected void onPause() {

		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		Map<String, ?> keys = PreferenceManager.getDefaultSharedPreferences(this).getAll();

		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			// Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
			Preference pref = findPreference(entry.getKey());
			if (pref != null) {
				pref.setSummary(entry.getValue().toString());
			}
		}

		super.onResume();
	}

}
