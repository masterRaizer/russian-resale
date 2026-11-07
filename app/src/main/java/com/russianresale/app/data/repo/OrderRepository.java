package com.russianresale.app.data.repo;

import androidx.lifecycle.LiveData;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.CartItem;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.Order;
import com.russianresale.app.data.entity.OrderItem;
import com.russianresale.app.data.entity.Review;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.CartLine;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.data.model.OrderCard;
import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.data.model.PaymentMethod;
import com.russianresale.app.data.model.RatingSummary;
import com.russianresale.app.data.model.ReviewItem;
import com.russianresale.app.data.model.SellerStats;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.OrderRules;
import com.russianresale.app.util.PriceCalculator;
import com.russianresale.app.util.Validators;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Корзина, заказы и отзывы. */
public class OrderRepository extends BaseRepository {

    public OrderRepository(AppDatabase db) {
        super(db);
    }

    // ---------------------------------------------------------------- корзина

    public LiveData<List<CartLine>> observeCart(long userId) {
        return db.cart().observeLines(userId);
    }

    public LiveData<Integer> observeCartCount(long userId) {
        return db.cart().observeCount(userId);
    }

    public LiveData<Boolean> observeInCart(long userId, long listingId) {
        return db.cart().observeInCart(userId, listingId);
    }

    public void addToCart(long buyerId, long listingId, Callback<Boolean> callback) {
        exec(() -> {
            requireBuyer(buyerId);
            Listing l = requireAvailable(listingId);
            CartItem existing = db.cart().find(buyerId, listingId);
            int quantity = existing == null ? 1 : existing.quantity + 1;
            if (quantity > l.quantity) throw new AppException("В наличии только " + l.quantity + " шт.");
            db.cart().upsert(new CartItem(buyerId, listingId, quantity));
            return true;
        }, callback);
    }

    public void setCartQuantity(long buyerId, long listingId, int quantity, Callback<Boolean> callback) {
        exec(() -> {
            requireBuyer(buyerId);
            if (quantity <= 0) {
                db.cart().delete(buyerId, listingId);
                return true;
            }
            Listing l = db.listings().getById(listingId);
            if (l != null && quantity > l.quantity) throw new AppException("В наличии только " + l.quantity + " шт.");
            db.cart().setQuantity(buyerId, listingId, quantity);
            return true;
        }, callback);
    }

    public void removeFromCart(long buyerId, long listingId) {
        exec(() -> {
            db.cart().delete(buyerId, listingId);
            return true;
        }, null);
    }

    private Listing requireAvailable(long listingId) {
        Listing l = db.listings().getById(listingId);
        if (l == null) throw new AppException("Объявление не найдено или удалено");
        if (!l.isActive()) throw new AppException("Вещь уже недоступна для покупки");
        if (l.quantity < 1) throw new AppException("Вещь закончилась");
        User seller = db.users().getById(l.sellerId);
        if (seller == null || seller.blocked) throw new AppException("Продавец недоступен");
        return l;
    }

    // ---------------------------------------------------------------- оформление

