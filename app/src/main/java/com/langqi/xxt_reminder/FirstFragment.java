package com.langqi.xxt_reminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.langqi.xxt_reminder.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EditText editTextAccount;
    private EditText editTextPassword;
    private Button buttonViewHomework;
    private xxt xxtInstance;
    private String account;
    private String password;
    private TextView textViewHomework;
    private RecyclerView recyclerViewHomework;
    private HomeworkAdapter homeworkAdapter;
    private List<HomeworkItem> homeworkItemList = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        editTextAccount = view.findViewById(R.id.editTextAccount);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonViewHomework = view.findViewById(R.id.button_view_homework);
        recyclerViewHomework = view.findViewById(R.id.recyclerViewHomework);
        recyclerViewHomework.setLayoutManager(new LinearLayoutManager(getContext()));
        homeworkAdapter = new HomeworkAdapter(homeworkItemList);
        recyclerViewHomework.setAdapter(homeworkAdapter);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonViewHomework.setOnClickListener(v -> {
            account = editTextAccount.getText().toString();
            password = editTextPassword.getText().toString();
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            xxtInstance = new xxt(account, password);
            xxtInstance.loginAsync(success -> {
                if (!success) {
                    requireActivity()
                            .runOnUiThread(() -> Toast.makeText(getContext(), "登录失败", Toast.LENGTH_SHORT).show());
                    return;
                }
                xxtInstance.getAllHomeworkSimpleAsync(homeworkList -> {
                    List<HomeworkItem> items = new ArrayList<>();
                    for (xxt.HomeworkInfo hw : homeworkList) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("科目: ").append(hw.subject == null ? "" : hw.subject).append("\n");
                        sb.append("作业名: ").append(hw.homeworkName == null ? "" : hw.homeworkName).append("\n");
                        sb.append("状态: ").append(hw.homeworkStatus == null ? "" : hw.homeworkStatus).append("\n");
                        if (hw.deadline != null && !hw.deadline.isEmpty()) {
                            sb.append("截止时间: ").append(hw.deadline).append("\n");
                        }
                        if (hw.url != null && !hw.url.isEmpty()) {
                            sb.append("链接: ").append(hw.url).append("\n");
                        }
                        if (hw.taskrefId != null && !hw.taskrefId.isEmpty()) {
                            sb.append("taskrefId: ").append(hw.taskrefId).append("\n");
                        }
                        boolean submitted = hw.homeworkStatus != null && !"未提交".equals(hw.homeworkStatus);
                        items.add(new HomeworkItem(sb.toString(), submitted));
                    }
                    requireActivity().runOnUiThread(() -> {
                        homeworkItemList.clear();
                        homeworkItemList.addAll(items);
                        homeworkAdapter.notifyDataSetChanged();
                    });
                });
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}