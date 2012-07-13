package com.hjy.lazylist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fedorvlasov.lazylist.R;
import com.hjy.cache.AsyncImageLoader;
import com.hjy.cache.ImageCacheManager;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private String[] data;
    private static LayoutInflater inflater=null;
   
    private AsyncImageLoader imageLoader;

    
    public LazyAdapter(Activity a, String[] d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageCacheManager cacheMgr = new ImageCacheManager(a);
        imageLoader = new AsyncImageLoader(a, cacheMgr.getMemoryCache(), cacheMgr.getPlacardFileCache());
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.item, null);
        Log.i("AsyncImageLoader", "position = " + position);

        TextView text=(TextView)vi.findViewById(R.id.text);;
        ImageView image=(ImageView)vi.findViewById(R.id.image);
        text.setText("item "+position);
        image.setTag(data[position]);
        Bitmap bmp = imageLoader.loadBitmap(image, data[position], true);
        if(bmp == null) {
        	image.setImageResource(R.drawable.stub);
        } else {
        	image.setImageBitmap(bmp);
        }
        return vi;
    }
    
    public void destroy() {
    	imageLoader.destroy();
    }
}