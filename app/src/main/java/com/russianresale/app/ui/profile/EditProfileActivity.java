package com.russianresale.app.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.repo.Callback;
import com.russianresale.app.databinding.ActivityEditProfileBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.util.ImageLoader;
import com.russianresale.app.util.PhotoStorage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends BaseActivity {
    private ActivityEditProfileBinding b;
    private String avatarPath;
    private String displayName = "";
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "Профиль");

        ActivityResultLauncher<PickVisualMediaRequest> picker = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), this::onPicked);
        b.changeAvatar.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        b.save.setOnClickListener(v -> save());

        app().auth().getUser(userId(), Ui.callback(this, (User u) -> {
            displayName = u.displayName();
            avatarPath = u.avatarPath;
            ImageLoader.loadAvatar(b.avatar, avatarPath, displayName);
            b.name.setText(u.fullName);
            b.phone.setText(u.phone);
            b.city.setText(u.city);
            b.sellerSection.setVisibility(u.isSeller() ? View.VISIBLE : View.GONE);
            b.shopName.setText(u.shopName);
            b.shopDescription.setText(u.shopDescription);
        }));
    }

    private void onPicked(Uri uri) {
        if (uri == null) return;
        io.execute(() -> {
            try {
                String path = PhotoStorage.save(getApplicationContext(), uri);
                runOnUiThread(() -> {
                    avatarPath = path;
                    ImageLoader.loadAvatar(b.avatar, avatarPath, displayName);
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("Не удалось загрузить фото"));
            }
        });
    }

    private void save() {
        b.save.setEnabled(false);
        app().auth().updateProfile(userId(), Ui.text(b.name), Ui.text(b.phone), Ui.text(b.city),
                Ui.text(b.shopName), Ui.text(b.shopDescription), avatarPath, new Callback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        session().save(user);
                        toast("Профиль сохранён");
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
