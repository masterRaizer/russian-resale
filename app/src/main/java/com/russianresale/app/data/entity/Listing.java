package com.russianresale.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.russianresale.app.data.model.Category;
import com.russianresale.app.data.model.Condition;
import com.russianresale.app.data.model.Gender;
import com.russianresale.app.data.model.ListingStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Объявление продавца: вещь или аксессуар (готовое изделие или пошив на заказ). */
@Entity(tableName = "listings",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "sellerId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("sellerId"), @Index("status"), @Index("category")})
public class Listing {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long sellerId;
    @NonNull
    public String title = "";
    public String description;
    @NonNull
    public String category = Category.TOPS.name();
    @NonNull
    public String gender = Gender.UNISEX.name();
    public String size;
    public String brand;
    public String color;
    public String material;
    @NonNull
    public String condition = Condition.NEW.name();
    /** Цена в рублях. */
    public int price;
    /** Сколько штук в наличии. */
    public int quantity = 1;
    /** Пошив на заказ: вещь шьётся после оформления заказа по меркам покупателя. */
    public boolean madeToOrder;
    public int productionDays;
    public String city;
    @NonNull
    public String status = ListingStatus.ACTIVE.name();
    /** Фото через перевод строки: путь к файлу или "demo:КАТЕГОРИЯ:#цвет" для демо-данных. */
    public String photos;
    public int views;
    /** Текст для поиска в нижнем регистре: название, бренд, цвет, материал, описание. */
    public String searchText;
    public long createdAt;
    public long updatedAt;

    public void updateSearchText() {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{title, brand, color, material, size, description,
                categoryEnum().title, genderEnum().title}) {
            if (part != null) sb.append(part).append(' ');
        }
        searchText = sb.toString().toLowerCase();
    }

    public List<String> photoList() {
        if (photos == null || photos.trim().isEmpty()) return new ArrayList<>();
        List<String> list = new ArrayList<>(Arrays.asList(photos.split("\n")));
        list.removeIf(s -> s.trim().isEmpty());
        return list;
    }

    public String mainPhoto() {
        List<String> list = photoList();
        return list.isEmpty() ? null : list.get(0);
    }

    public void setPhotoList(List<String> list) {
        photos = list == null ? "" : String.join("\n", list);
    }

    public Category categoryEnum() {
        return Category.of(category);
    }

    public Gender genderEnum() {
        return Gender.of(gender);
    }

    public Condition conditionEnum() {
        return Condition.of(condition);
    }

    public ListingStatus statusEnum() {
        return ListingStatus.of(status);
    }

    public boolean isActive() {
        return ListingStatus.ACTIVE.name().equals(status);
    }
}
