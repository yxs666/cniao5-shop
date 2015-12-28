/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare;

import static com.mob.tools.utils.BitmapHelper.captureView;
import static cn.sharesdk.framework.utils.ShareSDKR.getStringRes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import cn.sharesdk.framework.CustomPlatform;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import com.mob.tools.utils.UIHandler;

/**
 * 快捷分享的入口
 * <p>
 * 通过不同的setter设置参数，然后调用{@link #show(Context)}方法启动快捷分享
 */
public class OnekeyShare implements PlatformActionListener, Callback {
	private static final int MSG_TOAST = 1;
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;

	private HashMap<String, Object> shareParamsMap;
	private ArrayList<CustomerLogo> customers;
	private boolean silent;
	private PlatformActionListener callback;
	private ShareContentCustomizeCallback customizeCallback;
	private boolean dialogMode = false;
	private boolean disableSSO;
	private boolean shareVideo;
	private HashMap<String, String> hiddenPlatforms;
	private View bgView;
	private OnekeyShareTheme theme;

	private Context context;
	private PlatformListFakeActivity.OnShareButtonClickListener onShareButtonClickListener;

	public OnekeyShare() {
		shareParamsMap = new HashMap<String, Object>();
		customers = new ArrayList<CustomerLogo>();
		callback = this;
		hiddenPlatforms = new HashMap<String, String>();
	}

