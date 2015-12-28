package cn.sharesdk.onekeyshare;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;

public interface ShareContentCustomizeCallback {

	public void onShare(Platform platform, ShareParams paramsToShare);

}
