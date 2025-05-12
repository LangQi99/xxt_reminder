package com.langqi.xxt_reminder.network;

import android.util.Log;
import com.langqi.xxt_reminder.model.HomeworkInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkManager {
    private OkHttpClient client;
    private Map<String, List<Cookie>> cookieStore = new HashMap<>();
    private String cookieString = "";
    private boolean loginSuccess = false;
    private String account;
    private String password;

    public NetworkManager() {
        client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .build();
    }

    public interface LoginCallback {
        void onLoginResult(boolean success);
    }

    public interface HomeworkCallback {
        void onHomeworkResult(List<HomeworkInfo> homeworkList);
    }

    public void loginAsync(String account, String password, LoginCallback callback) {
        this.account = account;
        this.password = password;
        new Thread(() -> {
            boolean result = login(account, password);
            if (callback != null) {
                callback.onLoginResult(result);
            }
        }).start();
    }

    private boolean login(String account, String password) {
        Log.d("NetworkManager", "登录中");
        String url = "https://passport2-api.chaoxing.com/v11/loginregister?code=" + password
                + "&cx_xxt_passport=json&uname=" + account + "&loginType=1&roleSelect=true";
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Encoding", "gzip")
                .header("Accept-Language", "zh-Hans-CN;q=1, zh-Hant-CN;q=0.9")
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Log.d("NetworkManager", body);
            if (body.contains("验证通过")) {
                loginSuccess = true;
                Log.d("NetworkManager", "初始化登录成功");
                List<Cookie> cookies = cookieStore.get(HttpUrl.parse(url).host());
                if (cookies != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Cookie c : cookies) {
                        sb.append(c.name()).append("=").append(c.value()).append(";");
                    }
                    cookieString = sb.toString();
                    Log.d("NetworkManager", "拼接后的Cookie: " + cookieString);
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("NetworkManager", "登录失败");
            Log.d("NetworkManager", e.getMessage());
        }
        return false;
    }

    public void getAllHomeworkSimpleAsync(HomeworkCallback callback) {
        new Thread(() -> {
            List<HomeworkInfo> result = getAllHomeworkSimple();
            if (callback != null) {
                callback.onHomeworkResult(result);
            }
        }).start();
    }

    private List<HomeworkInfo> getAllHomeworkSimple() {
        List<HomeworkInfo> result = new ArrayList<>();
        if (!loginSuccess) {
            result.add(new HomeworkInfo("", "", "登录失败", "", "", ""));
            return result;
        }
        String url = "https://mooc1-api.chaoxing.com/work/stu-work";
        Log.d("Using Cookie", cookieString);
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                .header("Cookie", cookieString)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Document doc = Jsoup.parse(html);
            Elements items = doc.select("li");
            for (Element i : items) {
                String homeworkName = i.selectFirst("p").text();
                Elements spans = i.select("span");
                String homeworkStatus = spans.get(0).text();
                String subject = spans.get(1).text();
                String dataUrl = i.attr("data");
                String taskrefId = null;
                if (dataUrl != null && dataUrl.contains("taskrefId=")) {
                    try {
                        taskrefId = dataUrl.split("taskrefId=")[1].split("&")[0];
                    } catch (Exception e) {
                        taskrefId = null;
                    }
                }
                String deadline = null;
                Element spanWithAria = i.selectFirst("span[aria-label]");
                if (spanWithAria != null) {
                    String ariaLabel = spanWithAria.attr("aria-label");
                    int idx = ariaLabel.indexOf("剩余时间");
                    if (idx != -1) {
                        deadline = ariaLabel.substring(idx + 4);
                    }
                }
                result.add(new HomeworkInfo(subject, homeworkName, homeworkStatus, dataUrl, taskrefId, deadline));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.add(new HomeworkInfo("", "", "获取作业失败", "", "", ""));
        }
        return result;
    }

    /**
     * 新增：自动登录并获取作业列表，适用于JSESSION一次性场景
     */
    public void loginAndGetHomeworkAsync(String account, String password, HomeworkCallback callback) {
        this.account = account;
        this.password = password;
        new Thread(() -> {
            boolean loginResult = login(account, password);
            List<HomeworkInfo> result;
            Log.d("NetworkManager", "loginResult=" + loginResult);
            if (loginResult) {
                result = getAllHomeworkSimple();
                Log.d("NetworkManager", "result=" + result.size());
            } else {
                result = new ArrayList<>();
                result.add(new HomeworkInfo("", "", "登录失败", "", "", ""));
            }
            Log.d("NetworkManager", "result=" + result.size());
            Log.d("NetworkManager", "result=" + result.get(0).homeworkStatus);
            Log.d("NetworkManager", "result=" + result.get(0).homeworkName);
            Log.d("NetworkManager", "result=" + result.get(0).subject);
            Log.d("NetworkManager", "result=" + result.get(0).taskrefId);
            Log.d("NetworkManager", "result=" + result.get(0).deadline);
            Log.d("NetworkManager", "callback=" + callback);
            if (callback != null) {
                callback.onHomeworkResult(result);
            }
        }).start();
    }
}