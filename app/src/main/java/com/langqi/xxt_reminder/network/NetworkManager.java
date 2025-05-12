package com.langqi.xxt_reminder.network;

import android.util.Log;
import com.langqi.xxt_reminder.model.HomeworkInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.CookieManager;
import java.net.CookiePolicy;
import okhttp3.Call;
import okhttp3.JavaNetCookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.Collections;

public class NetworkManager {
    private OkHttpClient client;
    private CookieManager cookieManager;
    private boolean loginSuccess = false;
    private String account;
    private String password;

    public NetworkManager() {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
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
                Log.d("NetworkManager", "登录成功");
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
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
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

    private List<HomeworkInfo> getAllExamSimple() {
        List<HomeworkInfo> result = new ArrayList<>();
        if (!loginSuccess) {
            result.add(new HomeworkInfo("", "", "登录失败", "", "", ""));
            return result;
        }
        String url = "https://mooc1-api.chaoxing.com/exam-ans/exam/phone/examcode";
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                .build();
        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Document doc = Jsoup.parse(html);
            Log.d("html", html);
            Elements items = doc.select("li");
            for (Element i : items) {
                String homeworkName = "";
                Element dt = i.selectFirst("dt");
                if (dt != null) {
                    homeworkName = dt.text();
                }

                String homeworkStatus = "";
                Element statusSpan = i.selectFirst("span.ks_state");
                if (statusSpan != null) {
                    homeworkStatus = statusSpan.text();
                }

                String subject = "《考试》";

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
                Element dd = i.selectFirst("dd");
                if (dd != null) {
                    deadline = dd.text();
                } else {
                    Element dlWithAria = i.selectFirst("dl[aria-label]");
                    if (dlWithAria != null) {
                        String ariaLabel = dlWithAria.attr("aria-label");
                        int idx = ariaLabel.indexOf("剩余");
                        if (idx != -1) {
                            deadline = ariaLabel.substring(idx).trim();
                        }
                    }
                }
                Log.d("NetworkManager-exam", "homeworkName=" + homeworkName);
                Log.d("NetworkManager-exam", "homeworkStatus=" + homeworkStatus);
                Log.d("NetworkManager-exam", "dataUrl=" + dataUrl);
                Log.d("NetworkManager-exam", "taskrefId=" + taskrefId);
                Log.d("NetworkManager-exam", "deadline=" + deadline);
                result.add(new HomeworkInfo(subject, homeworkName, homeworkStatus, dataUrl, taskrefId, deadline));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.add(new HomeworkInfo("", "", "获取作业失败", "", "", ""));
        }
        return result;
    }

    public void loginAndGetHomeworkAsync(String account, String password, HomeworkCallback callback) {
        this.account = account;
        this.password = password;
        new Thread(() -> {
            boolean loginResult = login(account, password);
            List<HomeworkInfo> result;
            List<HomeworkInfo> result2;
            Log.d("NetworkManager", "loginResult=" + loginResult);
            if (loginResult) {
                result = getAllHomeworkSimple();
                Log.d("NetworkManager-result", "result=" + result.size());
                result2 = getAllExamSimple();
                Log.d("NetworkManager-result2", "result2=" + result2.size());
                result.addAll(result2);
            } else {
                result = new ArrayList<>();
                result.add(new HomeworkInfo("", "", "登录失败", "", "", ""));
            }
            // 将未提交的作业和考试按照截止时间排序放到最前面
            List<HomeworkInfo> unsubmitted = new ArrayList<>();
            List<HomeworkInfo> submitted = new ArrayList<>();

            // 分离未提交和已提交的作业
            for (HomeworkInfo info : result) {
                if (!info.submitted && info.deadline != null) {
                    unsubmitted.add(info);
                } else {
                    submitted.add(info);
                }
            }

            // 对未提交的作业按截止时间排序
            Collections.sort(unsubmitted, (a, b) -> {
                if (a.deadline == null)
                    return 1;
                if (b.deadline == null)
                    return -1;
                return a.deadline.compareTo(b.deadline);
            });

            // 清空原列表并重新添加排序后的内容
            result.clear();
            result.addAll(unsubmitted);
            result.addAll(submitted);
            Log.d("NetworkManager", "result=" + result.size());
            if (!result.isEmpty()) {
                HomeworkInfo firstItem = result.get(0);
                Log.d("NetworkManager", "result=" + firstItem.homeworkStatus);
                Log.d("NetworkManager", "result=" + firstItem.homeworkName);
                Log.d("NetworkManager", "result=" + firstItem.subject);
                Log.d("NetworkManager", "result=" + firstItem.taskrefId);
                Log.d("NetworkManager", "result=" + firstItem.deadline);
            }
            Log.d("NetworkManager", "callback=" + callback);
            if (callback != null) {
                callback.onHomeworkResult(result);
            }
        }).start();
    }
}