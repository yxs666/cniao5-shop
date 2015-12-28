/*
 * Offical Website:http://www.mob.com
 * Support QQ: 4006852216
 * Offical Wechat Account:ShareSDK   (We will inform you our updated news at the first time by Wechat, if we release a new version. If you get any problem, you can also contact us with Wechat, we will reply you within 24 hours.)
 *
 * Copyright (c) 2013 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare.theme.classic;

import static cn.sharesdk.framework.utils.ShareSDKR.getBitmapRes;
import static cn.sharesdk.framework.utils.ShareSDKR.getStringRes;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import com.mob.tools.gui.ViewPagerAdapter;
import com.mob.tools.gui.ViewPagerClassic;
import com.mob.tools.utils.UIHandler;
import cn.sharesdk.onekeyshare.CustomerLogo;

/** platform logo list gridview */
@SuppressWarnings("deprecation")
public class PlatformGridView extends LinearLayout implements
		OnClickListener, Callback {
	private static final int MIN_CLICK_INTERVAL = 1000;
	private static final int MSG_PLATFORM_LIST_GOT = 1;
	// grids in each line
	private int LINE_PER_PAGE;
	// lines in each page
	private int COLUMN_PER_LINE;
	// grids in each page
	private int PAGE_SIZE;
	// grids container
	private ViewPagerClassic pager;
	// indicators
	private ImageView[] points;
	private Bitmap grayPoint;
	private Bitmap bluePoint;
	// Determine whether don't jump editpage and share directly
	private boolean silent;
	// platforms
	private Platform[] platformList;
	// data to share
	private HashMap<String, Object> reqData;
	private PlatformListPage parent;
	private ArrayList<CustomerLogo> customers;
	private HashMap<String, String> hiddenPlatforms;
	private View bgView;
	private long lastClickTime;

	public PlatformGridView(Context context) {
		super(context);
		init(context);
	}

	public PlatformGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(final Context context) {
		calPageSize();
		setOrientation(VERTICAL);

		pager = new ViewPagerClassic(context);
		disableOverScrollMode(pager);
		pager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addView(pager);

		// in order to have a better UI effect, opening a thread request the list of platforms
		new Thread() {
			public void run() {
				try {
					platformList = ShareSDK.getPlatformList();
					if (platformList == null) {
						platformList = new Platform[0];
					}
					UIHandler.sendEmptyMessage(MSG_PLATFORM_LIST_GOT, PlatformGridView.this);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}.start();
	}

	private void calPageSize() {
		float scrW = com.mob.tools.utils.R.getScreenWidth(getContext());
		float scrH = com.mob.tools.utils.R.getScreenHeight(getContext());
		float whR = scrW / scrH;
		if (whR < 0.63) {
			COLUMN_PER_LINE = 3;
			LINE_PER_PAGE = 3;
		} else if (whR < 0.75) {
			COLUMN_PER_LINE = 3;
			LINE_PER_PAGE = 2;
		} else {
			LINE_PER_PAGE = 1;
			if (whR >= 1.75) {
				COLUMN_PER_LINE = 6;
			} else if (whR >= 1.5) {
				COLUMN_PER_LINE = 5;
			} else if (whR >= 1.3) {
				COLUMN_PER_LINE = 4;
			} else {
				COLUMN_PER_LINE = 3;
			}
		}
		PAGE_SIZE = COLUMN_PER_LINE * LINE_PER_PAGE;
	}

	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_PLATFORM_LIST_GOT: {
				afterPlatformListGot();
			}
			break;
		}
		return false;
	}

	// initializes the girdview of platforms
	public void afterPlatformListGot() {
		PlatformAdapter adapter = new PlatformAdapter(this);
		pager.setAdapter(adapter);
		int pageCount = 0;
		if (platformList != null) {
			int cusSize = customers == null ? 0 : customers.size();
			int platSize = platformList == null ? 0 : platformList.length;
			int hideSize = hiddenPlatforms == null ? 0 : hiddenPlatforms.size();
			platSize = platSize-hideSize;
			int size = platSize + cusSize;
			pageCount = size / PAGE_SIZE;
			if (size % PAGE_SIZE > 0) {
				pageCount++;
			}
		}
		points = new ImageView[pageCount];
		if (points.length <= 0) {
			return;
		}

		Context context = getContext();
		LinearLayout llPoints = new LinearLayout(context);
		// if the total number of pages exceeds 1, we set the page indicators
		llPoints.setVisibility(pageCount > 1 ? View.VISIBLE: View.GONE);
		LayoutParams lpLl = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpLl.gravity = Gravity.CENTER_HORIZONTAL;
		llPoints.setLayoutParams(lpLl);
		addView(llPoints);

		int dp_5 = com.mob.tools.utils.R.dipToPx(context, 5);
		int resId = getBitmapRes(getContext(), "ssdk_oks_light_blue_point");
		if (resId > 0) {
			grayPoint = BitmapFactory.decodeResource(getResources(), resId);
		}
		resId = getBitmapRes(getContext(), "ssdk_oks_blue_point");
		if (resId > 0) {
			bluePoint = BitmapFactory.decodeResource(getResources(), resId);
		}
		for (int i = 0; i < pageCount; i++) {
			points[i] = new ImageView(context);
			points[i].setScaleType(ScaleType.CENTER_INSIDE);
			points[i].setImageBitmap(grayPoint);
			LayoutParams lpIv = new LayoutParams(dp_5, dp_5);
			lpIv.setMargins(dp_5, dp_5, dp_5, 0);
			points[i].setLayoutParams(lpIv);
			llPoints.addView(points[i]);
		}
		int curPage = pager.getCurrentScreen();
		points[curPage].setImageBitmap(bluePoint);
	}

	/** after the screen rotates, this method will be called to refresh the list of gridviews */
	public void onConfigurationChanged() {
		int curFirst = pager.getCurrentScreen() * PAGE_SIZE;
		calPageSize();
		int newPage = curFirst / PAGE_SIZE;

		removeViewAt(1);
		afterPlatformListGot();

		pager.setCurrentScreen(newPage);
	}

	public void setData(HashMap<String, Object> data, boolean silent) {
		reqData = data;
		this.silent = silent;
	}

	public void setHiddenPlatforms(HashMap<String, String> hiddenPlatforms) {
		this.hiddenPlatforms = hiddenPlatforms;
	}

	/** Set the Click event of the custom icon */
	public void setCustomerLogos(ArrayList<CustomerLogo> customers) {
		this.customers = customers;
	}

	public void setEditPageBackground(View bgView) {
		this.bgView = bgView;
	}

	/** Sets the callback page sharing operations */
	public void setParent(PlatformListPage parent) {
		this.parent = parent;
	}

	public void onClick(View v) {
		long time = System.currentTimeMillis();
		if (time - lastClickTime < MIN_CLICK_INTERVAL) {
			return;
		}
		lastClickTime = time;

		ArrayList<Object> platforms = new ArrayList<Object>(1);
		platforms.add(v.getTag());
		parent.onPlatformIconClick(v, platforms);
	}

	// Disable the flashing effect when viewpages sliding to left/right edge
	private void disableOverScrollMode(View view) {
		if (Build.VERSION.SDK_INT < 9) {
			return;
		}
		try {
			Method m = View.class.getMethod("setOverScrollMode",
					new Class[] { Integer.TYPE });
			m.setAccessible(true);
			m.invoke(view, new Object[] { Integer.valueOf(2) });
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/** gridview adapter */
	private static class PlatformAdapter extends ViewPagerAdapter {
		private GridView[] girds;
		private List<Object> logos;
		private OnClickListener callback;
		private int lines;
		private PlatformGridView platformGridView;

		public PlatformAdapter(PlatformGridView platformGridView) {
			this.platformGridView = platformGridView;
			logos = new ArrayList<Object>();
			Platform[] platforms = platformGridView.platformList;
			HashMap<String, String> hiddenPlatforms = platformGridView.hiddenPlatforms;
			if (platforms != null) {
				if (hiddenPlatforms != null && hiddenPlatforms.size() > 0) {
					ArrayList<Platform> ps = new ArrayList<Platform>();
					for (Platform p : platforms) {
						if (hiddenPlatforms.containsKey(p.getName())) {
							continue;
						}
						ps.add(p);
					}

					platforms = new Platform[ps.size()];
					for (int i = 0; i < platforms.length; i++) {
						platforms[i] = ps.get(i);
					}
				}

				logos.addAll(Arrays.asList(platforms));
			}
			ArrayList<CustomerLogo> customers = platformGridView.customers;
			if (customers != null) {
				logos.addAll(customers);
			}
			this.callback = platformGridView;
			girds = null;

			if (logos != null) {
				int size = logos.size();
				int PAGE_SIZE = platformGridView.PAGE_SIZE;
				int pageCount = size / PAGE_SIZE;
				if (size % PAGE_SIZE > 0) {
					pageCount++;
				}
				girds = new GridView[pageCount];
			}
		}

		public int getCount() {
			return girds == null ? 0 : girds.length;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (girds[position] == null) {
				int pageSize = platformGridView.PAGE_SIZE;
				int curSize = pageSize * position;
				int listSize = logos == null ? 0 : logos.size();
				if (curSize + pageSize > listSize) {
					pageSize = listSize - curSize;
				}
				Object[] gridBean = new Object[pageSize];
				for (int i = 0; i < pageSize; i++) {
					gridBean[i] = logos.get(curSize + i);
				}

				if (position == 0) {
					int COLUMN_PER_LINE = platformGridView.COLUMN_PER_LINE;
					lines = gridBean.length / COLUMN_PER_LINE;
					if (gridBean.length % COLUMN_PER_LINE > 0) {
						lines++;
					}
				}
				girds[position] = new GridView(this);
				girds[position].setData(lines, gridBean);
			}

			return girds[position];
		}

		/** This method will be called after sliding the gridview */
		public void onScreenChange(int currentScreen, int lastScreen) {
			ImageView[] points = platformGridView.points;
			for (int i = 0; i < points.length; i++) {
				points[i].setImageBitmap(platformGridView.grayPoint);
			}

			points[currentScreen].setImageBitmap(platformGridView.bluePoint);
		}

	}

	/** a simple gridview */
	private static class GridView extends LinearLayout {
		private Object[] beans;
		private OnClickListener callback;
		private int lines;
		private PlatformAdapter platformAdapter;

		public GridView(PlatformAdapter platformAdapter) {
			super(platformAdapter.platformGridView.getContext());
			this.platformAdapter = platformAdapter;
			this.callback = platformAdapter.callback;
		}

		public void setData(int lines, Object[] beans) {
			this.lines = lines;
			this.beans = beans;
			init();
		}

		private void init() {
			int dp_5 = com.mob.tools.utils.R.dipToPx(getContext(), 5);
			setPadding(0, dp_5, 0, dp_5);
			setOrientation(VERTICAL);

			int size = beans == null ? 0 : beans.length;
			int COLUMN_PER_LINE = platformAdapter.platformGridView.COLUMN_PER_LINE;
			int lineSize = size / COLUMN_PER_LINE;
			if (size % COLUMN_PER_LINE > 0) {
				lineSize++;
			}
			LayoutParams lp = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			lp.weight = 1;
			for (int i = 0; i < lines; i++) {
				LinearLayout llLine = new LinearLayout(getContext());
				llLine.setLayoutParams(lp);
				llLine.setPadding(dp_5, 0, dp_5, 0);
				addView(llLine);

				if (i >= lineSize) {
					continue;
				}

				for (int j = 0; j < COLUMN_PER_LINE; j++) {
					final int index = i * COLUMN_PER_LINE + j;
					if (index >= size) {
						LinearLayout llItem = new LinearLayout(getContext());
						llItem.setLayoutParams(lp);
						llLine.addView(llItem);
						continue;
					}

					final LinearLayout llItem = getView(index, callback, getContext());
					llItem.setTag(beans[index]);
					llItem.setLayoutParams(lp);
					llLine.addView(llItem);
				}
			}
		}

		private LinearLayout getView(int position, OnClickListener ocL, Context context) {
			Bitmap logo;
			String label;
			OnClickListener listener;
			if (beans[position] instanceof Platform) {
				logo = getIcon((Platform) beans[position]);
				label = getName((Platform) beans[position]);
				listener = ocL;
			} else {
				logo = ((CustomerLogo) beans[position]).enableLogo;
				label = ((CustomerLogo) beans[position]).label;
				listener = ocL;
			}

			LinearLayout ll = new LinearLayout(context);
			ll.setOrientation(LinearLayout.VERTICAL);

			ImageView iv = new ImageView(context);
			int dp_5 = com.mob.tools.utils.R.dipToPx(context, 5);
			iv.setPadding(dp_5, dp_5, dp_5, dp_5);
			iv.setScaleType(ScaleType.CENTER_INSIDE);
			LayoutParams lpIv = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpIv.setMargins(dp_5, dp_5, dp_5, dp_5);
			lpIv.gravity = Gravity.CENTER_HORIZONTAL;
			iv.setLayoutParams(lpIv);
			iv.setImageBitmap(logo);
			ll.addView(iv);

			TextView tv = new TextView(context);
			tv.setTextColor(0xff000000);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			tv.setSingleLine();
			tv.setIncludeFontPadding(false);
			LayoutParams lpTv = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpTv.gravity = Gravity.CENTER_HORIZONTAL;
			lpTv.weight = 1;
			lpTv.setMargins(dp_5, 0, dp_5, dp_5);
			tv.setLayoutParams(lpTv);
			tv.setText(label);
			ll.addView(tv);
			ll.setOnClickListener(listener);

			return ll;
		}

		private Bitmap getIcon(Platform plat) {
			if (plat == null) {
				return null;
			}

			String name = plat.getName();
			if (name == null) {
				return null;
			}

			String resName = "logo_" + plat.getName();
			int resId = getBitmapRes(getContext(), resName.toLowerCase());
			return BitmapFactory.decodeResource(getResources(), resId);
		}

		private String getName(Platform plat) {
			if (plat == null) {
				return "";
			}

			String name = plat.getName();
			if (name == null) {
				return "";
			}

			int resId = getStringRes(getContext(), plat.getName().toLowerCase());
			if (resId > 0) {
				return getContext().getString(resId);
			}
			return null;
		}

	}

}
