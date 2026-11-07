package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.russianresale.app.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE id = :id")
    User getById(long id);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> observe(long id);

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE role = :role")
    List<User> getByRole(String role);

    @Query("SELECT COUNT(*) FROM users")
    int count();

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    int countByRole(String role);

    @Query("SELECT * FROM users WHERE role <> 'ADMIN' ORDER BY createdAt DESC")
    LiveData<List<User>> observeAllExceptAdmins();

    @Query("UPDATE users SET blocked = :blocked WHERE id = :id")
    void setBlocked(long id, boolean blocked);
}
