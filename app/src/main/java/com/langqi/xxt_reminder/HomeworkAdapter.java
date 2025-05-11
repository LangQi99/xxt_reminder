package com.langqi.xxt_reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.langqi.xxt_reminder.model.HomeworkInfo;

public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {
    private List<HomeworkInfo> homeworkList;

    public HomeworkAdapter(List<HomeworkInfo> homeworkList) {
        this.homeworkList = homeworkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_homework, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeworkInfo item = homeworkList.get(position);
        holder.textViewSubject.setText(item.subject == null ? "" : item.subject);
        holder.textViewHomeworkName.setText(item.homeworkName == null ? "" : item.homeworkName);
        holder.textViewStatus.setText(item.homeworkStatus == null ? "" : item.homeworkStatus);
        if (item.deadline != null && !item.deadline.isEmpty()) {
            holder.textViewDeadline.setText(item.deadline);
            holder.textViewDeadline.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDeadline.setVisibility(View.GONE);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("科目: ").append(item.subject == null ? "" : item.subject).append("\n");
        sb.append("作业名: ").append(item.homeworkName == null ? "" : item.homeworkName).append("\n");
        sb.append("状态: ").append(item.homeworkStatus == null ? "" : item.homeworkStatus).append("\n");
        if (item.deadline != null && !item.deadline.isEmpty()) {
            sb.append("截止时间: ").append(item.deadline).append("\n");
        }
        holder.textViewContent.setText(sb.toString());
        if (item.submitted) {
            holder.imageViewStatus.setImageResource(R.drawable.ic_check_circle_green_24dp); // 绿色对勾
        } else {
            holder.imageViewStatus.setImageResource(R.drawable.ic_error_red_24dp); // 红色感叹号
        }
    }

    @Override
    public int getItemCount() {
        return homeworkList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewStatus;
        TextView textViewContent;
        TextView textViewSubject;
        TextView textViewHomeworkName;
        TextView textViewStatus;
        TextView textViewDeadline;

        ViewHolder(View itemView) {
            super(itemView);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
            textViewHomeworkName = itemView.findViewById(R.id.textViewHomeworkName);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDeadline = itemView.findViewById(R.id.textViewDeadline);
        }
    }
}