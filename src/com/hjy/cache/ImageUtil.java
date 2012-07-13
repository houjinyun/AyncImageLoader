package com.hjy.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 图片工具类
 * 
 * @author Jinyun.Hou
 *
 */
public class ImageUtil {

	/**
	 * 从网络获取图片，并缓存在指定的文件中
	 * 
	 * @param url 图片url
	 * @param file 缓存文件
	 * @return
	 */
	public static Bitmap loadBitmapFromWeb(String url, File file) {
		HttpURLConnection conn = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			is = conn.getInputStream();
			os = new FileOutputStream(file);
			copyStream(is, os);
			
			bitmap = decodeFile(file);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if(os != null)
					os.close();
				if(is != null)
					is.close();
				if(conn != null)
					conn.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Decodes image and scales it to reduce memory consumption
	 * 
	 * @param f
	 * @return
	 */
	public static Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, options);
			int inSampleSize = computeSampleSize(options, -1, 128 * 128);
			// decode with inSampleSize
			options.inSampleSize = inSampleSize;
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, options);
		} catch (Exception e) {
		} 
		return null;
	}
	
	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 根据实际需要，计算出合适的inSampleSize，以减少内存的开销
	 * 
	 * @param options
	 * @param minSideLength 最小长度，不限制则为-1
	 * @param maxNumOfPixels 最大像素点，128 * 128
	 * @return
	 */
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}