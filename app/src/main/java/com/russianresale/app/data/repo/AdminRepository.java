package com.russianresale.app.data.repo;

import androidx.lifecycle.LiveData;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.Complaint;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.AdminStats;
import com.russianresale.app.data.model.ComplaintItem;
import com.russianresale.app.data.model.ComplaintStatus;
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.data.model.Role;

import java.util.List;

/** Модерация: жалобы, блокировка объявлений и пользователей, статистика площадки. */
public class AdminRepository extends BaseRepository {

    public AdminRepository(AppDatabase db) {
        super(db);
    }

    public LiveData<List<ComplaintItem>> observeComplaints() {
        return db.complaints().observeAll();
    }

    public LiveData<List<User>> observeUsers() {
        return db.users().observeAllExceptAdmins();
    }

    public LiveData<List<ListingItem>> observeListings() {
        return db.listings().observeAllForAdmin();
    }

    /** Решение по жалобе: block = true — объявление блокируется, иначе жалоба отклоняется. */
    public void resolveComplaint(long adminId, long complaintId, boolean block, Callback<Boolean> callback) {
        exec(() -> {
            requireAdmin(adminId);
            Complaint c = db.complaints().getById(complaintId);
            if (c == null) throw new AppException("Жалоба не найдена");
            if (ComplaintStatus.of(c.status) != ComplaintStatus.OPEN) throw new AppException("Жалоба уже рассмотрена");
            Listing l = db.listings().getById(c.listingId);
            if (block && l != null) {
                blockListing(l, c.reason);
                db.complaints().closeAllForListing(l.id, ComplaintStatus.RESOLVED.name());
            } else {
                c.status = ComplaintStatus.REJECTED.name();
                db.complaints().update(c);
            }
            notify(c.reporterId, AppNotification.TYPE_SYSTEM, c.listingId, "Жалоба рассмотрена",
                    block ? "Спасибо! Объявление заблокировано" : "Нарушений не найдено");
            return true;
        }, callback);
    }

    public void setListingBlocked(long adminId, long listingId, boolean blocked, Callback<Boolean> callback) {
        exec(() -> {
            requireAdmin(adminId);
            Listing l = db.listings().getById(listingId);
            if (l == null) throw new AppException("Объявление не найдено");
            if (blocked) {
                blockListing(l, "Нарушение правил площадки");
            } else {
                String status = l.quantity > 0 ? ListingStatus.ACTIVE.name() : ListingStatus.SOLD.name();
                db.listings().setStatus(l.id, status, System.currentTimeMillis());
                notify(l.sellerId, AppNotification.TYPE_LISTING, l.id, "Объявление разблокировано",
                        "«" + l.title + "» снова доступно покупателям");
            }
            return true;
        }, callback);
    }

    private void blockListing(Listing l, String reason) {
        db.listings().setStatus(l.id, ListingStatus.BLOCKED.name(), System.currentTimeMillis());
        notify(l.sellerId, AppNotification.TYPE_LISTING, l.id, "Объявление заблокировано",
                "«" + l.title + "» скрыто модератором. Причина: " + reason);
    }

    public void setUserBlocked(long adminId, long userId, boolean blocked, Callback<Boolean> callback) {
        exec(() -> {
            requireAdmin(adminId);
            User u = db.users().getById(userId);
            if (u == null) throw new AppException("Пользователь не найден");
            if (u.isAdmin()) throw new AppException("Нельзя заблокировать администратора");
            db.users().setBlocked(userId, blocked);
            return true;
        }, callback);
    }

    public void stats(Callback<AdminStats> callback) {
        exec(() -> {
            AdminStats s = new AdminStats();
            s.buyers = db.users().countByRole(Role.BUYER.name());
            s.sellers = db.users().countByRole(Role.SELLER.name());
            s.activeListings = db.listings().countActive();
            s.orders = db.orders().countAll();
            s.turnover = db.orders().turnover();
            s.openComplaints = db.complaints().countOpen();
            return s;
        }, callback);
    }
}
