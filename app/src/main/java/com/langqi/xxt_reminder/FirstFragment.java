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
import com.langqi.xxt_reminder.model.HomeworkInfo;
import com.langqi.xxt_reminder.network.NetworkManager;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EditText editTextAccount;
    private EditText editTextPassword;
    private Button buttonViewHomework;
    private NetworkManager networkManager;
    private String account;
    private String password;
    private TextView textViewHomework;
    private RecyclerView recyclerViewHomework;
    private HomeworkAdapter homeworkAdapter;
    private List<HomeworkInfo> homeworkItemList = new ArrayList<>();

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
            networkManager = new NetworkManager();
            networkManager.loginAsync(account, password, success -> {
                if (!success) {
                    requireActivity()
                            .runOnUiThread(() -> Toast.makeText(getContext(), "登录失败", Toast.LENGTH_SHORT).show());
                    return;
                }
                networkManager.getAllHomeworkSimpleAsync(homeworkList -> {
                    List<HomeworkInfo> items = new ArrayList<>();
                    for (HomeworkInfo hw : homeworkList) {
                        items.add(hw);
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