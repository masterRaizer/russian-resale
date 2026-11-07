package com.russianresale.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.russianresale.app.data.model.Role;

/** Пользователь: покупатель, продавец (ателье/магазин) или администратор. */
@Entity(tableName = "users", indices = {@Index(value = "email", unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String email = "";
    @NonNull
    public String passwordHash = "";
    @NonNull
    public String salt = "";
    @NonNull
    public String fullName = "";
    public String phone;
    public String city;
    @NonNull
    public String role = Role.BUYER.name();
    /** Название ателье или магазина (только у продавца). */
    public String shopName;
    public String shopDescription;
    public String avatarPath;
    public boolean blocked;
    public long createdAt;

    public Role roleEnum() {
        return Role.of(role);
    }

    public boolean isSeller() {
        return Role.SELLER.name().equals(role);
    }

    public boolean isBuyer() {
        return Role.BUYER.name().equals(role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.name().equals(role);
    }

    /** Имя, которое видят другие: у продавца — название ателье. */
    public String displayName() {
        if (isSeller() && shopName != null && !shopName.trim().isEmpty()) return shopName;
        return fullName;
    }
}
