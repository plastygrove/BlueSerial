/*
 * Released under MIT License http://opensource.org/licenses/MIT
 * Copyright (c) 2013 Plasty Grove
 * Refer to file LICENSE or URL above for full text 
 */

package com.blueserial;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;

public class ActivityHelper {
	public static void initialize(Activity activity) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		String orientation = prefs.getString("prefOrientation", "Null");
		if ("Landscape".equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if ("Portrait".equals(orientation)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
	}
}
