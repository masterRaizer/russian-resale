package com.russianresale.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ItemUserBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.seller.SellerProfileActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Пользователи площадки: администратор может заблокировать покупателя или продавца. */
public class UsersFragment extends Fragment {
    private static final int F_ALL = 1;
    private static final int F_BUYERS = 2;
    private static final int F_SELLERS = 3;
    private static final int F_BLOCKED = 4;

    private FragmentListBinding b;
    private Adapter adapter;
    private List<User> all = new ArrayList<>();
    private App app;
    private long adminId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        app = App.get(requireContext());
        adminId = app.session().getUserId();
        b.chipsScroll.setVisibility(View.VISIBLE);
        Ui.addChoiceChip(b.chips, "Все", F_ALL).setChecked(true);
        Ui.addChoiceChip(b.chips, "Покупатели", F_BUYERS);
        Ui.addChoiceChip(b.chips, "Продавцы", F_SELLERS);
        Ui.addChoiceChip(b.chips, "Заблокированные", F_BLOCKED);
        b.chips.setOnCheckedStateChangeListener((g, ids) -> applyFilter());

        adapter = new Adapter();
        b.recycler.setPadding(0, 0, 0, 0);
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        b.recycler.setAdapter(adapter);
        app.admin().observeUsers().observe(getViewLifecycleOwner(), list -> {
            all = list == null ? new ArrayList<>() : list;
            applyFilter();
        });
    }

    private void applyFilter() {
        int f = b.chips.getCheckedChipId();
        List<User> result = new ArrayList<>();
        for (User u : all) {
            boolean match = f == F_ALL || (f == F_BUYERS && u.isBuyer()) || (f == F_SELLERS && u.isSeller())
                    || (f == F_BLOCKED && u.blocked);
            if (match) result.add(u);
        }
        adapter.submitList(result);
        Ui.showEmpty(b.empty, result.isEmpty(), R.drawable.ic_group, "Пользователей нет", null);
    }

    private void toggleBlock(User u) {
        boolean block = !u.blocked;
        Ui.confirm(requireContext(), block ? "Заблокировать пользователя?" : "Разблокировать пользователя?",
                block ? u.displayName() + " не сможет войти, " + (u.isSeller() ? "его объявления скроются из каталога" : "покупки станут недоступны")
                        : u.displayName() + " снова получит доступ к площадке",
                block ? "Заблокировать" : "Разблокировать",
                () -> app.admin().setUserBlocked(adminId, u.id, block, Ui.callback(requireContext(), null)));
    }

    private class Adapter extends ListAdapter<User, Holder> {
        Adapter() {
            super(new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User a, @NonNull User b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull User a, @NonNull User b) {
                    return a.blocked == b.blocked && a.displayName().equals(b.displayName());
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            User u = getItem(position);
            ImageLoader.loadAvatar(h.b.avatar, u.avatarPath, u.displayName());
            h.b.name.setText(u.displayName());
            h.b.meta.setText(u.email + " · с " + Format.date(u.createdAt));
            if (u.blocked) Ui.tintLabel(h.b.role, "Заблокирован", R.color.error);
            else Ui.tintLabel(h.b.role, u.roleEnum().title, u.isSeller() ? R.color.secondary : R.color.primary);
            h.b.action.setText(u.blocked ? "Разблокировать" : "Заблокировать");
            h.b.action.setOnClickListener(v -> toggleBlock(u));
            h.itemView.setOnClickListener(v -> {
                if (u.isSeller()) SellerProfileActivity.open(requireContext(), u.id);
            });
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemUserBinding b;

        Holder(ItemUserBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
