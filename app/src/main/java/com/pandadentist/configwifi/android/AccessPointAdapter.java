package com.pandadentist.configwifi.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.configwifi.utils.Constants;

/**
 * The adapter with AccessPoint
 * @author ZhangGuoYin
 */
public class AccessPointAdapter extends BaseAdapter {

	private static final String TAG = AccessPointAdapter.class.getSimpleName();
	private Context mContext;
	private List<AccessPoint> mAccessPoints = new ArrayList<AccessPoint>();
	private LayoutInflater mInflater;
    private String mDefaultSSID;
//    private int mPositionChecked = -1;
    private AccessPoint mAccessPointChecked;

    /**
     * @param context
     * @param defaultSSID the ssid which to control, it will not show the try connect button
     */
	public AccessPointAdapter(Context context, String defaultSSID) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mDefaultSSID = defaultSSID;
	}

	public AccessPointAdapter(Context context) {
		super();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}


	/**
	 * @return the mDefaultSSID
	 */
	public String getDefaultSSID() {
		return mDefaultSSID;
	}

	/**
	 * @param mDefaultSSID the mDefaultSSID to set
	 */
	public void setDefaultSSID(String defaultSSID) {
		this.mDefaultSSID = defaultSSID;
	}

	/**
	 * add the access points and notify data set changed
	 * @param list
	 */
	public void addAccessPoints(List<AccessPoint> list) {

		for (AccessPoint accessPoint : list) {
			if (! AccessPoint.removeDoubleQuotes(accessPoint.getSsid()).equals(mDefaultSSID)) {
				mAccessPoints.add(accessPoint);
			}
		}
		Collections.sort(mAccessPoints);
		notifyDataSetChanged();
	}

	/**
	 * clear the all of current access points list and add the new access points, and then notify data set changed
	 * @param list
	 */
	public void updateAccessPoints(List<AccessPoint> list) {

		synchronized (mAccessPoints) {

			for (int i = 0; i < list.size(); i++) {
				if (AccessPoint.removeDoubleQuotes(list.get(i).getSsid()).equals(mDefaultSSID)) {
					list.remove(i);
					i--;
				}
			}

			if (AccessPoint.accessPointsEquals(mAccessPoints, list)) {

				for (int i = 0; i < mAccessPoints.size(); i++) {
					mAccessPoints.set(i, list.get(mAccessPoints.get(i).indexInList(list)));
				}
				notifyDataSetChanged();
			}else {
				mAccessPoints.clear();
				addAccessPoints(list);
			}
		}
	}

	/**
	 * clear the all of current access points list and notify data set changed
	 */
	public void removeAll() {
		mAccessPoints.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mAccessPoints.size();
	}

	@Override
	public AccessPoint getItem(int position) {
		return mAccessPoints.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mAccessPoints.get(position).getNetworkId();
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {

		final AccessPoint accessPoint = mAccessPoints.get(position);
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.wireless_list_item, null);
			holder = new ViewHolder();
			holder.ssidTextView = (TextView)convertView.findViewById(R.id.textView1);
			holder.summaryTextView = (TextView)convertView.findViewById(R.id.textView2);
			holder.signalLevelImageView = (ImageView)convertView.findViewById(R.id.imageView1);
			holder.checkedRadioButton = (RadioButton)convertView.findViewById(R.id.radioButton1);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}

        holder.ssidTextView.setText(accessPoint.getSsid());
        holder.summaryTextView.setText(accessPoint.getSummary());
        if (accessPoint.getRssi() == Integer.MAX_VALUE) {
        	holder.signalLevelImageView.setImageDrawable(null);
        } else {
        	holder.signalLevelImageView.setImageResource(R.drawable.about);
        	holder.signalLevelImageView.setImageState((accessPoint.getSecurity() != AccessPoint.SECURITY_NONE) ?
        			AccessPoint.STATE_SECURED : AccessPoint.STATE_NONE, true);
        }
        int signalLevel = accessPoint.getLevel();
    	holder.signalLevelImageView.setImageLevel(signalLevel);
//    	holder.checkedRadioButton.setClickable(false);
    	convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onItemClicked(accessPoint, position);
				mAccessPointChecked = accessPoint;
				notifyDataSetChanged();
			}
		});
    	convertView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				onItemLongClicked(accessPoint, position);
				return true;
			}
		});
    	if (mAccessPointChecked != null) {
    		holder.checkedRadioButton.setChecked(
    				scanResultEquals(accessPoint.getScanResult(), mAccessPointChecked.getScanResult()));
		}else {
			holder.checkedRadioButton.setChecked(false);
		}

		return convertView;
	}

	private class ViewHolder {
		ImageView signalLevelImageView;
		TextView ssidTextView;
		TextView summaryTextView;
		RadioButton checkedRadioButton;
	}

	public void onItemClicked(AccessPoint accessPoint, int position) {
		Log.d("onItemClicked", "Clicked position: " + position + ",  " + accessPoint + ", " + accessPoint.getScanResult().BSSID);
	}

	public void onItemLongClicked(AccessPoint accessPoint, int position) {
		Log.d("onItemClicked", "LongClicked position: " + position + ",  " + accessPoint);
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		if (mAccessPointChecked != null) {
			synchronized (mAccessPointChecked) {

				boolean find = false;
				for (AccessPoint accessPoint : mAccessPoints) {

					if (scanResultEquals(accessPoint.getScanResult(), mAccessPointChecked.getScanResult())) {
						find = true;
						break;
					}
				}

				if (!find) {
					mAccessPointChecked = null;
					onResetPositionChecked();
				}
			}
		}
	}

	public void onResetPositionChecked() {
		Log.d("onItemClicked", "onResetPositionChecked()");
	}

	/**
	 * @return the mPositionChecked
	 */
	public AccessPoint getSelected() {

		if (mAccessPointChecked == null) {
			return null;
		}

		for (AccessPoint accessPoint : mAccessPoints) {
			if (scanResultEquals(accessPoint.getScanResult(), mAccessPointChecked.getScanResult())) {
				return accessPoint;
			}
		}
		return null;
	}

	public void setSelected(ScanResult scanResult) {
		if (scanResult == null) {
			mAccessPointChecked = null;
			return;
		}

		for (AccessPoint accessPoint : mAccessPoints) {
			if (scanResultEquals(accessPoint.getScanResult(), scanResult)) {
				mAccessPointChecked = accessPoint;
				break;
			}
		}
	}

	public void clearSelected() {
		mAccessPointChecked = null;
	}


	private boolean scanResultEquals(ScanResult one, ScanResult other) {

		if (one!=null && one.BSSID!=null && other!=null && other.BSSID!=null
				&& one.BSSID.trim().equals(other.BSSID.trim())) {
			return true;
		}

		return false;
	}

	private boolean scanResultsEquals(List<ScanResult> one, List<ScanResult> other) {

		if (one == null || other == null || one.size() != other.size()) {
			return false;
		}

		for (ScanResult scanResult : one) {
			if (indexOf(other, scanResult) != -1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * find the first index of a scan result in list
	 * @param scanResults
	 * @param scanResult
	 * @return if -1 not find
	 */
	private int indexOf(List<ScanResult> scanResults, ScanResult scanResult) {

		if (scanResults == null || scanResult == null) {
			return -1;
		}

		for (int i = 0; i < scanResults.size(); i++) {
			if (scanResultEquals(scanResults.get(i), scanResult)) {
				return i;
			}
		}

		return -1;
	}
}
