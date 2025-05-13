package com.happ.antivirus_java.ui.url;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

public class UrlViewModel extends ViewModel {

    private final MutableLiveData<String> _urlInfo = new MutableLiveData<>();
    public LiveData<String> urlInfo = _urlInfo;

    private final String whoisApiKey = "at_MWB0JPGr5ogjPOIuaCxYJ7F8WG6mx";
    private final String virusTotalApiKey = "47485c20a183f23f1311ee3d02b4ab649aef896ef7cd68fbad5164950ef67b88"; // 여기에 실제 키를 입력

    public void getURLInfo(String inputUrl) {
        new Thread(() -> {
            try {
                // WHOIS 정보 조회
                String whoisResult = getWhoisInfo(inputUrl);

                // VirusTotal 분석 정보 조회
                String vtResult = getVirusTotalInfo(inputUrl);

                // UI에 우선 WHOIS 정보 전달
//                _urlInfo.postValue(whoisResult);

                // 두 결과 합쳐서 UI 갱신
                String fullResult = whoisResult + "\n\n🛡️ [바이러스 토탈 분석 결과]\n" + vtResult;
                _urlInfo.postValue(fullResult);

            } catch (Exception e) {
                e.printStackTrace();
                _urlInfo.postValue("❌ URL 정보 조회 중 오류가 발생했습니다.");
            }
        }).start();
    }

    private String getWhoisInfo(String inputUrl) throws Exception {
        String apiUrl = "https://www.whoisxmlapi.com/whoisserver/WhoisService"
                + "?apiKey=" + whoisApiKey
                + "&domainName=" + inputUrl
                + "&outputFormat=JSON";
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");

        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        conn.disconnect();

        JSONObject json = new JSONObject(result.toString());
        JSONObject whoisRecord = json.getJSONObject("WhoisRecord");

        String domain = whoisRecord.optString("domainName", "정보 없음");

        String registrantCountry = "정보 없음";
        if (whoisRecord.has("registrant")) {
            JSONObject registrant = whoisRecord.getJSONObject("registrant");
            registrantCountry = registrant.optString("country", "정보 없음");
        }

        String ipAddress = "정보 없음";
        try {
            InetAddress inetAddress = InetAddress.getByName(domain);
            ipAddress = inetAddress.getHostAddress();
        } catch (Exception e) {
            Log.e("##DNS", "IP 변환 실패: " + e.getMessage());
        }

        return "🌐 도메인 이름: " + domain + "\n"
                + "📡 IP 주소: " + ipAddress + "\n"
                + "📍 등록 국가: " + registrantCountry;
    }

    private String getVirusTotalInfo(String urlToCheck) throws Exception {
        // 1. POST 분석 요청
        URL vtPostUrl = new URL("https://www.virustotal.com/api/v3/urls");
        HttpURLConnection postConn = (HttpURLConnection) vtPostUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("x-apikey", virusTotalApiKey);
        postConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        postConn.setDoOutput(true);

        String postData = "url=" + URLEncoder.encode(urlToCheck, "UTF-8");
        try (OutputStream os = postConn.getOutputStream()) {
            os.write(postData.getBytes("UTF-8"));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(postConn.getInputStream(), "UTF-8"));
        StringBuilder postResponse = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            postResponse.append(line);
        }
        br.close();

        // ✅ POST 응답 로그 출력
        Log.d("VT_POST_RESPONSE", postResponse.toString());

        JSONObject postJson = new JSONObject(postResponse.toString());
        String analysisId = postJson.getJSONObject("data").getString("id");

        // 2. 일정 시간 대기 후 분석 결과 요청
        Thread.sleep(20000);

        URL vtGetUrl = new URL("https://www.virustotal.com/api/v3/analyses/" + analysisId);
        HttpURLConnection getConn = (HttpURLConnection) vtGetUrl.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setRequestProperty("x-apikey", virusTotalApiKey);

        BufferedReader getReader = new BufferedReader(new InputStreamReader(getConn.getInputStream(), "UTF-8"));
        StringBuilder getResponse = new StringBuilder();
        while ((line = getReader.readLine()) != null) {
            getResponse.append(line);
        }
        getReader.close();

        // ✅ GET 응답 로그 출력
        Log.d("VT_GET_RESPONSE", getResponse.toString());

        JSONObject getJson = new JSONObject(getResponse.toString());
        JSONObject stats = getJson.getJSONObject("data")
                .getJSONObject("attributes")
                .getJSONObject("stats");

        int harmless = stats.optInt("harmless", 0);
        int suspicious = stats.optInt("suspicious", 0);
        int malicious = stats.optInt("malicious", 0);

        return "✅ 정상: " + harmless + "개\n"
                + "⚠️ 의심됨: " + suspicious + "개\n"
                + "❌ 악성: " + malicious + "개";
    }

}
