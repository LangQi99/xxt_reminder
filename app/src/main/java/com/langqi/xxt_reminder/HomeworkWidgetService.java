package com.langqi.xxt_reminder;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.langqi.xxt_reminder.model.HomeworkInfo;
import java.util.ArrayList;
import java.util.List;

public class HomeworkWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HomeworkRemoteViewsFactory(getApplicationContext());
    }

    public static List<HomeworkInfo> homeworkList = new ArrayList<>();

    static class HomeworkRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;
        private List<HomeworkInfo> data = new ArrayList<>();

        HomeworkRemoteViewsFactory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            data.clear();
            data.addAll(homeworkList);
        }

        @Override
        public void onDataSetChanged() {
            data.clear();
            data.addAll(homeworkList);
        }

        @Override
        public void onDestroy() {
            data.clear();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            HomeworkInfo hw = data.get(position);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.item_widget_homework);
            rv.setTextViewText(R.id.textViewSubject, hw.subject == null ? "" : hw.subject);
            rv.setTextViewText(R.id.textViewHomeworkName, hw.homeworkName == null ? "" : hw.homeworkName);
            rv.setTextViewText(R.id.textViewStatus, hw.homeworkStatus == null ? "" : hw.homeworkStatus);
            if (hw.deadline != null && !hw.deadline.isEmpty()) {
                rv.setTextViewText(R.id.textViewDeadline, "截止时间: " + hw.deadline);
            } else {
                rv.setTextViewText(R.id.textViewDeadline, "");
            }
            if (hw.submitted) {
                rv.setImageViewResource(R.id.imageViewStatus, R.drawable.ic_check_circle_green_24dp);
            } else {
                rv.setImageViewResource(R.id.imageViewStatus, R.drawable.ic_error_red_24dp);
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}