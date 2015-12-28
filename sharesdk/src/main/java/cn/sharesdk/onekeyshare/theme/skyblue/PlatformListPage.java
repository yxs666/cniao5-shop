/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare.theme.skyblue;

import android.os.AsyncTask;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.PlatformListFakeActivity;

import static cn.sharesdk.framework.utils.ShareSDKR.getLayoutRes;
import static cn.sharesdk.framework.utils.ShareSDKR.getStringRes;

public class PlatformListPage extends PlatformListFakeActivity implements View.OnClickListener {
	private PlatformGridViewAdapter gridViewAdapter;

	public void onCreate() {
		super.onCreate();
		activity.setContentView(getLayoutRes(activity, "ssdk_oks_skyblue_share_platform_list"));

		initView();
	}

	private void initView() {
		View backImageView = findViewByResName("backImageView");
		backImageView.setTag(android.R.string.cancel);
		backImageView.setOnClickListener(this);

		View okImageView = findViewByResName("okImageView");
		okImageView.setTag(android.R.string.ok);
		okImageView.setOnClickListener(this);

		gridViewAdapter = new PlatformGridViewAdapter(activity);
		gridViewAdapter.setCustomerLogos(customerLogos);

		GridView gridView = (GridView) findViewByResName("gridView");
		gridView.setAdapter(gridViewAdapter);

		new AsyncTask<Void, Void, Platform[]>() {

			@Override
			protected Platform[] doInBackground(Void... params) {
				return ShareSDK.getPlatformList();
			}

			@Override
			protected void onPostExecute(Platform[] platforms) {
				gridViewAdapter.setData(platforms, hiddenPlatforms);
			}
		}.execute();
	}

	public void onClick(View v) {
		Object tag = v.getTag();
		if(tag == null || !(tag instanceof Integer))
			return;

		switch ((Integer)tag) {
			case android.R.string.cancel:
				setCanceled(true);
				finish();
				break;
			case android.R.string.ok:
				onShareButtonClick(v);
				break;
		}
	}

	private void onShareButtonClick(View v) {
		if(gridViewAdapter == null || "locked".equals(v.getTag()))
			return;

		List<Object> checkedPlatforms = gridViewAdapter.getCheckedItems();
		if(checkedPlatforms.size() == 0){
			Toast.makeText(activity, getStringRes(activity, "ssdk_oks_select_one_plat_at_least"), Toast.LENGTH_SHORT).show();
			return;
		}

		v.setTag("locked");
		onShareButtonClick(v, checkedPlatforms);
	}

}
