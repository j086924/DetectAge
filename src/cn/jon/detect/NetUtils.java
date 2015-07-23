package cn.jon.detect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {

	private NetUtils() {
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 其他网络链接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null != cm) {
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (null != info && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}

			}
		}// if

		return false;

	}

	/**
	 * 是否为wifi链接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifi(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == cm) {
			return false;
		} else {
			return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
		}

	}

}
