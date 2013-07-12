/*
 * Released under MIT License http://opensource.org/licenses/MIT
 * Copyright (c) 2013 Plasty Grove
 * Refer to file LICENSE or URL above for full text 
 */

package com.blueserial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.blueserial.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Homescreen extends Activity {

	private Button mBtnSearch;
	private Button mBtnConnect;
	private ListView mLstDevices;

	private BluetoothAdapter mBTAdapter;

	private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
	private static final int SETTINGS = 20;

	private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	// (http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createInsecureRfcommSocketToServiceRecord%28java.util.UUID%29)

	private int mBufferSize = 50000; //Default
	public static final String DEVICE_EXTRA = "com.blueserial.SOCKET";
	public static final String DEVICE_UUID = "com.blueserial.uuid";
	private static final String DEVICE_LIST = "com.blueserial.devicelist";
	private static final String DEVICE_LIST_SELECTED = "com.blueserial.devicelistselected";
	public static final String BUFFER_SIZE = "com.blueserial.buffersize";
	private static final String TAG = "BlueTest5-Homescreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_homescreen);
		ActivityHelper.initialize(this); //This is to ensure that the rotation persists across activities and not just this one
		Log.d(TAG, "Created");

		mBtnSearch = (Button) findViewById(R.id.btnSearch);
		mBtnConnect = (Button) findViewById(R.id.btnConnect);

		mLstDevices = (ListView) findViewById(R.id.lstDevices);
		/*
		 *Check if there is a savedInstanceState. If yes, that means the onCreate was probably triggered by a configuration change
		 *like screen rotate etc. If that's the case then populate all the views that are necessary here 
		 */
		if (savedInstanceState != null) {
			ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
			if(list!=null){
				initList(list);
				MyAdapter adapter = (MyAdapter)mLstDevices.getAdapter();
				int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);
				if(selectedIndex != -1){
					adapter.setSelectedIndex(selectedIndex);
					mBtnConnect.setEnabled(true);
				}
			} else {
				initList(new ArrayList<BluetoothDevice>());
			}
			
		} else {
			initList(new ArrayList<BluetoothDevice>());
		}
		

		mBtnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mBTAdapter = BluetoothAdapter.getDefaultAdapter();

				if (mBTAdapter == null) {
					Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
				} else if (!mBTAdapter.isEnabled()) {
					Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBT, BT_ENABLE_REQUEST);
				} else {
					new SearchDevices().execute();
				}
			}
		});

		mBtnConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				BluetoothDevice device = ((MyAdapter) (mLstDevices.getAdapter())).getSelectedItem();
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.putExtra(DEVICE_EXTRA, device);
				intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
				intent.putExtra(BUFFER_SIZE, mBufferSize);
				startActivity(intent);
			}
		});
	}

	/**
	 * Called when the screen rotates. If this isn't handled, data already generated is no longer available
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MyAdapter adapter = (MyAdapter) (mLstDevices.getAdapter());
		ArrayList<BluetoothDevice> list = (ArrayList<BluetoothDevice>) adapter.getEntireList();
		
		if (list != null) {
			outState.putParcelableArrayList(DEVICE_LIST, list);
			int selectedIndex = adapter.selectedIndex;
			outState.putInt(DEVICE_LIST_SELECTED, selectedIndex);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BT_ENABLE_REQUEST:
			if (resultCode == RESULT_OK) {
				msg("Bluetooth Enabled successfully");
				new SearchDevices().execute();
			} else {
				msg("Bluetooth couldn't be enabled");
			}

			break;
		case SETTINGS: //If the settings have been updated
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String uuid = prefs.getString("prefUuid", "Null");
			mDeviceUUID = UUID.fromString(uuid);
			Log.d(TAG, "UUID: " + uuid);
			String bufSize = prefs.getString("prefTextBuffer", "Null");
			mBufferSize = Integer.parseInt(bufSize);

			String orientation = prefs.getString("prefOrientation", "Null");
			Log.d(TAG, "Orientation: " + orientation);
			if (orientation.equals("Landscape")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if (orientation.equals("Portrait")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (orientation.equals("Auto")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Quick way to call the Toast
	 * @param str
	 */
	private void msg(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Initialize the List adapter
	 * @param objects
	 */
	private void initList(List<BluetoothDevice> objects) {
		final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
		mLstDevices.setAdapter(adapter);
		mLstDevices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				adapter.setSelectedIndex(position);
				mBtnConnect.setEnabled(true);
			}
		});
	}

	/**
	 * Searches for paired devices. Doesn't do a scan! Only devices which are paired through Settings->Bluetooth
	 * will show up with this. I didn't see any need to re-build the wheel over here
	 * @author ryder
	 *
	 */
	private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

		@Override
		protected List<BluetoothDevice> doInBackground(Void... params) {
			Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
			List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
			for (BluetoothDevice device : pairedDevices) {
				listDevices.add(device);
			}
			return listDevices;

		}

		@Override
		protected void onPostExecute(List<BluetoothDevice> listDevices) {
			super.onPostExecute(listDevices);
			if (listDevices.size() > 0) {
				MyAdapter adapter = (MyAdapter) mLstDevices.getAdapter();
				adapter.replaceItems(listDevices);
			} else {
				msg("No paired devices found, please pair your serial BT device and try again");
			}
		}

	}

	/**
	 * Custom adapter to show the current devices in the list. This is a bit of an overkill for this 
	 * project, but I figured it would be good learning
	 * Most of the code is lifted from somewhere but I can't find the link anymore
	 * @author ryder
	 *
	 */
	private class MyAdapter extends ArrayAdapter<BluetoothDevice> {
		private int selectedIndex;
		private Context context;
		private int selectedColor = Color.parseColor("#abcdef");
		private List<BluetoothDevice> myList;

		public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
			super(ctx, resource, textViewResourceId, objects);
			context = ctx;
			myList = objects;
			selectedIndex = -1;
		}

		public void setSelectedIndex(int position) {
			selectedIndex = position;
			notifyDataSetChanged();
		}

		public BluetoothDevice getSelectedItem() {
			return myList.get(selectedIndex);
		}

		@Override
		public int getCount() {
			return myList.size();
		}

		@Override
		public BluetoothDevice getItem(int position) {
			return myList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			TextView tv;
		}

		public void replaceItems(List<BluetoothDevice> list) {
			myList = list;
			notifyDataSetChanged();
		}

		public List<BluetoothDevice> getEntireList() {
			return myList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			ViewHolder holder;
			if (convertView == null) {
				vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
				holder = new ViewHolder();

				holder.tv = (TextView) vi.findViewById(R.id.lstContent);

				vi.setTag(holder);
			} else {
				holder = (ViewHolder) vi.getTag();
			}

			if (selectedIndex != -1 && position == selectedIndex) {
				holder.tv.setBackgroundColor(selectedColor);
			} else {
				holder.tv.setBackgroundColor(Color.WHITE);
			}
			BluetoothDevice device = myList.get(position);
			holder.tv.setText(device.getName() + "\n   " + device.getAddress());

			return vi;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.homescreen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(Homescreen.this, PreferencesActivity.class);
			startActivityForResult(intent, SETTINGS);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
