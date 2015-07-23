package cn.jon.detect;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class FaceUtils {

	public interface CallBack {
		void sucessful(JSONObject mResult);

		void error(FaceppParseException exception);

	}

	public static void FaceParser(final Bitmap mBitmap, final CallBack callBack) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				HttpRequests mRequest = null;
				JSONObject mJsonObject = null;
				try {
					mRequest = new HttpRequests(Constant.API_KEY,
							Constant.API_SECRET, true, true);
					Bitmap mSmallBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
							mBitmap.getWidth(), mBitmap.getHeight());
					byte[] mByte = Bitmap2Bytes(mSmallBitmap);
					PostParameters mParams = new PostParameters();
					mParams.setImg(mByte);
					mJsonObject = ((HttpRequests) mRequest)
							.detectionDetect(mParams);
				} catch (FaceppParseException e) {
					if (null != callBack) {
						callBack.error(e);
					}
				}

				if (null != callBack) {
					callBack.sucessful(mJsonObject);
				}

			}
		}).start();

	}

	/**
	 * 将Bitmap转化为byte[]字节数组
	 * 
	 * @param mBitmap
	 * @return
	 */
	public static byte[] Bitmap2Bytes(Bitmap mBitmap) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

		return baos.toByteArray();

	}

}
