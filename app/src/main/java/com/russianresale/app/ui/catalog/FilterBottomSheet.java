package com.russianresale.app.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.russianresale.app.data.model.Condition;
import com.russianresale.app.data.model.Gender;
import com.russianresale.app.data.model.ListingFilter;
import com.russianresale.app.databinding.BottomSheetFilterBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.util.Validators;

/** Панель дополнительных фильтров каталога. */
public class FilterBottomSheet extends BottomSheetDialogFragment {
    private static final int TYPE_ALL = 1;
    private static final int TYPE_READY = 2;
    private static final int TYPE_CUSTOM = 3;

    private BottomSheetFilterBinding b;
    private CatalogViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = BottomSheetFilterBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(CatalogViewModel.class);
        ListingFilter f = vm.current();

        for (Gender g : Gender.values()) Ui.addChoiceChip(b.genders, g.title, g.ordinal() + 1);
        for (Condition c : Condition.values()) Ui.addChoiceChip(b.conditions, c.title, c.ordinal() + 1);
        Ui.addChoiceChip(b.types, "Все", TYPE_ALL);
        Ui.addChoiceChip(b.types, "Готовые вещи", TYPE_READY);
        Ui.addChoiceChip(b.types, "Пошив на заказ", TYPE_CUSTOM);

        if (f.gender != null) b.genders.check(f.gender.ordinal() + 1);
        if (f.condition != null) b.conditions.check(f.condition.ordinal() + 1);
        b.types.check(f.madeToOrder == null ? TYPE_ALL : (f.madeToOrder ? TYPE_CUSTOM : TYPE_READY));
        if (f.size != null) b.size.setText(f.size);
        if (f.minPrice != null) b.minPrice.setText(String.valueOf(f.minPrice));
        if (f.maxPrice != null) b.maxPrice.setText(String.valueOf(f.maxPrice));

        b.reset.setOnClickListener(v -> {
            ListingFilter reset = vm.current();
            reset.gender = null;
            reset.condition = null;
            reset.size = null;
            reset.minPrice = null;
            reset.maxPrice = null;
            reset.madeToOrder = null;
            vm.apply(reset);
            dismiss();
        });
        b.apply.setOnClickListener(v -> {
            ListingFilter result = vm.current();
            result.gender = checked(b.genders) == 0 ? null : Gender.values()[checked(b.genders) - 1];
            result.condition = checked(b.conditions) == 0 ? null : Condition.values()[checked(b.conditions) - 1];
            int type = checked(b.types);
            result.madeToOrder = type == TYPE_CUSTOM ? Boolean.TRUE : type == TYPE_READY ? Boolean.FALSE : null;
            String size = Ui.text(b.size);
            result.size = size.isEmpty() ? null : size;
            result.minPrice = Validators.parseIntOrNull(Ui.text(b.minPrice));
            result.maxPrice = Validators.parseIntOrNull(Ui.text(b.maxPrice));
            if (result.minPrice != null && result.maxPrice != null && result.minPrice > result.maxPrice) {
                Ui.toast(requireContext(), "Минимальная цена больше максимальной");
                return;
            }
            vm.apply(result);
            dismiss();
        });
    }

    private static int checked(ChipGroup group) {
        int id = group.getCheckedChipId();
        return id == View.NO_ID ? 0 : id;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
