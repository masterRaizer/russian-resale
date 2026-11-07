package com.russianresale.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.model.ComplaintItem;
import com.russianresale.app.data.model.ComplaintStatus;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ItemComplaintBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.Objects;

/** Жалобы пользователей на объявления (модерация). */
public class ComplaintsFragment extends Fragment {
    private FragmentListBinding b;
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
        Adapter adapter = new Adapter();
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);
        app.admin().observeComplaints().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            Ui.showEmpty(b.empty, list == null || list.isEmpty(), R.drawable.ic_shield, "Жалоб нет",
                    "Когда пользователи пожалуются на объявление, оно появится здесь");
        });
    }

    private void resolve(ComplaintItem c, boolean block) {
        Ui.confirm(requireContext(), block ? "Заблокировать объявление?" : "Отклонить жалобу?",
                block ? "«" + c.listingTitle + "» пропадёт из каталога. Продавец и автор жалобы получат уведомления."
                        : "Объявление останется в каталоге, автор жалобы получит уведомление.",
                block ? "Заблокировать" : "Отклонить",
                () -> app.admin().resolveComplaint(adminId, c.complaint.id, block, Ui.callback(requireContext(), ok ->
                        Ui.toast(requireContext(), block ? "Объявление заблокировано" : "Жалоба отклонена"))));
    }

    private class Adapter extends ListAdapter<ComplaintItem, Holder> {
        Adapter() {
            super(new DiffUtil.ItemCallback<ComplaintItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ComplaintItem a, @NonNull ComplaintItem b) {
                    return a.complaint.id == b.complaint.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ComplaintItem a, @NonNull ComplaintItem b) {
                    return Objects.equals(a.complaint.status, b.complaint.status)
                            && Objects.equals(a.listingStatus, b.listingStatus);
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemComplaintBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            ComplaintItem c = getItem(position);
            ComplaintStatus status = ComplaintStatus.of(c.complaint.status);
            int color = status == ComplaintStatus.OPEN ? R.color.warning
                    : status == ComplaintStatus.RESOLVED ? R.color.error : R.color.on_surface_variant;
            Ui.tintLabel(h.b.status, status.title, color);
            ImageLoader.load(h.b.photo, ImageLoader.first(c.listingPhoto));
            h.b.reason.setText(c.complaint.reason);
            h.b.listing.setText("«" + c.listingTitle + "» · " + c.sellerName);
            h.b.meta.setText("От: " + c.reporterName + " · " + Format.dateTime(c.complaint.createdAt));
            boolean hasText = c.complaint.text != null && !c.complaint.text.isEmpty();
            h.b.text.setVisibility(hasText ? View.VISIBLE : View.GONE);
            h.b.text.setText(c.complaint.text);
            h.b.buttons.setVisibility(status == ComplaintStatus.OPEN ? View.VISIBLE : View.GONE);
            h.b.block.setOnClickListener(v -> resolve(c, true));
            h.b.reject.setOnClickListener(v -> resolve(c, false));
            h.itemView.setOnClickListener(v -> ListingDetailsActivity.open(requireContext(), c.complaint.listingId));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemComplaintBinding b;

        Holder(ItemComplaintBinding b) {
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