    /** Оформляет заказ(ы). Товары разных продавцов попадают в разные заказы. Возвращает id заказов. */
    public void checkout(long buyerId, CheckoutRequest req, Callback<List<Long>> callback) {
        exec(() -> {
            User buyer = requireBuyer(buyerId);
            if (!Validators.isValidPhone(req.phone)) throw new AppException("Укажите телефон для связи");
            if (req.delivery.needsAddress && !Validators.isNotBlank(req.address)) {
                throw new AppException("Укажите адрес доставки");
            }

            // позиции: одна вещь («Купить сейчас») или вся корзина
            List<CartLine> lines = new ArrayList<>();
            if (req.buyNowListingId != null) {
                Listing l = requireAvailable(req.buyNowListingId);
                CartLine line = new CartLine();
                line.listingId = l.id;
                line.quantity = Math.max(1, req.buyNowQuantity);
                lines.add(line);
            } else {
                lines.addAll(db.cart().getLines(buyerId));
            }
            if (lines.isEmpty()) throw new AppException("Корзина пуста");

            Map<Long, List<OrderItem>> bySeller = new LinkedHashMap<>();
            Map<Long, Listing> listings = new LinkedHashMap<>();
            boolean needMeasurements = false;
            for (CartLine line : lines) {
                Listing l = requireAvailable(line.listingId);
                if (l.sellerId == buyer.id) throw new AppException("Нельзя купить свою вещь");
                if (line.quantity > l.quantity) {
                    throw new AppException("«" + l.title + "»: в наличии только " + l.quantity + " шт.");
                }
                if (l.madeToOrder) needMeasurements = true;
                OrderItem item = new OrderItem();
                item.listingId = l.id;
                item.title = l.title;
                item.photo = l.mainPhoto();
                item.size = l.size;
                item.price = l.price;
                item.quantity = line.quantity;
                item.madeToOrder = l.madeToOrder;
                List<OrderItem> group = bySeller.get(l.sellerId);
                if (group == null) {
                    group = new ArrayList<>();
                    bySeller.put(l.sellerId, group);
                }
                group.add(item);
                listings.put(l.id, l);
            }
            if (needMeasurements && !Validators.isNotBlank(req.measurements)) {
                throw new AppException("Для вещей на заказ укажите мерки: рост, обхват груди, талии и бёдер");
            }

            long now = System.currentTimeMillis();
            List<Long> ids = new ArrayList<>();
            for (Map.Entry<Long, List<OrderItem>> entry : bySeller.entrySet()) {
                int itemsTotal = 0;
                for (OrderItem item : entry.getValue()) itemsTotal += item.price * item.quantity;

                Order order = new Order();
                order.buyerId = buyerId;
                order.sellerId = entry.getKey();
                order.status = OrderStatus.NEW.name();
                order.deliveryMethod = req.delivery.name();
                order.deliveryCost = PriceCalculator.deliveryCost(req.delivery, itemsTotal);
                order.itemsTotal = itemsTotal;
                order.total = itemsTotal + order.deliveryCost;
                order.address = req.delivery.needsAddress ? req.address.trim() : null;
                order.phone = req.phone.trim();
                order.comment = req.comment;
                order.measurements = req.measurements;
                order.paymentMethod = req.payment.name();
                // онлайн-оплата в учебном проекте имитируется и всегда проходит успешно
                order.paid = req.payment != PaymentMethod.CASH;
                order.createdAt = now;
                order.updatedAt = now;
                order.id = db.orders().insert(order);
                for (OrderItem item : entry.getValue()) item.orderId = order.id;
                db.orders().insertItems(entry.getValue());

                // списываем остатки
                for (OrderItem item : entry.getValue()) {
                    Listing l = listings.get(item.listingId);
                    l.quantity -= item.quantity;
                    if (l.quantity <= 0) {
                        l.quantity = 0;
                        l.status = ListingStatus.SOLD.name();
                    }
                    l.updatedAt = now;
                    db.listings().update(l);
                }

                ids.add(order.id);
                notify(order.sellerId, AppNotification.TYPE_ORDER, order.id,
                        "Новый заказ " + Format.orderNumber(order.id),
                        buyer.fullName + " оформил(а) заказ на " + Format.price(order.total) + ". Подтвердите его");
                notify(buyerId, AppNotification.TYPE_ORDER, order.id,
                        "Заказ " + Format.orderNumber(order.id) + " оформлен",
                        "Продавец получил заказ и скоро его подтвердит");
            }
            if (req.buyNowListingId == null) db.cart().clear(buyerId);
            else db.cart().delete(buyerId, req.buyNowListingId);
            return ids;
        }, callback);
    }

    /** Предварительный расчёт для экрана оформления: позиции «Купить сейчас» или корзины. */
    public void previewLines(long buyerId, Long buyNowListingId, Callback<List<CartLine>> callback) {
        exec(() -> {
            requireBuyer(buyerId);
            if (buyNowListingId == null) return db.cart().getLines(buyerId);
            Listing l = requireAvailable(buyNowListingId);
            User seller = db.users().getById(l.sellerId);
            CartLine line = new CartLine();
            line.listingId = l.id;
            line.quantity = 1;
            line.title = l.title;
            line.price = l.price;
            line.photos = l.photos;
            line.size = l.size;
            line.category = l.category;
            line.stock = l.quantity;
            line.status = l.status;
            line.sellerId = l.sellerId;
            line.madeToOrder = l.madeToOrder;
            line.sellerName = seller == null ? "" : seller.displayName();
            List<CartLine> list = new ArrayList<>();
            list.add(line);
            return list;
        }, callback);
    }

    // ---------------------------------------------------------------- заказы

    public LiveData<List<OrderCard>> observeBuyerOrders(long buyerId) {
        return db.orders().observeByBuyer(buyerId);
    }

    public LiveData<List<OrderCard>> observeSellerOrders(long sellerId) {
        return db.orders().observeBySeller(sellerId);
    }

    public LiveData<Integer> observeNewOrdersForSeller(long sellerId) {
        return db.orders().observeNewCountForSeller(sellerId);
    }

    public LiveData<OrderCard> observeOrder(long orderId) {
        return db.orders().observeCard(orderId);
    }

