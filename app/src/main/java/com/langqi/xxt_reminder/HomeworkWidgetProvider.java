package com.langqi.xxt_reminder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.langqi.xxt_reminder.network.NetworkManager;
import com.langqi.xxt_reminder.model.HomeworkInfo;
import java.util.List;

public class HomeworkWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.langqi.xxt_reminder.ACTION_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_homework);

            // 设置按钮点击事件
            Intent intent = new Intent(context, HomeworkWidgetProvider.class);
            intent.setAction(ACTION_REFRESH);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                fetchAndUpdateHomework(context, appWidgetId);
            }
        }
    }

    private void fetchAndUpdateHomework(Context context, int appWidgetId) {
        String account = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("account", "");
        String password = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("password", "");
        NetworkManager nm = new NetworkManager();
        nm.loginAndGetHomeworkAsync(account, password, homeworkList -> {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_homework);
            if (homeworkList == null || homeworkList.isEmpty()) {
                views.setTextViewText(R.id.textViewSubject, "无作业");
                views.setTextViewText(R.id.textViewHomeworkName, "");
                views.setTextViewText(R.id.textViewStatus, "");
                views.setTextViewText(R.id.textViewDeadline, "");
                views.setImageViewResource(R.id.imageViewStatus, R.drawable.ic_check_circle_green_24dp);
            } else {
                HomeworkInfo hw = homeworkList.get(0);
                views.setTextViewText(R.id.textViewSubject, hw.subject == null ? "" : hw.subject);
                views.setTextViewText(R.id.textViewHomeworkName, hw.homeworkName == null ? "" : hw.homeworkName);
                views.setTextViewText(R.id.textViewStatus, hw.homeworkStatus == null ? "" : hw.homeworkStatus);
                if (hw.deadline != null && !hw.deadline.isEmpty()) {
                    views.setTextViewText(R.id.textViewDeadline, "截止时间: " + hw.deadline);
                } else {
                    views.setTextViewText(R.id.textViewDeadline, "");
                }
                if (hw.submitted) {
                    views.setImageViewResource(R.id.imageViewStatus, R.drawable.ic_check_circle_green_24dp);
                } else {
                    views.setImageViewResource(R.id.imageViewStatus, R.drawable.ic_error_red_24dp);
                }
            }
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
        });
    }
} 