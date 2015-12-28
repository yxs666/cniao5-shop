/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare.theme.skyblue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.TitleLayout;
import com.mob.tools.gui.AsyncImageView;
import com.mob.tools.gui.BitmapProcessor;
import com.mob.tools.gui.PullToRefreshListAdapter;
import com.mob.tools.gui.PullToRefreshView;
import com.mob.tools.utils.UIHandler;
import cn.sharesdk.onekeyshare.FollowerListFakeActivity;

import static com.mob.tools.utils.R.dipToPx;
import static cn.sharesdk.framework.utils.ShareSDKR.getBitmapRes;
import static cn.sharesdk.framework.utils.ShareSDKR.getStringRes;

/** 获取好友或关注列表 */
public class FollowListPage extends FollowerListFakeActivity implements OnClickListener, OnItemClickListener {
	private TitleLayout llTitle;
	private FollowAdapter adapter;
	private int lastPosition = -1;


	public void onCreate() {
		LinearLayout llPage = new LinearLayout(getContext());
		llPage.setBackgroundColor(0xfff5f5f5);
		llPage.setOrientation(LinearLayout.VERTICAL);
		activity.setContentView(llPage);

		// 标题栏
		llTitle = new TitleLayout(getContext());
		int resId = getBitmapRes(getContext(), "ssdk_oks_title_back");
		if (resId > 0) {
			llTitle.setBackgroundResource(resId);
		}
		llTitle.getBtnBack().setOnClickListener(this);
		resId = getStringRes(getContext(), "ssdk_oks_multi_share");
		if (resId > 0) {
			llTitle.getTvTitle().setText(resId);
		}
		llTitle.getBtnRight().setVisibility(View.VISIBLE);
		resId = getStringRes(getContext(), "ssdk_oks_finish");
		if (resId > 0) {
			llTitle.getBtnRight().setText(resId);
		}
		llTitle.getBtnRight().setOnClickListener(this);
		llTitle.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		llPage.addView(llTitle);

		FrameLayout flPage = new FrameLayout(getContext());
		LinearLayout.LayoutParams lpFl = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lpFl.weight = 1;
		flPage.setLayoutParams(lpFl);
		llPage.addView(flPage);

		// 关注（或朋友）列表
		PullToRefreshView followList = new PullToRefreshView(getContext());
		FrameLayout.LayoutParams lpLv = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		followList.setLayoutParams(lpLv);
		flPage.addView(followList);
		adapter = new FollowAdapter(followList);
		adapter.setPlatform(platform);
		followList.setAdapter(adapter);
		adapter.getListView().setOnItemClickListener(this);

		ImageView ivShadow = new ImageView(getContext());
		resId = getBitmapRes(getContext(), "ssdk_oks_title_shadow");
		if (resId > 0) {
			ivShadow.setBackgroundResource(resId);
		}
		FrameLayout.LayoutParams lpSd = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ivShadow.setLayoutParams(lpSd);
		flPage.addView(ivShadow);

		// 请求数据
		followList.performPulling(true);
	}

	public void onClick(View v) {
		if (v.equals(llTitle.getBtnRight())) {
			ArrayList<String> selected = new ArrayList<String>();
			for (int i = 0, size = adapter.getCount(); i < size; i++) {
				if (adapter.getItem(i).checked) {
					selected.add(adapter.getItem(i).atName);
				}
			}

			setResultForChecked(selected);
		}

		finish();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String name = platform.getName();
		if (isRadioMode(name)) {
			if(lastPosition >= 0) {
				Following lastFollwing = adapter.getItem(lastPosition);
				lastFollwing.checked = false;
			}
			lastPosition  = position;
		}
		Following following = adapter.getItem(position);
		following.checked = !following.checked;
		adapter.notifyDataSetChanged();
	}

