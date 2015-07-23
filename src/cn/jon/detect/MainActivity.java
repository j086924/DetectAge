package cn.jon.detect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.jon.detect.FaceUtils.CallBack;

import com.facepp.error.FaceppParseException;

public class MainActivity extends Activity implements OnClickListener {

	private Button mButton;
	private Button mDetectButton;
	private Button mSaveButton;
	private Button mCaptureButton;
	private ImageView mImageView;
	private Bitmap mPhotoBitmap;
	private Paint mPaint;
	View mWaiting;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constant.SUCCESS:
				mWaiting.setVisibility(View.GONE);
				JSONObject mJsonObject = (JSONObject) msg.obj;
				if (null != mJsonObject) {
					parseJsonObject(mJsonObject);
				}
				mImageView.setImageBitmap(mPhotoBitmap);
				break;
			case Constant.FAILED:
				mWaiting.setVisibility(View.GONE);
				Toast.makeText(MainActivity.this, "网络异常，请重试！",
						Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}

		/**
		 * 解析返回的json数据 x获取的是face中心点x坐标的位置 y获取的是face中心点y坐标的位置 width获取的是face显示的宽度
		 * height获取的是face显示的高度 gender获取性别 age获取年龄
		 * 
		 * @param mJsonObject
		 */
		private void parseJsonObject(JSONObject mJsonObject) {

			try {

				Bitmap mBitmap = Bitmap.createBitmap(mPhotoBitmap.getWidth(),
						mPhotoBitmap.getHeight(), mPhotoBitmap.getConfig());
				Canvas mCanvas = new Canvas(mBitmap);
				mCanvas.drawBitmap(mPhotoBitmap, 0, 0, mPaint);

				JSONArray faces = mJsonObject.getJSONArray("face");
				int faceCount = faces.length();
				for (int i = 0; i < faceCount; i++) {
					JSONObject posiObject = faces.getJSONObject(i)
							.getJSONObject("position");
					Double x = posiObject.getJSONObject("center")
							.getDouble("x");
					Double y = posiObject.getJSONObject("center")
							.getDouble("y");

					Double width = posiObject.getDouble("width");
					Double height = posiObject.getDouble("height");

					JSONObject attribute = faces.getJSONObject(i)
							.getJSONObject("attribute");
					String gender = attribute.getJSONObject("gender")
							.getString("value");
					int age = attribute.getJSONObject("age").getInt("value");

					float mCenterX = (float) (x / 100 * mBitmap.getWidth());
					float mCenterY = (float) (y / 100 * mBitmap.getHeight());
					float mLineWidth = (float) (width / 100 * mBitmap
							.getWidth());
					float mLineHeight = (float) (height / 100 * mBitmap
							.getHeight());

					mPaint.setColor(Color.RED);
					mPaint.setStrokeWidth(3);

					// 画脸部的矩形框
					mCanvas.drawLine(mCenterX - mLineWidth / 2, mCenterY
							- mLineHeight / 2, mCenterX - mLineWidth / 2,
							mCenterY + mLineHeight / 2, mPaint);
					mCanvas.drawLine(mCenterX - mLineWidth / 2, mCenterY
							- mLineHeight / 2, mCenterX + mLineWidth / 2,
							mCenterY - mLineHeight / 2, mPaint);
					mCanvas.drawLine(mCenterX - mLineWidth / 2, mCenterY
							+ mLineHeight / 2, mCenterX + mLineWidth / 2,
							mCenterY + mLineHeight / 2, mPaint);
					mCanvas.drawLine(mCenterX + mLineWidth / 2, mCenterY
							- mLineHeight / 2, mCenterX + mLineWidth / 2,
							mCenterY + mLineHeight / 2, mPaint);

					Bitmap mAgeBitmap = buiLdAgeBitmap(age, gender);
					// 将图片和imageView的宽高进行对比得出最大的比例，使mAgeBitmap不会超过图片的大小，并尽可能适中
					if (mBitmap.getWidth() < mPhotoBitmap.getWidth()
							&& mBitmap.getHeight() < mPhotoBitmap.getHeight()) {

						float rate = Math.max(mBitmap.getWidth() * 1.0f
								/ mPhotoBitmap.getWidth(), mBitmap.getHeight()
								* 1.0f / mPhotoBitmap.getHeight());
						mAgeBitmap = Bitmap.createScaledBitmap(mAgeBitmap,
								(int) (mAgeBitmap.getWidth() * rate),
								(int) (mAgeBitmap.getHeight() * rate), false);

					}

					mCanvas.drawBitmap(mAgeBitmap,
							mCenterX - mAgeBitmap.getWidth() / 2, mCenterY
									- mLineHeight / 2 - mAgeBitmap.getHeight(),
							null);

				}

				mPhotoBitmap = mBitmap;
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		/**
		 * 将TextView以Bitmap的形式呈现
		 * 
		 * @param age
		 * @param gender
		 * @return
		 */
		private Bitmap buiLdAgeBitmap(int age, String gender) {
			TextView mAgeText = (TextView) mWaiting.findViewById(R.id.age_text);
			mAgeText.setText(age + " ");
			if (gender.equalsIgnoreCase("Female")) {
				mAgeText.setCompoundDrawablesWithIntrinsicBounds(getResources()
						.getDrawable(R.drawable.female), null, null, null);
			} else {
				mAgeText.setCompoundDrawablesWithIntrinsicBounds(getResources()
						.getDrawable(R.drawable.male), null, null, null);
			}
			mAgeText.setDrawingCacheEnabled(true);
			Bitmap mBitmap = Bitmap.createBitmap(mAgeText.getDrawingCache());
			mAgeText.destroyDrawingCache();
			return mBitmap;
		};

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		initListeners();
		initPaints();

	}

	private void initPaints() {
		mPaint = new Paint();
	}

	private void initListeners() {
		mButton.setOnClickListener(this);
		mDetectButton.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		mCaptureButton.setOnClickListener(this);

	}

	private void initViews() {
		mButton = (Button) findViewById(R.id.button);
		mDetectButton = (Button) findViewById(R.id.detect_button);
		mSaveButton = (Button) findViewById(R.id.save_button);
		mCaptureButton = (Button) findViewById(R.id.capture_button);
		mImageView = (ImageView) findViewById(R.id.img);
		mWaiting = (View) findViewById(R.id.waiting);
		setDefaultBitmap();

	}

	private void setDefaultBitmap() {
		mPhotoBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.girl);
		mImageView.setImageBitmap(mPhotoBitmap);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.button:
			Intent mIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			mIntent.setType("image/*");
			startActivityForResult(mIntent, Constant.PICK_CODE);

			break;

		case R.id.detect_button:

			if (null == mPhotoBitmap) {
				Toast.makeText(MainActivity.this, "请加载照片！", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			mWaiting.setVisibility(View.VISIBLE);

			FaceUtils.FaceParser(mPhotoBitmap, new CallBack() {

				@Override
				public void sucessful(JSONObject mResult) {

					Message msg = new Message();
					msg.what = Constant.SUCCESS;
					msg.obj = mResult;
					mHandler.sendMessage(msg);

				}

				@Override
				public void error(FaceppParseException exception) {
					Message msg = new Message();
					msg.what = Constant.FAILED;
					mHandler.sendMessage(msg);
				}
			});

			break;

		case R.id.save_button:
			savePicture();

			break;

		case R.id.capture_button:
			Intent mIntent2 = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(mIntent2, Constant.CAPTURE_CODE);
			break;

		default:
			break;
		}

	}

	/**
	 * 保存图片到mnt/sdcard/face/picture文件夹下
	 */
	private void savePicture() {

		String pictueName = String.format("%d.png", System.currentTimeMillis());
		File fileDir = new File("mnt/sdcard/face/picture");
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}

		File mFile = new File(fileDir.getPath(), pictueName);
		if (null != mPhotoBitmap) {
			FileOutputStream mOutputStream = null;

			try {
				if (!mFile.exists()) {
					mFile.createNewFile();
				}

				mOutputStream = new FileOutputStream(mFile);
				mPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 90,
						mOutputStream);
				Toast.makeText(this, "您的照片保存在face文件夾下", Toast.LENGTH_SHORT)
						.show();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				if (null != mOutputStream) {
					try {
						mOutputStream.flush();
						mOutputStream.close();
					} catch (IOException e) {
					}
				}
			}

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == Constant.PICK_CODE) {

			Uri imageUri = data.getData();
			resetBitmap(imageUri);
			mImageView.setImageBitmap(mPhotoBitmap);
		} else if (resultCode == RESULT_OK
				&& requestCode == Constant.CAPTURE_CODE) {
			mPhotoBitmap = (Bitmap) data.getExtras().get("data");
			mImageView.setImageBitmap(mPhotoBitmap);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 解析图片Uri的路径
	 * 
	 * @param imageUri
	 * @return
	 */
	private String parseImagePath(Uri imageUri) {

		String[] mFilePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = this.getContentResolver().query(imageUri,
				mFilePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(mFilePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;

	}

	/**
	 * 调整Bitmap，预防图片太大出现oom
	 * 
	 * @param imageUri
	 */
	private void resetBitmap(Uri imageUri) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(parseImagePath(imageUri), options);
		float rate = Math.max(options.outWidth * 1.0f / 1024,
				options.outHeight * 1.0f / 1024);
		options.inSampleSize = (int) Math.ceil(rate);
		options.inJustDecodeBounds = false;
		mPhotoBitmap = BitmapFactory.decodeFile(parseImagePath(imageUri),
				options);

	}

}