	public void show(Context context) {
		ShareSDK.initSDK(context);
		this.context = context;

		// 打开分享菜单的统计
		ShareSDK.logDemoEvent(1, null);

		// 显示方式是由platform和silent两个字段控制的
		// 如果platform设置了，则无须显示九宫格，否则都会显示；
		// 如果silent为true，表示不进入编辑页面，否则会进入。
		// 本类只判断platform，因为九宫格显示以后，事件交给PlatformGridView控制
		// 当platform和silent都为true，则直接进入分享；
		// 当platform设置了，但是silent为false，则判断是否是“使用客户端分享”的平台，
		// 若为“使用客户端分享”的平台，则直接分享，否则进入编辑页面
		if (shareParamsMap.containsKey("platform")) {
			String name = String.valueOf(shareParamsMap.get("platform"));
			Platform platform = ShareSDK.getPlatform(name);

			if (silent
					|| ShareCore.isUseClientToShare(name)
					|| platform instanceof CustomPlatform
					) {
				HashMap<Platform, HashMap<String, Object>> shareData
						= new HashMap<Platform, HashMap<String,Object>>();
				shareData.put(ShareSDK.getPlatform(name), shareParamsMap);
				share(shareData);
				return;
			}
		}

		PlatformListFakeActivity platformListFakeActivity;
		try {
			if(OnekeyShareTheme.SKYBLUE == theme){
				platformListFakeActivity = (PlatformListFakeActivity) Class.forName("cn.sharesdk.onekeyshare.theme.skyblue.PlatformListPage").newInstance();
			}else{
				platformListFakeActivity = (PlatformListFakeActivity) Class.forName("cn.sharesdk.onekeyshare.theme.classic.PlatformListPage").newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		platformListFakeActivity.setDialogMode(dialogMode);
		platformListFakeActivity.setShareParamsMap(shareParamsMap);
		platformListFakeActivity.setSilent(silent);
		platformListFakeActivity.setCustomerLogos(customers);
		platformListFakeActivity.setBackgroundView(bgView);
		platformListFakeActivity.setHiddenPlatforms(hiddenPlatforms);
		platformListFakeActivity.setOnShareButtonClickListener(onShareButtonClickListener);
		platformListFakeActivity.setThemeShareCallback(new ThemeShareCallback() {
			public void doShare(HashMap<Platform, HashMap<String, Object>> shareData) {
				share(shareData);
			}
		});
		if (shareParamsMap.containsKey("platform")) {
			String name = String.valueOf(shareParamsMap.get("platform"));
			Platform platform = ShareSDK.getPlatform(name);
			platformListFakeActivity.showEditPage(context, platform);
			return;
		}
		platformListFakeActivity.show(context, null);
	}

	public void setTheme(OnekeyShareTheme theme) {
		this.theme = theme;
	}

	/** address是接收人地址，仅在信息和邮件使用，否则可以不提供 */
	public void setAddress(String address) {
		shareParamsMap.put("address", address);
	}

	/**
	 * title标题，在印象笔记、邮箱、信息、微信（包括好友、朋友圈和收藏）、
	 * 易信（包括好友、朋友圈）、人人网和QQ空间使用，否则可以不提供
	 */
	public void setTitle(String title) {
		shareParamsMap.put("title", title);
	}

	/** titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供 */
	public void setTitleUrl(String titleUrl) {
		shareParamsMap.put("titleUrl", titleUrl);
	}

	/** text是分享文本，所有平台都需要这个字段 */
	public void setText(String text) {
		shareParamsMap.put("text", text);
	}

	/** 获取text字段的值 */
	public String getText() {
		return shareParamsMap.containsKey("text") ? String.valueOf(shareParamsMap.get("text")) : null;
	}

	/** imagePath是本地的图片路径，除Linked-In外的所有平台都支持这个字段 */
	public void setImagePath(String imagePath) {
		if(!TextUtils.isEmpty(imagePath))
			shareParamsMap.put("imagePath", imagePath);
	}

	/** imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段 */
	public void setImageUrl(String imageUrl) {
		if (!TextUtils.isEmpty(imageUrl))
			shareParamsMap.put("imageUrl", imageUrl);
	}

	/** url在微信（包括好友、朋友圈收藏）和易信（包括好友和朋友圈）中使用，否则可以不提供 */
 	public void setUrl(String url) {
		shareParamsMap.put("url", url);
	}

	/** filePath是待分享应用程序的本地路劲，仅在微信（易信）好友和Dropbox中使用，否则可以不提供 */
	public void setFilePath(String filePath) {
		shareParamsMap.put("filePath", filePath);
	}

	/** comment是我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供 */
	public void setComment(String comment) {
		shareParamsMap.put("comment", comment);
	}

	/** site是分享此内容的网站名称，仅在QQ空间使用，否则可以不提供 */
	public void setSite(String site) {
		shareParamsMap.put("site", site);
	}

	/** siteUrl是分享此内容的网站地址，仅在QQ空间使用，否则可以不提供 */
	public void setSiteUrl(String siteUrl) {
		shareParamsMap.put("siteUrl", siteUrl);
	}

	/** foursquare分享时的地方名 */
	public void setVenueName(String venueName) {
		shareParamsMap.put("venueName", venueName);
	}

	/** foursquare分享时的地方描述 */
	public void setVenueDescription(String venueDescription) {
		shareParamsMap.put("venueDescription", venueDescription);
	}

	/** 分享地纬度，新浪微博、腾讯微博和foursquare支持此字段 */
	public void setLatitude(float latitude) {
		shareParamsMap.put("latitude", latitude);
	}

	/** 分享地经度，新浪微博、腾讯微博和foursquare支持此字段 */
	public void setLongitude(float longitude) {
		shareParamsMap.put("longitude", longitude);
	}

	/** 是否直接分享 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/** 设置编辑页的初始化选中平台 */
	public void setPlatform(String platform) {
		shareParamsMap.put("platform", platform);
	}

	/** 设置KakaoTalk的应用下载地址 */
	public void setInstallUrl(String installurl) {
		shareParamsMap.put("installurl", installurl);
	}

	/** 设置KakaoTalk的应用打开地址 */
	public void setExecuteUrl(String executeurl) {
		shareParamsMap.put("executeurl", executeurl);
	}

	/** 设置微信分享的音乐的地址 */
	public void setMusicUrl(String musicUrl) {
		shareParamsMap.put("musicUrl", musicUrl);
	}

	/** 设置自定义的外部回调 */
	public void setCallback(PlatformActionListener callback) {
		this.callback = callback;
	}

	/** 返回操作回调 */
	public PlatformActionListener getCallback() {
		return callback;
	}

	/** 设置用于分享过程中，根据不同平台自定义分享内容的回调 */
	public void setShareContentCustomizeCallback(ShareContentCustomizeCallback callback) {
		customizeCallback = callback;
	}

	/** 返回自定义分享内容的回调 */
	public ShareContentCustomizeCallback getShareContentCustomizeCallback() {
		return customizeCallback;
	}

	/** 设置自己图标和点击事件，可以重复调用添加多次 */
	public void setCustomerLogo(Bitmap enableLogo,Bitmap disableLogo, String label, OnClickListener ocListener) {
		CustomerLogo cl = new CustomerLogo();
		cl.label = label;
		cl.enableLogo = enableLogo;
		cl.disableLogo = disableLogo;
		cl.listener = ocListener;
		customers.add(cl);
	}

	/** 设置一个总开关，用于在分享前若需要授权，则禁用sso功能 */
 	public void disableSSOWhenAuthorize() {
		disableSSO = true;
	}

	/** 设置一个开关，用于微信分享视频 */
 	public void shareVideoToWechat() {
 		shareVideo = true;
	}

	/** 设置编辑页面的显示模式为Dialog模式 */
	public void setDialogMode() {
		dialogMode = true;
		shareParamsMap.put("dialogMode", dialogMode);
	}

	/** 添加一个隐藏的platform */
	public void addHiddenPlatform(String platform) {
		hiddenPlatforms.put(platform, platform);
	}

	/** 设置一个将被截图分享的View , surfaceView是截不了图片的*/
	public void setViewToShare(View viewToShare) {
		try {
			Bitmap bm = captureView(viewToShare, viewToShare.getWidth(), viewToShare.getHeight());
			shareParamsMap.put("viewToShare", bm);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/** 腾讯微博分享多张图片 */
	public void setImageArray(String[] imageArray) {
		shareParamsMap.put("imageArray", imageArray);
	}

	public void setEditPageBackground(View bgView) {
		this.bgView = bgView;
	}

	public void setOnShareButtonClickListener(PlatformListFakeActivity.OnShareButtonClickListener onShareButtonClickListener) {
		this.onShareButtonClickListener = onShareButtonClickListener;
	}

	/** 循环执行分享 */
	public void share(HashMap<Platform, HashMap<String, Object>> shareData) {
		boolean started = false;
		for (Entry<Platform, HashMap<String, Object>> ent : shareData.entrySet()) {
			Platform plat = ent.getKey();
			plat.SSOSetting(disableSSO);
			String name = plat.getName();

			boolean isGooglePlus = "GooglePlus".equals(name);
			if (isGooglePlus && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_google_plus_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isAlipay = "Alipay".equals(name);
			if (isAlipay && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_alipay_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isKakaoTalk = "KakaoTalk".equals(name);
			if (isKakaoTalk && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_kakaotalk_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isKakaoStory = "KakaoStory".equals(name);
			if (isKakaoStory && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_kakaostory_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isLine = "Line".equals(name);
			if (isLine && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_line_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isWhatsApp = "WhatsApp".equals(name);
			if (isWhatsApp && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_whatsapp_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isPinterest = "Pinterest".equals(name);
			if (isPinterest && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_pinterest_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			if ("Instagram".equals(name) && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_instagram_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			boolean isLaiwang = "Laiwang".equals(name);
			boolean isLaiwangMoments = "LaiwangMoments".equals(name);
			if(isLaiwang || isLaiwangMoments){
				if (!plat.isClientValid()) {
					Message msg = new Message();
					msg.what = MSG_TOAST;
					int resId = getStringRes(context, "ssdk_laiwang_client_inavailable");
					msg.obj = context.getString(resId);
					UIHandler.sendMessage(msg, this);
					continue;
				}
			}

			boolean isYixin = "YixinMoments".equals(name) || "Yixin".equals(name);
			if (isYixin && !plat.isClientValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				int resId = getStringRes(context, "ssdk_yixin_client_inavailable");
				msg.obj = context.getString(resId);
				UIHandler.sendMessage(msg, this);
				continue;
			}

			HashMap<String, Object> data = ent.getValue();
			int shareType = Platform.SHARE_TEXT;
			String imagePath = String.valueOf(data.get("imagePath"));
			if (imagePath != null && (new File(imagePath)).exists()) {
				shareType = Platform.SHARE_IMAGE;
				if (imagePath.endsWith(".gif")) {
					shareType = Platform.SHARE_EMOJI;
				} else if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
					shareType = Platform.SHARE_WEBPAGE;
					if (shareVideo) {
						shareType = Platform.SHARE_VIDEO;
					} else if (data.containsKey("musicUrl") && !TextUtils.isEmpty(data.get("musicUrl").toString())) {
						shareType = Platform.SHARE_MUSIC;
					}
				}
			} else {
				Bitmap viewToShare = (Bitmap) data.get("viewToShare");
				if (viewToShare != null && !viewToShare.isRecycled()) {
					shareType = Platform.SHARE_IMAGE;
					if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
						shareType = Platform.SHARE_WEBPAGE;
						if (shareVideo) {
							shareType = Platform.SHARE_VIDEO;
						} else if (data.containsKey("musicUrl") && !TextUtils.isEmpty(data.get("musicUrl").toString())) {
							shareType = Platform.SHARE_MUSIC;
						}
					}
				} else {
					Object imageUrl = data.get("imageUrl");
					if (imageUrl != null && !TextUtils.isEmpty(String.valueOf(imageUrl))) {
						shareType = Platform.SHARE_IMAGE;
						if (String.valueOf(imageUrl).endsWith(".gif")) {
							shareType = Platform.SHARE_EMOJI;
						} else if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
							shareType = Platform.SHARE_WEBPAGE;
							if (shareVideo) {
								shareType = Platform.SHARE_VIDEO;
							} else if (data.containsKey("musicUrl") && !TextUtils.isEmpty(data.get("musicUrl").toString())) {
								shareType = Platform.SHARE_MUSIC;
							}
						}
					}
				}
			}
			data.put("shareType", shareType);

			if (!started) {
				started = true;
//				if (this == callback) {
					int resId = getStringRes(context, "ssdk_oks_sharing");
					if (resId > 0) {
						showNotification(context.getString(resId));
					}
//				}
			}
			plat.setPlatformActionListener(callback);
			ShareCore shareCore = new ShareCore();
			shareCore.setShareContentCustomizeCallback(customizeCallback);
			shareCore.share(plat, data);
		}
	}

	public void onComplete(Platform platform, int action,
			HashMap<String, Object> res) {
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	public void onError(Platform platform, int action, Throwable t) {
		t.printStackTrace();

		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = t;
		UIHandler.sendMessage(msg, this);

		// 分享失败的统计
		ShareSDK.logDemoEvent(4, platform);
	}

	public void onCancel(Platform platform, int action) {
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);

		// 分享失败的统计
		ShareSDK.logDemoEvent(5, platform);
	}

	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case MSG_TOAST: {
				String text = String.valueOf(msg.obj);
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			}
			break;
			case MSG_ACTION_CCALLBACK: {
				switch (msg.arg1) {
					case 1: {
						// 成功
						int resId = getStringRes(context, "ssdk_oks_share_completed");
						if (resId > 0) {
							showNotification(context.getString(resId));
						}
					}
					break;
					case 2: {
						// 失败
						String expName = msg.obj.getClass().getSimpleName();
						if ("WechatClientNotExistException".equals(expName)
								|| "WechatTimelineNotSupportedException".equals(expName)
								|| "WechatFavoriteNotSupportedException".equals(expName)) {
							int resId = getStringRes(context, "ssdk_wechat_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("GooglePlusClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "ssdk_google_plus_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("QQClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "ssdk_qq_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("YixinClientNotExistException".equals(expName)
								|| "YixinTimelineNotSupportedException".equals(expName)) {
							int resId = getStringRes(context, "ssdk_yixin_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						} else if ("KakaoTalkClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "ssdk_kakaotalk_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}else if ("KakaoStoryClientNotExistException".equals(expName)) {
							int resId = getStringRes(context, "ssdk_kakaostory_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}else if("WhatsAppClientNotExistException".equals(expName)){
							int resId = getStringRes(context, "ssdk_whatsapp_client_inavailable");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}else {
							int resId = getStringRes(context, "ssdk_oks_share_failed");
							if (resId > 0) {
								showNotification(context.getString(resId));
							}
						}
					}
					break;
					case 3: {
						// 取消
						int resId = getStringRes(context, "ssdk_oks_share_canceled");
						if (resId > 0) {
							showNotification(context.getString(resId));
						}
					}
					break;
				}
			}
			break;
			case MSG_CANCEL_NOTIFY: {
				NotificationManager nm = (NotificationManager) msg.obj;
				if (nm != null) {
					nm.cancel(msg.arg1);
				}
			}
			break;
		}
		return false;
	}

	// 在状态栏提示分享操作
	private void showNotification(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/** 是否支持QQ,QZone授权登录后发微博 */
	public void setShareFromQQAuthSupport(boolean shareFromQQLogin)
	{
		shareParamsMap.put("isShareTencentWeibo", shareFromQQLogin);
	}
}