	private static class FollowAdapter extends PullToRefreshListAdapter
			implements PlatformActionListener, Callback {
		private static final int FOLLOW_LIST_EMPTY = 2;
		private int curPage;
		private ArrayList<Following> follows;
		private HashMap<String, Boolean> map;
		private boolean hasNext;
		private Platform platform;
		private PRTHeader llHeader;
		private Bitmap bmChd;
		private Bitmap bmUnch;

		public FollowAdapter(PullToRefreshView view) {
			super(view);
			curPage = -1;
			hasNext = true;
			map = new HashMap<String, Boolean>();
			follows = new ArrayList<Following>();

			llHeader = new PRTHeader(getContext());

			int resId = getBitmapRes(getContext(), "ssdk_oks_auth_follow_cb_chd");
			if (resId > 0) {
				bmChd = BitmapFactory.decodeResource(view.getResources(), resId);
			}
			resId = getBitmapRes(getContext(), "ssdk_oks_auth_follow_cb_unc");
			if (resId > 0) {
				bmUnch = BitmapFactory.decodeResource(view.getResources(), resId);
			}
		}

		public void setPlatform(Platform platform) {
			this.platform = platform;
			platform.setPlatformActionListener(this);
		}

		private void next() {
			if (hasNext) {
				platform.listFriend(15, curPage + 1, null);
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			FollowListItem item = null;
			boolean simpleMode = "FacebookMessenger".equals(platform.getName());
			if (convertView == null) {
				LinearLayout llItem = new LinearLayout(parent.getContext());
				item = new FollowListItem();
				llItem.setTag(item);
				convertView = llItem;

				int dp_52 = com.mob.tools.utils.R.dipToPx(getContext(), 52);
				int dp_10 = com.mob.tools.utils.R.dipToPx(parent.getContext(), 10);
				int dp_5 = com.mob.tools.utils.R.dipToPx(parent.getContext(), 5);

				if(!simpleMode) {
					item.aivIcon = new AsyncImageView(getContext());
					LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(dp_52, dp_52);
					lpIcon.gravity = Gravity.CENTER_VERTICAL;
					lpIcon.setMargins(dp_10, dp_5, dp_10, dp_5);
					item.aivIcon.setLayoutParams(lpIcon);
					llItem.addView(item.aivIcon);
				}

				LinearLayout llText = new LinearLayout(parent.getContext());
				llText.setPadding(0, dp_10, dp_10, dp_10);
				llText.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lpText.gravity = Gravity.CENTER_VERTICAL;
				lpText.weight = 1;
				llText.setLayoutParams(lpText);
				llItem.addView(llText);

				item.tvName = new TextView(parent.getContext());
				item.tvName.setTextColor(0xff000000);
				item.tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
				item.tvName.setSingleLine();
				if(simpleMode) {
					item.tvName.setPadding(dp_10, 0, 0, 0);
				}
				llText.addView(item.tvName);

				if(!simpleMode) {
					item.tvSign = new TextView(parent.getContext());
					item.tvSign.setTextColor(0x7f000000);
					item.tvSign.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					item.tvSign.setSingleLine();
					llText.addView(item.tvSign);
				}

				item.ivCheck = new ImageView(parent.getContext());
				item.ivCheck.setPadding(0, 0, dp_10, 0);
				LinearLayout.LayoutParams lpCheck = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lpCheck.gravity = Gravity.CENTER_VERTICAL;
				item.ivCheck.setLayoutParams(lpCheck);
				llItem.addView(item.ivCheck);
			} else {
				item = (FollowListItem) convertView.getTag();
			}

			Following following = getItem(position);
			item.tvName.setText(following.screenName);
			if(!simpleMode) {
				item.tvSign.setText(following.description);
			}
			item.ivCheck.setImageBitmap(following.checked ? bmChd : bmUnch);
			if(!simpleMode) {
				if (isFling()) {
					Bitmap bm = BitmapProcessor.getBitmapFromCache(following.icon);
					if (bm != null && !bm.isRecycled()) {
						item.aivIcon.setImageBitmap(bm);
					} else {
						item.aivIcon.execute(null, 0);
					}
				} else {
					item.aivIcon.execute(following.icon,0);
				}
			}

			if (position == getCount() - 1) {
				next();
			}
			return convertView;
		}

		public Following getItem(int position) {
			return follows.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return follows == null ? 0 : follows.size();
		}

		public View getHeaderView() {
			return llHeader;
		}

		public void onPullDown(int percent) {
			llHeader.onPullDown(percent);
		}

		public void onRequest() {
			llHeader.onRequest();
			curPage = -1;
			hasNext = true;
			map.clear();
			next();
		}

		public void onCancel(Platform plat, int action) {
			UIHandler.sendEmptyMessage(-1, this);
		}

		public void onComplete(Platform plat, int action, HashMap<String, Object> res) {
			FollowersResult followersResult = parseFollowers(platform.getName(), res, map);

			if(followersResult == null) {
				UIHandler.sendEmptyMessage(FOLLOW_LIST_EMPTY, this);
				return;
			}
			hasNext = followersResult.hasNextPage;
			if (followersResult.list != null && followersResult.list.size() > 0) {
				curPage++;
				Message msg = new Message();
				msg.what = 1;
				msg.obj = followersResult.list;
				UIHandler.sendMessage(msg, this);
			}
		}

		public void onError(Platform plat, int action, Throwable t) {
			t.printStackTrace();
		}

		public boolean handleMessage(Message msg) {
			if (msg.what < 0) {
				((Activity) getContext()).finish();
			} else if(msg.what == FOLLOW_LIST_EMPTY) {
				notifyDataSetChanged();
			} else {
				if (curPage <= 0) {
					follows.clear();
				}
				@SuppressWarnings("unchecked")
				ArrayList<Following> data = (ArrayList<Following>) msg.obj;
				follows.addAll(data);
				notifyDataSetChanged();
			}
			return false;
		}

		public void onReversed() {
			super.onReversed();
			llHeader.reverse();
		}

	}

	private static class FollowListItem {
		public AsyncImageView aivIcon;
		public TextView tvName;
		public TextView tvSign;
		public ImageView ivCheck;
	}

	private static class PRTHeader extends LinearLayout {
		private TextView tvHeader;
		private RotateImageView ivArrow;
		private ProgressBar pbRefreshing;

		public PRTHeader(Context context) {
			super(context);
			setOrientation(VERTICAL);

			LinearLayout llInner = new LinearLayout(context);
			LinearLayout.LayoutParams lpInner = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpInner.gravity = Gravity.CENTER_HORIZONTAL;
			addView(llInner, lpInner);

			ivArrow = new RotateImageView(context);
			int resId = getBitmapRes(context, "ssdk_oks_ptr_ptr");
			if (resId > 0) {
				ivArrow.setImageResource(resId);
			}
			int dp_32 = dipToPx(context, 32);
			LayoutParams lpIv = new LayoutParams(dp_32, dp_32);
			lpIv.gravity = Gravity.CENTER_VERTICAL;
			llInner.addView(ivArrow, lpIv);

			pbRefreshing = new ProgressBar(context);
			llInner.addView(pbRefreshing, lpIv);
			pbRefreshing.setVisibility(View.GONE);

			tvHeader = new TextView(getContext());
			tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			tvHeader.setGravity(Gravity.CENTER);
			int dp_10 = com.mob.tools.utils.R.dipToPx(getContext(), 10);
			tvHeader.setPadding(dp_10, dp_10, dp_10, dp_10);
			tvHeader.setTextColor(0xff000000);
			LayoutParams lpTv = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpTv.gravity = Gravity.CENTER_VERTICAL;
			llInner.addView(tvHeader, lpTv);
		}

		public void onPullDown(int percent) {
			if (percent > 100) {
				int degree = (percent - 100) * 180 / 20;
				if (degree > 180) {
					degree = 180;
				}
				if (degree < 0) {
					degree = 0;
				}
				ivArrow.setRotation(degree);
			} else {
				ivArrow.setRotation(0);
			}

			if (percent < 100) {
				int resId = getStringRes(getContext(), "ssdk_oks_pull_to_refresh");
				if (resId > 0) {
					tvHeader.setText(resId);
				}
			} else {
				int resId = getStringRes(getContext(), "ssdk_oks_release_to_refresh");
				if (resId > 0) {
					tvHeader.setText(resId);
				}
			}
		}

		public void onRequest() {
			ivArrow.setVisibility(View.GONE);
			pbRefreshing.setVisibility(View.VISIBLE);
			int resId = getStringRes(getContext(), "ssdk_oks_refreshing");
			if (resId > 0) {
				tvHeader.setText(resId);
			}
		}

		public void reverse() {
			pbRefreshing.setVisibility(View.GONE);
			ivArrow.setRotation(180);
			ivArrow.setVisibility(View.VISIBLE);
		}

	}

	private static class RotateImageView extends ImageView {
		private int rotation;

		public RotateImageView(Context context) {
			super(context);
		}

		public void setRotation(int degree) {
			rotation = degree;
			invalidate();
		}

		protected void onDraw(Canvas canvas) {
			canvas.rotate(rotation, getWidth() / 2, getHeight() / 2);
			super.onDraw(canvas);
		}

	}

}
