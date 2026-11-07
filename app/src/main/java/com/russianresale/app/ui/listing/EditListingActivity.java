package com.russianresale.app.ui.listing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.Category;
import com.russianresale.app.data.model.Condition;
import com.russianresale.app.data.model.Gender;
import com.russianresale.app.data.repo.ListingRepository;
import com.russianresale.app.databinding.ActivityEditListingBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.util.PhotoStorage;
import com.russianresale.app.util.Validators;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Создание и редактирование объявления (только для продавца). */
public class EditListingActivity extends BaseActivity {
    private static final String EXTRA_ID = "listing_id";

    private ActivityEditListingBinding b;
    private PhotoThumbAdapter photos;
    private Listing editing;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<PickVisualMediaRequest> picker;

    public static void open(Context context, long listingId) {
        context.startActivity(new Intent(context, EditListingActivity.class).putExtra(EXTRA_ID, listingId));
    }

    public static void create(Context context) {
        context.startActivity(new Intent(context, EditListingActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEditListingBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        long id = getIntent().getLongExtra(EXTRA_ID, 0);
        setupToolbar(b.toolbar, id == 0 ? "Новое объявление" : "Редактирование");

        if (!session().isSeller()) {
            // защита на уровне интерфейса; репозиторий тоже проверяет роль
            toast("Размещать объявления могут только продавцы");
            finish();
            return;
        }

        picker = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(ListingRepository.MAX_PHOTOS), this::onPicked);
        photos = new PhotoThumbAdapter(this::pickPhotos);
        b.photos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        b.photos.setAdapter(photos);

        b.category.setSimpleItems(Category.titles());
        b.gender.setSimpleItems(Gender.titles());
        b.condition.setSimpleItems(Condition.titles());
        b.madeToOrder.setOnCheckedChangeListener((v, checked) ->
                b.productionDaysLayout.setVisibility(checked ? View.VISIBLE : View.GONE));
        b.productionDaysLayout.setVisibility(View.GONE);
        b.save.setOnClickListener(v -> save());

        if (savedInstanceState != null && savedInstanceState.getStringArray("photos") != null) {
            photos.setPhotos(java.util.Arrays.asList(savedInstanceState.getStringArray("photos")));
        }

        if (id == 0) {
            b.save.setText("Опубликовать");
            b.quantity.setText("1");
            setDropdown(b.gender, Gender.WOMEN.title);
            setDropdown(b.condition, Condition.NEW.title);
            app().auth().getUser(userId(), Ui.callback(this, (User u) -> {
                if (Ui.text(b.city).isEmpty() && u.city != null) b.city.setText(u.city);
                if (Ui.text(b.brand).isEmpty() && u.shopName != null) b.brand.setText(u.shopName);
            }));
        } else {
            b.save.setText("Сохранить изменения");
            app().listings().get(id, Ui.callback(this, l -> {
                editing = l;
                if (savedInstanceState == null) fill(l);
            }));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photos != null) outState.putStringArray("photos", photos.getPhotos().toArray(new String[0]));
    }

    private void fill(Listing l) {
        photos.setPhotos(l.photoList());
        b.title.setText(l.title);
        b.description.setText(l.description);
        setDropdown(b.category, l.categoryEnum().title);
        setDropdown(b.gender, l.genderEnum().title);
        setDropdown(b.condition, l.conditionEnum().title);
        b.size.setText(l.size);
        b.brand.setText(l.brand);
        b.color.setText(l.color);
        b.material.setText(l.material);
        b.price.setText(String.valueOf(l.price));
        b.quantity.setText(String.valueOf(l.quantity));
        b.madeToOrder.setChecked(l.madeToOrder);
        if (l.madeToOrder) b.productionDays.setText(String.valueOf(l.productionDays));
        b.city.setText(l.city);
    }

    private static void setDropdown(MaterialAutoCompleteTextView view, String value) {
        view.setText(value, false);
    }

    private static int indexOf(String[] titles, String value) {
        for (int i = 0; i < titles.length; i++) if (titles[i].equals(value)) return i;
        return -1;
    }

    // ---------------------------------------------------------------- фото

    private void pickPhotos() {
        if (photos.remaining() <= 0) {
            toast("Можно добавить не больше " + ListingRepository.MAX_PHOTOS + " фото");
            return;
        }
        picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void onPicked(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;
        int limit = Math.min(uris.size(), photos.remaining());
        b.save.setEnabled(false);
        io.execute(() -> {
            int failed = 0;
            for (int i = 0; i < limit; i++) {
                try {
                    String path = PhotoStorage.save(getApplicationContext(), uris.get(i));
                    runOnUiThread(() -> photos.add(path));
                } catch (Exception e) {
                    failed++;
                }
            }
            int failedCount = failed;
            runOnUiThread(() -> {
                b.save.setEnabled(true);
                if (failedCount > 0) toast("Не удалось загрузить фото: " + failedCount);
                if (uris.size() > limit) toast("Добавлены только первые " + limit + " фото");
            });
        });
    }

    // ---------------------------------------------------------------- сохранение

    private void save() {
        Listing l = editing != null ? editing : new Listing();
        l.title = Ui.text(b.title);
        l.description = Ui.text(b.description);
        int category = indexOf(Category.titles(), Ui.text(b.category));
        int gender = indexOf(Gender.titles(), Ui.text(b.gender));
        int condition = indexOf(Condition.titles(), Ui.text(b.condition));
        if (category < 0) {
            toast("Выберите категорию");
            return;
        }
        if (gender < 0 || condition < 0) {
            toast("Укажите, для кого вещь и её состояние");
            return;
        }
        l.category = Category.values()[category].name();
        l.gender = Gender.values()[gender].name();
        l.condition = Condition.values()[condition].name();
        l.size = Ui.text(b.size);
        l.brand = Ui.text(b.brand);
        l.color = Ui.text(b.color);
        l.material = Ui.text(b.material);
        Integer price = Validators.parseIntOrNull(Ui.text(b.price));
        Integer quantity = Validators.parseIntOrNull(Ui.text(b.quantity));
        l.price = price == null ? 0 : price;
        l.quantity = quantity == null ? 0 : quantity;
        l.madeToOrder = b.madeToOrder.isChecked();
        Integer days = Validators.parseIntOrNull(Ui.text(b.productionDays));
        l.productionDays = days == null ? 0 : days;
        l.city = Ui.text(b.city);
        l.setPhotoList(photos.getPhotos());

        b.save.setEnabled(false);
        boolean isNew = editing == null;
        app().listings().save(userId(), l, new com.russianresale.app.data.repo.Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                toast(isNew ? "Объявление опубликовано" : "Изменения сохранены");
                if (isNew) ListingDetailsActivity.open(EditListingActivity.this, id);
                finish();
            }

            @Override
            public void onError(String message) {
                b.save.setEnabled(true);
                toast(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}
