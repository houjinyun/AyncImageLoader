package com.hjy.cache;

import java.io.File;

import android.content.Context;

/**
 * 管理文件缓存和内存缓存
 * 
 * @author Jinyun.Hou
 *
 */
public class ImageCacheManager {
	/** 图片缓存的一级目录，所有图片都在该目录的子目录下 */
	public static final String CACHE_DIR = "iOrange";
	
	/** 海报文件缓存目录 */
	public static final String DIR_PLACARD = "placard";
	/** 商品列表图片文件缓存目录 */
	public static final String DIR_PRODUCT = "product";
	
	/** 高速缓存 */
	private MemoryCache mMemoryCache;
	
	/** 海报图片文件缓存 */
	private FileCache mFileCachePlacard;
	
	/** 商品列表图片文件缓存 */
	private FileCache mFileCacheProduct;
	
	public ImageCacheManager(Context context) {
		mMemoryCache = new MemoryCache();
		File sdCard = android.os.Environment.getExternalStorageDirectory();
		File cacheDir = new File(sdCard, CACHE_DIR);
		mFileCachePlacard = new FileCache(context, cacheDir, DIR_PLACARD);
		mFileCacheProduct = new FileCache(context, cacheDir, DIR_PRODUCT);
	}
	
	/**
	 * 获取高速缓存
	 * 
	 * @return
	 */
	public MemoryCache getMemoryCache() {
		return mMemoryCache;
	}
	
	/**
	 * 获取海报文件缓存
	 * 
	 * @return
	 */
	public FileCache getPlacardFileCache() {
		return mFileCachePlacard;
	}
	
	/**
	 * 获取商品列表图片文件缓存
	 * 
	 * @return
	 */
	public FileCache getProductFileCache() {
		return mFileCacheProduct;
	}
	
	public void clearMemoryCache() {
		mMemoryCache.clear();
	}
	
	/**
	 * 清除所有的文件缓存
	 */
	public void clearAllFileCache() {
		mFileCachePlacard.clear();
		mFileCacheProduct.clear();
	}
	
	/**
	 * 清除文件缓存
	 * 
	 * @param fileCacheDir
	 */
	public void clearFileCache(String fileCacheDir) {
		if(DIR_PLACARD.equals(fileCacheDir)) {
			mFileCachePlacard.clear();
		} else if(DIR_PRODUCT.equals(fileCacheDir)) {
			mFileCacheProduct.clear();
		}
	}
}