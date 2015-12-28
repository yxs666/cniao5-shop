/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare.theme.skyblue;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.EditPageFakeActivity;
import cn.sharesdk.onekeyshare.PicViewer;

import static cn.sharesdk.framework.utils.ShareSDKR.getIdRes;
import static cn.sharesdk.framework.utils.ShareSDKR.getLayoutRes;
import static cn.sharesdk.framework.utils.ShareSDKR.getStringRes;

/** 执行图文分享的页面，此页面不支持微信平台的分享 */
public class EditPage extends EditPageFakeActivity implements OnClickListener, TextWatcher {
	private static final int MAX_TEXT_COUNT = 140;

	// 字数计算器
	private TextView textCounterTextView;
	private EditText titleEditText;
	private EditText textEditText;

	public void onCreate() {
		if (shareParamMap == null || platforms == null) {
			finish();
			return;
		}

		activity.setContentView(getLayoutRes(activity, "ssdk_oks_skyblue_editpage"));
		initView();
	}

	private void initView() {
		if(!dialogMode) {
			RelativeLayout mainRelLayout = (RelativeLayout)findViewByResName("mainRelLayout");
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mainRelLayout.getLayoutParams();
			lp.setMargins(0,0,0,0);
			lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
			mainRelLayout.setLayoutParams(lp);
		}
		initTitleView();
		initBodyView();
		initImageListView();
	}

	private void initTitleView() {
		View backImageView = findViewByResName("backImageView");
		backImageView.setTag("close");
		backImageView.setOnClickListener(this);

		View okImageView = findViewByResName("okImageView");
		okImageView.setTag("ok");
		okImageView.setOnClickListener(this);
	}

	private void initBodyView() {
		View closeImageView = findViewByResName("closeImageView");
		closeImageView.setTag("close");
		closeImageView.setOnClickListener(this);

		if(shareParamMap.containsKey("title")) {
			titleEditText = (EditText) findViewByResName("titleEditText");
			titleEditText.setText(String.valueOf(shareParamMap.get("title")));
		}

		textCounterTextView = (TextView) findViewByResName("textCounterTextView");
		textCounterTextView.setText(String.valueOf(MAX_TEXT_COUNT));

		textEditText = (EditText) findViewByResName("textEditText");
		textEditText.addTextChangedListener(this);
		textEditText.setText(String.valueOf(shareParamMap.get("text")));

		initAtUserView();
	}

	private void initAtUserView() {
		LinearLayout atLayout = (LinearLayout) findViewByResName("atLayout");
		for(Platform platform : platforms) {
			String platformName = platform.getName();
			if (isShowAtUserLayout(platformName)) {
				View view = LayoutInflater.from(activity).inflate(getLayoutRes(activity, "ssdk_oks_skyblue_editpage_at_layout"), null);
				TextView atDescTextView = (TextView) view.findViewById(getIdRes(activity, "atDescTextView"));
				TextView atTextView = (TextView) view.findViewById(getIdRes(activity, "atTextView"));

				OnClickListener atBtnClickListener = new OnClickListener() {
					public void onClick(View v) {
						FollowListPage subPage = new FollowListPage();
						subPage.setPlatform((Platform) v.getTag());
						subPage.showForResult(activity, null, EditPage.this);
					}
				};
				atTextView.setTag(platform);
				atTextView.setOnClickListener(atBtnClickListener);
				atDescTextView.setTag(platform);
				atDescTextView.setOnClickListener(atBtnClickListener);

				atTextView.setText(getAtUserButtonText(platformName));
				atDescTextView.setText(getContext().getString(getStringRes(activity, "ssdk_oks_list_friends"), getLogoName(platformName)));

				atLayout.addView(view);
			}
		}

	}

	private void initImageListView() {
		final HorizontalScrollView hScrollView = (HorizontalScrollView) findViewByResName("hScrollView");
		ImageListResultsCallback callback = new ImageListResultsCallback() {

			@Override
			public void onFinish(ArrayList<ImageInfo> results) {
				if(results == null)
					return;
				LinearLayout layout = (LinearLayout) findViewByResName("imagesLinearLayout");
				for(ImageInfo imageInfo : results) {
					if(imageInfo.bitmap == null)
						continue;
					layout.addView(makeImageItemView(imageInfo));
				}
			}
		};
		if(!initImageList(callback)) {
			hScrollView.setVisibility(View.GONE);
		}

	}

	private View makeImageItemView(final ImageInfo imageInfo) {
		final View view = LayoutInflater.from(activity).inflate(getLayoutRes(activity, "ssdk_oks_skyblue_editpage_inc_image_layout"), null);

		ImageView imageView = (ImageView) view.findViewById(getIdRes(activity, "imageView"));
		imageView.setImageBitmap(imageInfo.bitmap);
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PicViewer pv = new PicViewer();
				pv.setImageBitmap(imageInfo.bitmap);
				pv.show(activity, null);
			}
		});

		View removeBtn = view.findViewById(getIdRes(activity, "imageRemoveBtn"));
		removeBtn.setTag(imageInfo);
		removeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				view.setVisibility(View.GONE);
				removeImage((ImageInfo) v.getTag());
			}
		});

		return view;
	}

	public void onClick(View v) {
		if(v.getTag() == null)
			return;
		String tag = (String) v.getTag();
		if (tag.equals("close")) {
			// 取消分享的统计
			for(Platform plat : platforms) {
				ShareSDK.logDemoEvent(5, plat);
			}
			finish();
			return;
		}

		if (tag.equals("ok")) {
			onShareButtonClick(v);
			return;
		}
	}

	private void onShareButtonClick(View v) {
		if(shareParamMap.containsKey("title")) {
			String title = titleEditText.getText().toString().trim();
			shareParamMap.put("title", title);
		}

		String text = textEditText.getText().toString().trim();
		shareParamMap.put("text", text);

		setResultAndFinish();
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		int remain = MAX_TEXT_COUNT - textEditText.length();
		textCounterTextView.setText(String.valueOf(remain));
		textCounterTextView.setTextColor(remain > 0 ? 0xffcfcfcf : 0xffff0000);
	}

	public void afterTextChanged(Editable s) {

	}

	public void onResult(HashMap<String, Object> data) {
		String atText = getJoinSelectedUser(data);
		if(atText != null) {
			textEditText.append(atText);
		}
	}

	public boolean onFinish() {
		textCounterTextView = null;
		textEditText = null;
		titleEditText = null;
		return super.onFinish();
	}

}
