package com.russianresale.app.data.repo;

import androidx.lifecycle.LiveData;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.Complaint;
import com.russianresale.app.data.entity.Favorite;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.ListingFilter;
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.util.Validators;

import java.util.List;

public class ListingRepository extends BaseRepository {
    public static final int MAX_PHOTOS = 8;

    public ListingRepository(AppDatabase db) {
        super(db);
    }

    public LiveData<List<ListingItem>> search(ListingFilter filter, long userId) {
        return db.listings().search(filter.toQuery(userId));
    }

    public LiveData<List<ListingItem>> observeFavorites(long userId) {
        return db.listings().observeFavorites(userId);
    }

    public LiveData<List<ListingItem>> observeSellerActive(long sellerId, long viewerId) {
        return db.listings().observeSellerActive(sellerId, viewerId);
    }

    public LiveData<List<Listing>> observeMyListings(long sellerId) {
        return db.listings().observeBySeller(sellerId);
    }

    public LiveData<Listing> observe(long id) {
        return db.listings().observe(id);
    }

    public LiveData<Boolean> observeIsFavorite(long userId, long listingId) {
        return db.listings().observeIsFavorite(userId, listingId);
    }

    public LiveData<Integer> observeFavoritesCount(long listingId) {
        return db.listings().observeFavoritesCount(listingId);
    }

    public void get(long id, Callback<Listing> callback) {
        exec(() -> {
            Listing listing = db.listings().getById(id);
            if (listing == null) throw new AppException("Объявление не найдено или удалено");
            return listing;
        }, callback);
    }

    /** Просмотр объявления: свои просмотры продавцу не засчитываются. */
    public void registerView(long listingId, long viewerId) {
        runInBackground(() -> {
            Listing listing = db.listings().getById(listingId);
            if (listing != null && listing.sellerId != viewerId) db.listings().incrementViews(listingId);
        });
    }

    /** Создание или редактирование объявления. Доступно только продавцу и только для своих объявлений. */
    public void save(long sellerId, Listing data, Callback<Long> callback) {
        exec(() -> {
            User seller = requireSeller(sellerId);
            validate(data);
            long now = System.currentTimeMillis();
            if (data.id == 0) {
                data.sellerId = seller.id;
                data.status = ListingStatus.ACTIVE.name();
                data.createdAt = now;
                data.updatedAt = now;
                if (data.city == null || data.city.trim().isEmpty()) data.city = seller.city;
                data.updateSearchText();
                return db.listings().insert(data);
            }
            Listing existing = db.listings().getById(data.id);
            if (existing == null) throw new AppException("Объявление не найдено");
            if (existing.sellerId != sellerId) throw new AppException("Можно редактировать только свои объявления");
            if (existing.statusEnum() == ListingStatus.BLOCKED) {
                throw new AppException("Объявление заблокировано модератором и не может быть изменено");
            }
            data.sellerId = existing.sellerId;
            data.createdAt = existing.createdAt;
            data.views = existing.views;
            data.updatedAt = now;
            // если вещь снова появилась в наличии — объявление опять активно
            if (existing.statusEnum() == ListingStatus.SOLD && data.quantity > 0) {
                data.status = ListingStatus.ACTIVE.name();
            } else {
                data.status = existing.status;
            }
            data.updateSearchText();
            db.listings().update(data);
            return data.id;
        }, callback);
    }

    private void validate(Listing l) {
        if (l.title == null || l.title.trim().length() < 3) throw new AppException("Название — минимум 3 символа");
        if (l.title.trim().length() > 80) throw new AppException("Название — не длиннее 80 символов");
        if (l.description == null || l.description.trim().length() < 10) {
            throw new AppException("Опишите вещь подробнее (минимум 10 символов)");
        }
        if (!Validators.isValidPrice(l.price)) {
            throw new AppException("Цена — от " + Validators.MIN_PRICE + " до " + Validators.MAX_PRICE + " ₽");
        }
        if (l.quantity < 1 || l.quantity > 999) throw new AppException("Количество — от 1 до 999");
        if (l.madeToOrder && (l.productionDays < 1 || l.productionDays > 90)) {
            throw new AppException("Срок пошива — от 1 до 90 дней");
        }
        if (!l.madeToOrder) l.productionDays = 0;
        if (l.photoList().isEmpty()) throw new AppException("Добавьте хотя бы одно фото");
        if (l.photoList().size() > MAX_PHOTOS) throw new AppException("Не больше " + MAX_PHOTOS + " фото");
        if (l.size == null || l.size.trim().isEmpty()) throw new AppException("Укажите размер (или «Без размера»)");
        l.title = l.title.trim();
        l.description = l.description.trim();
    }

    /** Снять с продажи (в архив) или вернуть в продажу. */
    public void setArchived(long sellerId, long listingId, boolean archived, Callback<Boolean> callback) {
        exec(() -> {
            requireSeller(sellerId);
            Listing l = requireOwn(sellerId, listingId);
            if (l.statusEnum() == ListingStatus.BLOCKED) throw new AppException("Объявление заблокировано модератором");
            if (!archived && l.quantity < 1) throw new AppException("Сначала укажите количество в наличии");
            db.listings().setStatus(listingId, archived ? ListingStatus.ARCHIVED.name() : ListingStatus.ACTIVE.name(),
                    System.currentTimeMillis());
            return true;
        }, callback);
    }

    public void delete(long sellerId, long listingId, Callback<Boolean> callback) {
        exec(() -> {
            requireSeller(sellerId);
            requireOwn(sellerId, listingId);
            db.listings().delete(listingId);
            return true;
        }, callback);
    }

    private Listing requireOwn(long sellerId, long listingId) {
        Listing l = db.listings().getById(listingId);
        if (l == null) throw new AppException("Объявление не найдено");
        if (l.sellerId != sellerId) throw new AppException("Это не ваше объявление");
        return l;
    }

    /** Избранное есть только у покупателей. Возвращает новое состояние. */
    public void toggleFavorite(long userId, long listingId, Callback<Boolean> callback) {
        exec(() -> {
            requireBuyer(userId);
            if (db.favorites().exists(userId, listingId)) {
                db.favorites().delete(userId, listingId);
                return false;
            }
            db.favorites().insert(new Favorite(userId, listingId));
            return true;
        }, callback);
    }

    public void complain(long userId, long listingId, String reason, String text, Callback<Boolean> callback) {
        exec(() -> {
            User user = requireUser(userId);
            Listing l = db.listings().getById(listingId);
            if (l == null) throw new AppException("Объявление не найдено");
            if (l.sellerId == user.id) throw new AppException("Нельзя пожаловаться на своё объявление");
            if (db.complaints().hasOpen(listingId, userId)) {
                throw new AppException("Вы уже отправили жалобу, она на рассмотрении");
            }
            Complaint c = new Complaint();
            c.listingId = listingId;
            c.reporterId = userId;
            c.reason = reason;
            c.text = text;
            c.createdAt = System.currentTimeMillis();
            long id = db.complaints().insert(c);
            for (User admin : adminsList()) {
                notify(admin.id, AppNotification.TYPE_LISTING, listingId, "Новая жалоба",
                        reason + ": «" + l.title + "»");
            }
            return id > 0;
        }, callback);
    }

    private List<User> adminsList() {
        return db.users().getByRole("ADMIN");
    }
}
