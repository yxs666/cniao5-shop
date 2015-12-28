package cn.sharesdk.onekeyshare;

import java.util.ArrayList;
import java.util.HashMap;

import com.mob.tools.FakeActivity;
import cn.sharesdk.framework.Platform;

public class FollowerListFakeActivity extends FakeActivity {
	protected Platform platform;

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	public Platform getPlatform() {
		return platform;
	}

	public boolean isRadioMode(String platformName) {
		return "FacebookMessenger".equals(platformName);
	}

	public void setResultForChecked(ArrayList<String> checked) {
		HashMap<String, Object> res = new HashMap<String, Object>();
		res.put("selected", checked);
		res.put("platform", platform);
		setResult(res);
	}

	public static class Following {
		public boolean checked;
		public String screenName;
		public String description;
		public String uid;
		public String icon;
		//@Name 用于微博等提示或关联某个人
		public String atName;
	}

	public static class FollowersResult {
		public ArrayList<Following> list;
		public boolean hasNextPage = false;
	}

	public static FollowersResult parseFollowers(String platformName, HashMap<String, Object> res, HashMap<String, Boolean> uidMap) {
		if (res == null || res.size() <= 0) {
			return null;
		}

		boolean hasNext = false;
		ArrayList<Following> data = new ArrayList<Following>();
		if ("SinaWeibo".equals(platformName)) {
			// users[id, name, description]
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, Object>> users
					= (ArrayList<HashMap<String,Object>>) res.get("users");
			for (HashMap<String, Object> user : users) {
				String uid = String.valueOf(user.get("id"));
				if (!uidMap.containsKey(uid)) {
					Following following = new Following();
					following.uid = uid;
					following.screenName = String.valueOf(user.get("name"));
					following.description = String.valueOf(user.get("description"));
					following.icon = String.valueOf(user.get("profile_image_url"));
					following.atName = following.screenName;
					uidMap.put(following.uid, true);
					data.add(following);
				}
			}
			hasNext = (Integer) res.get("total_number") > uidMap.size();
		}
		else if ("TencentWeibo".equals(platformName)) {
			hasNext = ((Integer)res.get("hasnext") == 0);
			// info[nick, name, tweet[text]]
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, Object>> infos
					= (ArrayList<HashMap<String,Object>>) res.get("info");
			for (HashMap<String, Object> info : infos) {
				String uid = String.valueOf(info.get("name"));
				if (!uidMap.containsKey(uid)) {
					Following following = new Following();
					following.screenName = String.valueOf(info.get("nick"));
					following.uid = uid;
					following.atName = uid;
					@SuppressWarnings("unchecked")
					ArrayList<HashMap<String, Object>> tweets = (ArrayList<HashMap<String,Object>>) info.get("tweet");
					for (HashMap<String, Object> tweet : tweets) {
						following.description = String.valueOf(tweet.get("text"));
						break;
					}
					following.icon = String.valueOf(info.get("head")) + "/100";
					uidMap.put(following.uid, true);
					data.add(following);
				}
			}
		}
		else if ("Facebook".equals(platformName)) {
			// data[id, name]
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, Object>> datas
					= (ArrayList<HashMap<String,Object>>) res.get("data");
			for (HashMap<String, Object> d : datas) {
				String uid = String.valueOf(d.get("id"));
				if (!uidMap.containsKey(uid)) {
					Following following = new Following();
					following.uid = uid;
					following.atName = "["+uid+"]";
					following.screenName = String.valueOf(d.get("name"));
					@SuppressWarnings("unchecked")
					HashMap<String, Object> picture = (HashMap<String, Object>) d.get("picture");
					if (picture != null) {
						@SuppressWarnings("unchecked")
						HashMap<String, Object> pData = (HashMap<String, Object>) picture.get("data");
						following.icon = String.valueOf(pData.get("url"));
					}
					uidMap.put(following.uid, true);
					data.add(following);
				}
			}
			@SuppressWarnings("unchecked")
			HashMap<String, Object> paging = (HashMap<String, Object>) res.get("paging");
			hasNext = paging.containsKey("next");
		}
		else if ("Twitter".equals(platformName)) {
			// users[screen_name, name, description]
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, Object>> users
					= (ArrayList<HashMap<String,Object>>) res.get("users");
			for (HashMap<String, Object> user : users) {
				String uid = String.valueOf(user.get("screen_name"));
				if (!uidMap.containsKey(uid)) {
					Following following = new Following();
					following.uid = uid;
					following.atName = uid;
					following.screenName = String.valueOf(user.get("name"));
					following.description = String.valueOf(user.get("description"));
					following.icon = String.valueOf(user.get("profile_image_url"));
					uidMap.put(following.uid, true);
					data.add(following);
				}
			}
		}
		else if ("FacebookMessenger".equals(platformName)) {
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, Object>> users
					= (ArrayList<HashMap<String,Object>>) res.get("users");
			for (HashMap<String, Object> user : users) {
				String userAddr = String.valueOf(user.get("jid"));
				if (!uidMap.containsKey(userAddr)) {
					Following following = new Following();
					following.uid = userAddr;
					following.atName = userAddr;
					following.screenName = String.valueOf(user.get("name"));
					uidMap.put(following.uid, true);
					data.add(following);
				}
			}
			hasNext = false;
		}

		FollowersResult ret = new FollowersResult();
		ret.list = data;
		ret.hasNextPage = hasNext;
		return ret;
	}
}
