package com.jaoafa.PeriodMatch2.Lib;

import java.io.IOException;

import org.json.simple.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Discord {
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	String token;

	public Discord(String token) {
		this.token = token;
	}

	/**
	 * Discordにメッセージを送信します。
	 * @param channelId チャンネルID
	 * @param content メッセージテキスト
	 * @return 成功の場合true
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public boolean sendMessage(String channelId, String content) {
		try {
			JSONObject paramobj = new JSONObject();
			paramobj.put("content", content);

			String url = "https://discordapp.com/api/channels/" + channelId + "/messages";
			RequestBody body = RequestBody.create(paramobj.toJSONString(), JSON);
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
					.url(url)
					.addHeader("Content-Type", "application/json")
					.addHeader("Authorization", "Bot " + token)
					.addHeader("User-Agent", "DiscordBot (https://jaoafa.com, v0.0.1)")
					.post(body)
					.build();
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					return true;
				}
				if (response.body() != null) {
					System.out.println("[PeriodMatch2] Discord Warning: " + response.body().string());
					return false;
				}
				return false;
			}
		} catch (IOException e) {
			System.out.println("[PeriodMatch2] Discord IOException: " + e.getMessage());
			return false;
		}
	}
}
