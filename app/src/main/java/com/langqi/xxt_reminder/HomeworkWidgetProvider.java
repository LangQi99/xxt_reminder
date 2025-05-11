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
        // 这里建议用SharedPreferences获取账号密码
        String account = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("account", "");
        String password = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("password", "");
        NetworkManager nm = new NetworkManager();
        nm.loginAndGetHomeworkAsync(account, password, homeworkList -> {
            StringBuilder sb = new StringBuilder();
            for (HomeworkInfo hw : homeworkList) {
                sb.append(hw.subject).append(" | ").append(hw.homeworkName).append(" | ").append(hw.homeworkStatus);
                if (hw.deadline != null && !hw.deadline.isEmpty()) {
                    sb.append(" | 截止: ").append(hw.deadline);
                }
                sb.append("\n");
            }
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_homework);
            views.setTextViewText(R.id.tv_homework, sb.toString());
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
        });
    }
} 