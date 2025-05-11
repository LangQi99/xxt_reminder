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
import android.content.Context;
import android.content.SharedPreferences;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EditText editTextAccount;
    private EditText editTextPassword;
    private Button buttonLogin;
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
        buttonLogin = view.findViewById(R.id.button_login);
        recyclerViewHomework = view.findViewById(R.id.recyclerViewHomework);
        recyclerViewHomework.setLayoutManager(new LinearLayoutManager(getContext()));
        homeworkAdapter = new HomeworkAdapter(homeworkItemList);
        recyclerViewHomework.setAdapter(homeworkAdapter);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 自动填充账号和密码
        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String lastAccount = sp.getString("account", "");
        String lastPassword = sp.getString("password", "");
        editTextAccount.setText(lastAccount);
        editTextPassword.setText(lastPassword);

        buttonLogin.setOnClickListener(v -> {
            account = editTextAccount.getText().toString();
            password = editTextPassword.getText().toString();
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 保存账号和密码
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("account", account);
            editor.putString("password", password);
            editor.apply();
            networkManager = new NetworkManager();
            networkManager.loginAndGetHomeworkAsync(account, password, homeworkList -> {
                List<HomeworkInfo> items = new ArrayList<>();
                for (HomeworkInfo hw : homeworkList) {
                    items.add(hw);
                }
                requireActivity().runOnUiThread(() -> {
                    homeworkItemList.clear();
                    homeworkItemList.addAll(items);
                    homeworkAdapter.notifyDataSetChanged();
                    if (!items.isEmpty() && "登录失败".equals(items.get(0).homeworkStatus)) {
                        Toast.makeText(getContext(), "登录失败", Toast.LENGTH_SHORT).show();
                    }
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