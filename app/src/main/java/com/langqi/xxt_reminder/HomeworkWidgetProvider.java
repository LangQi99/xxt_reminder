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
import android.util.Log;
public class HomeworkWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.langqi.xxt_reminder.ACTION_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("HomeworkWidgetProvider", "onUpdate"+appWidgetIds.length);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_homework);

            // 设置按钮点击事件
            Intent intent = new Intent(context, HomeworkWidgetProvider.class);
            intent.setAction(ACTION_REFRESH);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent);

            // 设置ListView的RemoteAdapter
            Intent serviceIntent = new Intent(context, HomeworkWidgetService.class);
            views.setRemoteAdapter(R.id.listViewHomework, serviceIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("HomeworkWidgetProvider", "onReceive"+intent.getAction());
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                // 按钮被点击时，先将按钮文字改为'test'
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_homework);
                views.setTextViewText(R.id.btn_refresh, "test");
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
                // 然后再异步获取作业
                fetchAndUpdateHomework(context, appWidgetId);
            }
        }
    }

    private void fetchAndUpdateHomework(Context context, int appWidgetId) {
        Log.d("HomeworkWidgetProvider", "fetchAndUpdateHomework"+appWidgetId+" "+context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("account", ""));
        String account = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("account", "");
        String password = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("password", "");
        NetworkManager nm = new NetworkManager();
        nm.loginAndGetHomeworkAsync(account, password, homeworkList -> {
            // 更新RemoteViewsService的数据
            HomeworkWidgetService.homeworkList.clear();
            if (homeworkList != null) {
                HomeworkWidgetService.homeworkList.addAll(homeworkList);
            }
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_homework);
            Intent serviceIntent = new Intent(context, HomeworkWidgetService.class);
            views.setRemoteAdapter(R.id.listViewHomework, serviceIntent);
            views.setTextViewText(R.id.btn_refresh, "test");
            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewHomework);
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
        });
    }
} 