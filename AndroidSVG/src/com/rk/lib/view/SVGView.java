/**
 * SVGView.java
 * 
 * A view used to render the SVG File into image view.
 *
 * @category   View
 * @package    com.android.lib.view
 * @version    1.0
 * @author     Rajkumar.N <rkmail1989@gmail.com>
 * @license    http://www.apache.org/licenses/LICENSE-2.0 
 */
package com.rk.lib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.rk.lib.view.R;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class SVGView extends ImageView {

	private static LruCache<Integer, Drawable> memoryCache;
	private Context mContext;
	private int resourceId;
	private boolean isSvgResource;

	public SVGView(Context context) {
		super(context);
		mContext = context;
	}

	public SVGView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public SVGView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Loads the initial resource for the View.
	 * 
	 * @param context
	 *            - The Context associated with the view.
	 * @param attrs
	 *            - The AttributeSet associated with the view.
	 * @param defStyle
	 *            - The style associated with the view.
	 */
	public void init(Context context, AttributeSet attrs, int defStyle) {
		if (!isInEditMode()) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			mContext = context;
			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			final int cacheSize = maxMemory / 8;
			memoryCache = new LruCache<Integer, Drawable>(cacheSize);
			TypedArray typedArray = getContext().getTheme()
					.obtainStyledAttributes(attrs, R.styleable.SVGView,
							defStyle, 0);
			resourceId = typedArray.getResourceId(R.styleable.SVGView_source,
					-1);
			isSvgResource = typedArray.getBoolean(R.styleable.SVGView_isSvg,
					false);
			if (resourceId != -1) {
				if (isSvgResource) {
					setCacheImageDrawable(resourceId, true);
				} else {
					setCacheImageDrawable(resourceId, false);
				}
			}
		}
	}

	/**
	 * Caches the previous reference resources or create the new reference if
	 * the previous reference not availabe and Sets a drawable as the content of
	 * this ImageView.
	 * 
	 * @param resourceId
	 *            - Integer value that refers the SVG resource
	 * @param isSvgResource
	 *            - Boolean value that refers the resource is drawable or svg.
	 *            If the resource is SVG then it will be true, otherwise false.
	 */
	public void setCacheImageDrawable(int resourceId, boolean isSvgResource) {
		Drawable drawable = memoryCache.get(resourceId);
		if (drawable != null) {
			this.setImageDrawable(drawable);
		} else {
			if (isSvgResource) {
				drawable = new PictureDrawable(getPicture(resourceId));
			} else {
				drawable = getResources().getDrawable(resourceId);
			}

			if (drawable != null) {
				this.setImageDrawable(drawable);
				memoryCache.put(resourceId, drawable);
			}
		}
	}

	/**
	 * Caches the previous reference resources or create the new reference if
	 * the previous reference not available and Sets a background drawable
	 * associated with this ImageView
	 * 
	 * @param resourceId
	 *            - Integer value that refers the SVG resource
	 * @param isSvgResource
	 *            - Boolean value that refers the resource is drawable or svg.
	 *            If the resource is SVG then it will be true, otherwise false.
	 */

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void setCacheBackgroundDrawable(int resourceId, boolean isSvgResource) {
		Drawable drawable = memoryCache.get(resourceId);
		if (drawable != null) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				this.setBackgroundDrawable(drawable);
			} else {
				this.setBackground(drawable);
			}
		} else {
			if (isSvgResource) {
				drawable = new PictureDrawable(getPicture(resourceId));
			} else {
				drawable = getResources().getDrawable(resourceId);
			}

			if (drawable != null) {
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					this.setBackgroundDrawable(drawable);
				} else {
					this.setBackground(drawable);
				}
				memoryCache.put(resourceId, drawable);
			}
		}
	}

	/**
	 * Gets the Picture parsed from the SVG Parser
	 * 
	 * @param resourceId
	 *            - The resourceId need to be parsed
	 * @return Picture - Picture which is parsed by the SVG Parser
	 */
	public Picture getPicture(int resourceId) {
		try {
			return SVG.getFromResource(mContext, resourceId).renderToPicture();
		} catch (SVGParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	/**
	 * Clears LruCahe and Recycle the bitmap associated to this view
	 */
	public void clearMemoryGarbage() {
		memoryCache.evictAll();
		Bitmap bitmapToRecycle = ((BitmapDrawable) this.getDrawable())
				.getBitmap();
		if (bitmapToRecycle != null && !bitmapToRecycle.isRecycled()) {
			bitmapToRecycle.recycle();
		}
		System.gc();
	}
}
