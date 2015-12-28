package cn.sharesdk.onekeyshare;

import android.os.Handler.Callback;
import android.os.Message;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import com.mob.tools.utils.UIHandler;

/**
 * 此类在Onekeyshare中并无用途，只是在Socialization中考虑到耦合度，需要通过反射的方式操作Onekeyshare，
 *而原先的{@link ShareContentCustomizeCallback}无法完成此需求，故创建本类，以供外部设置操作回调。
 *
 * @author Brook
 */
public class ReflectableShareContentCustomizeCallback implements ShareContentCustomizeCallback {
	private int onShareWhat;
	private Callback onShareCallback;

	public void setOnShareCallback(int what, Callback callback) {
		onShareWhat = what;
		onShareCallback = callback;
	}

	@Override
	public void onShare(Platform platform, ShareParams paramsToShare) {
		if (onShareCallback != null) {
			Message msg = new Message();
			msg.what = onShareWhat;
			msg.obj = new Object[] {platform, paramsToShare};
			UIHandler.sendMessage(msg, onShareCallback);
		}
	}

}