    public LiveData<List<OrderItem>> observeItems(long orderId) {
        return db.orders().observeItems(orderId);
    }

    public void accept(long sellerId, long orderId, Callback<Boolean> callback) {
        exec(() -> {
            Order o = requireSellerOrder(sellerId, orderId);
            if (!OrderRules.sellerCanAccept(o.statusEnum())) throw new AppException("Заказ уже обработан");
            boolean custom = false;
            for (OrderItem item : db.orders().getItems(orderId)) custom |= item.madeToOrder;
            setStatus(o, OrderStatus.ACCEPTED);
            notify(o.buyerId, AppNotification.TYPE_ORDER, o.id, "Заказ " + Format.orderNumber(o.id) + " принят",
                    custom ? "Ателье приступило к пошиву по вашим меркам" : "Продавец готовит заказ к отправке");
            return true;
        }, callback);
    }

    public void ship(long sellerId, long orderId, String trackNumber, Callback<Boolean> callback) {
        exec(() -> {
            Order o = requireSellerOrder(sellerId, orderId);
            if (!OrderRules.sellerCanShip(o.statusEnum())) throw new AppException("Сначала примите заказ");
            if (o.deliveryEnum().needsAddress && !Validators.isNotBlank(trackNumber)) {
                throw new AppException("Укажите трек-номер отправления");
            }
            o.trackNumber = trackNumber == null ? null : trackNumber.trim();
            setStatus(o, OrderStatus.SHIPPED);
            String text = o.deliveryEnum().needsAddress
                    ? "Трек-номер: " + o.trackNumber + ". Подтвердите получение, когда заберёте заказ"
                    : "Заказ готов к выдаче. Подтвердите получение, когда заберёте его";
            notify(o.buyerId, AppNotification.TYPE_ORDER, o.id, "Заказ " + Format.orderNumber(o.id) + " отправлен", text);
            return true;
        }, callback);
    }

    public void cancelBySeller(long sellerId, long orderId, String reason, Callback<Boolean> callback) {
        exec(() -> {
            Order o = requireSellerOrder(sellerId, orderId);
            if (!OrderRules.sellerCanCancel(o.statusEnum())) throw new AppException("Этот заказ уже нельзя отменить");
            if (!Validators.isNotBlank(reason)) throw new AppException("Укажите причину отмены");
            cancel(o, reason.trim());
            notify(o.buyerId, AppNotification.TYPE_ORDER, o.id, "Заказ " + Format.orderNumber(o.id) + " отменён продавцом",
                    "Причина: " + reason.trim() + (o.paid ? ". Деньги вернутся на карту" : ""));
            return true;
        }, callback);
    }

    public void cancelByBuyer(long buyerId, long orderId, String reason, Callback<Boolean> callback) {
        exec(() -> {
            Order o = requireBuyerOrder(buyerId, orderId);
            if (!OrderRules.buyerCanCancel(o.statusEnum())) {
                throw new AppException("Продавец уже принял заказ — отменить его можно только через продавца");
            }
            cancel(o, Validators.isNotBlank(reason) ? reason.trim() : "Покупатель передумал");
            notify(o.sellerId, AppNotification.TYPE_ORDER, o.id, "Заказ " + Format.orderNumber(o.id) + " отменён",
                    "Покупатель отменил заказ");
            return true;
        }, callback);
    }

    public void confirmReceived(long buyerId, long orderId, Callback<Boolean> callback) {
        exec(() -> {
            Order o = requireBuyerOrder(buyerId, orderId);
            if (!OrderRules.buyerCanConfirm(o.statusEnum())) throw new AppException("Заказ ещё не отправлен");
            o.paid = true;
            setStatus(o, OrderStatus.DELIVERED);
            notify(o.sellerId, AppNotification.TYPE_ORDER, o.id, "Заказ " + Format.orderNumber(o.id) + " получен",
                    "Покупатель подтвердил получение. Сделка завершена");
            notify(o.buyerId, AppNotification.TYPE_ORDER, o.id, "Оцените покупку",
                    "Расскажите, как вам вещи из заказа " + Format.orderNumber(o.id));
            return true;
        }, callback);
    }

    private void cancel(Order o, String reason) {
        long now = System.currentTimeMillis();
        for (OrderItem item : db.orders().getItems(o.id)) {
            Listing l = db.listings().getById(item.listingId);
            if (l == null) continue;
            l.quantity += item.quantity;
            if (l.statusEnum() == ListingStatus.SOLD) l.status = ListingStatus.ACTIVE.name();
            l.updatedAt = now;
            db.listings().update(l);
        }
        o.cancelReason = reason;
        setStatus(o, OrderStatus.CANCELLED);
    }

