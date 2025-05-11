package com.langqi.xxt_reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {
    private List<HomeworkItem> homeworkList;

    public HomeworkAdapter(List<HomeworkItem> homeworkList) {
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
        HomeworkItem item = homeworkList.get(position);
        holder.textViewContent.setText(item.content);
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

        ViewHolder(View itemView) {
            super(itemView);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            textViewContent = itemView.findViewById(R.id.textViewContent);
        }
    }
}