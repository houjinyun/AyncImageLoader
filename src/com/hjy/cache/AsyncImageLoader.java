package com.hjy.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
/**
 * 异步加载图片资源
 * 
 * @author Jinyun.Hou
 *
 */
public class AsyncImageLoader{
	
	/** 内存缓存 */
	private MemoryCache mMemoryCache;
	/** 文件缓存 */
	private FileCache mFileCache;
	
	/** 线程池 */
	private ExecutorService mExecutorService;
	
	private Map<ImageView, String> mImageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	
	/** 保存正在加载图片的url */
	private List<LoadPhotoTask> mTaskQueue = new ArrayList<LoadPhotoTask>();
	
	/**
	 * 默认采用一个大小为5的线程池
	 * 
	 * @param context
	 * @param memoryCache 所采用的高速缓存
	 * @param fileCache 所采用的文件缓存
	 */
	public AsyncImageLoader(Context context, MemoryCache memoryCache, FileCache fileCache) {
		mMemoryCache = memoryCache;
		mFileCache = fileCache;
		mExecutorService = Executors.newFixedThreadPool(5);
	}

	/**
	 * 根据url加载相应的图片
	 * 
	 * @param url
	 * @param flag
	 * @return 如果缓存里有则直接返回，如果没有则异步从文件或网络端获取
	 */
	public Bitmap loadBitmap(ImageView imageView, String url, boolean flag) {
		mImageViews.put(imageView, url);
		Bitmap bitmap = mMemoryCache.get(url);
		if(bitmap == null) {
			enquequeLoadPhoto(url, imageView);
		}
		return bitmap;
	}
	
	/**
	 * 加入图片下载队列
	 * 
	 * @param url
	 */
	private void enquequeLoadPhoto(String url, ImageView imageView) {
		//如果任务已经存在，则不重新添加
		if(isTaskExisted(url))
			return;
		LoadPhotoTask task = new LoadPhotoTask(url, imageView);
		synchronized (mTaskQueue) {
			mTaskQueue.add(task);			
		}
		mExecutorService.submit(task);
	}
	
	/**
	 * 判断下载队列中是否已经存在该任务
	 * 
	 * @param url
	 * @return
	 */
	private boolean isTaskExisted(String url) {
		if(url == null)
			return false;
		synchronized (mTaskQueue) {
			int size = mTaskQueue.size();
			for(int i=0; i<size; i++) {
				LoadPhotoTask task = mTaskQueue.get(i);
				if(task != null && task.getUrl().equals(url))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * 从缓存文件或者网络端获取图片
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getBitmapByUrl(String url) {
		File f = mFileCache.getFile(url);
		Bitmap b = ImageUtil.decodeFile(f);
		if (b != null)
			return b;
		return ImageUtil.loadBitmapFromWeb(url, f);
	}
	
	/**
	 * 判断该ImageView是否已经被复用
	 * 
	 * @param imageView
	 * @param url
	 * @return
	 */
	private boolean imageViewReused(ImageView imageView, String url) {
		String tag = mImageViews.get(imageView);
		if (tag == null || !tag.equals(url))
			return true;
		return false;
	}
	
	private void removeTask(LoadPhotoTask task) {
		synchronized (mTaskQueue) {
			mTaskQueue.remove(task);
		}
	}
	
	class LoadPhotoTask implements Runnable {
		private String url;
		private ImageView imageView;
		
		LoadPhotoTask(String url, ImageView imageView) {
			this.url = url;
			this.imageView = imageView;
		}

		@Override
		public void run() {
			if (imageViewReused(imageView, url)) {
				removeTask(this);
				return;
			}
			Bitmap bmp = getBitmapByUrl(url);
			mMemoryCache.put(url, bmp);
			if (imageViewReused(imageView, url)) {
				removeTask(this);
				return;
			}
			removeTask(this);
			BitmapDisplayer bd = new BitmapDisplayer(bmp, imageView, url);
			Activity a = (Activity) imageView.getContext();
			a.runOnUiThread(bd);
		}
		
		public String getUrl() {
			return url;
		}
	}
	
	/**
	 * Used to display bitmap in the UI thread
	 *
	 */
	class BitmapDisplayer implements Runnable {
		private Bitmap bitmap;
		private ImageView imageView;
		private String url;
		
		public BitmapDisplayer(Bitmap b, ImageView imageView, String url) {
			bitmap = b;
			this.imageView = imageView;
			this.url = url;
		}

		public void run() {
			if (imageViewReused(imageView, url))
				return;
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);
		}
	}
	
	/**
	 * 释放资源
	 */
	public void destroy() {
		mMemoryCache.clear();
		mMemoryCache = null;
		mImageViews.clear();
		mImageViews = null;
		mTaskQueue.clear();
		mTaskQueue = null;
		mExecutorService.shutdown();
		mExecutorService = null;
	}
}