    private void setStatus(Order o, OrderStatus status) {
        o.status = status.name();
        o.updatedAt = System.currentTimeMillis();
        db.orders().update(o);
    }

    private Order requireSellerOrder(long sellerId, long orderId) {
        requireSeller(sellerId);
        Order o = db.orders().getById(orderId);
        if (o == null || o.sellerId != sellerId) throw new AppException("Заказ не найден");
        return o;
    }

    private Order requireBuyerOrder(long buyerId, long orderId) {
        requireBuyer(buyerId);
        Order o = db.orders().getById(orderId);
        if (o == null || o.buyerId != buyerId) throw new AppException("Заказ не найден");
        return o;
    }

    // ---------------------------------------------------------------- отзывы

    public LiveData<List<ReviewItem>> observeSellerReviews(long sellerId) {
        return db.reviews().observeBySeller(sellerId);
    }

    public LiveData<List<ReviewItem>> observeListingReviews(long listingId) {
        return db.reviews().observeByListing(listingId);
    }

    public LiveData<List<ReviewItem>> observeBuyerReviews(long buyerId) {
        return db.reviews().observeByBuyer(buyerId);
    }

    public LiveData<RatingSummary> observeSellerRating(long sellerId) {
        return db.reviews().observeSellerRating(sellerId);
    }

    /** Отзыв можно оставить только на позицию полученного заказа и только один раз. */
    public void addReview(long buyerId, long orderItemId, int rating, String text, Callback<Boolean> callback) {
        exec(() -> {
            User buyer = requireBuyer(buyerId);
            OrderItem item = db.orders().getItem(orderItemId);
            if (item == null) throw new AppException("Позиция заказа не найдена");
            Order o = db.orders().getById(item.orderId);
            if (o == null || o.buyerId != buyerId) throw new AppException("Можно оценивать только свои покупки");
            if (!OrderRules.buyerCanReview(o.statusEnum())) {
                throw new AppException("Отзыв можно оставить после получения заказа");
            }
            if (item.reviewed || db.reviews().findByOrderItem(orderItemId) != null) {
                throw new AppException("Вы уже оставили отзыв на эту вещь");
            }
            if (!Validators.isValidRating(rating)) throw new AppException("Поставьте оценку от 1 до 5");
            if (text == null || text.trim().length() < 5) throw new AppException("Напишите хотя бы пару слов (от 5 символов)");
            Review r = new Review();
            r.orderItemId = orderItemId;
            r.orderId = o.id;
            r.listingId = item.listingId;
            r.sellerId = o.sellerId;
            r.buyerId = buyerId;
            r.rating = rating;
            r.text = text.trim();
            r.createdAt = System.currentTimeMillis();
            db.reviews().insert(r);
            item.reviewed = true;
            db.orders().updateItem(item);
            notify(o.sellerId, AppNotification.TYPE_REVIEW, o.sellerId, "Новый отзыв: " + rating + " из 5",
                    buyer.fullName + " о «" + item.title + "»: " + r.text);
            return true;
        }, callback);
    }

    public void replyToReview(long sellerId, long reviewId, String reply, Callback<Boolean> callback) {
        exec(() -> {
            User seller = requireSeller(sellerId);
            Review r = db.reviews().getById(reviewId);
            if (r == null || r.sellerId != sellerId) throw new AppException("Отзыв не найден");
            if (reply == null || reply.trim().length() < 2) throw new AppException("Введите текст ответа");
            r.sellerReply = reply.trim();
            r.replyAt = System.currentTimeMillis();
            db.reviews().update(r);
            notify(r.buyerId, AppNotification.TYPE_REVIEW, r.sellerId, "Продавец ответил на ваш отзыв",
                    seller.displayName() + ": " + r.sellerReply);
            return true;
        }, callback);
    }

    // ---------------------------------------------------------------- статистика продавца

    public void sellerStats(long sellerId, Callback<SellerStats> callback) {
        exec(() -> {
            SellerStats s = new SellerStats();
            s.activeListings = db.listings().countActiveBySeller(sellerId);
            s.soldItems = db.orders().countSoldItems(sellerId);
            s.newOrders = db.orders().countNewForSeller(sellerId);
            s.ordersInProgress = db.orders().countInProgressForSeller(sellerId);
            s.revenue = db.orders().revenue(sellerId);
            RatingSummary rating = db.reviews().sellerRating(sellerId);
            s.rating = rating == null ? 0 : rating.average;
            s.reviewsCount = rating == null ? 0 : rating.count;
            return s;
        }, callback);
    }
}
