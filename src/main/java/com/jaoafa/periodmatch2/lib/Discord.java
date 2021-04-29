package com.jaoafa.periodmatch2.lib;

import java.io.IOException;

import okhttp3.*;
import org.json.JSONObject;

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
	 */
	public void sendMessage(String channelId, String content) {
		try {
			JSONObject param = new JSONObject();
			param.put("content", content);

			String url = "https://discord.com/api/channels/" + channelId + "/messages";
			RequestBody body = RequestBody.create(JSON, param.toString());
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
					return;
				}
                ResponseBody res_body = response.body();
				if (res_body != null) {
					System.out.println("[PeriodMatch2] Discord Warning: " + res_body.string());
                }
            }
		} catch (IOException e) {
			System.out.println("[PeriodMatch2] Discord IOException: " + e.getMessage());
        }
	}
}
