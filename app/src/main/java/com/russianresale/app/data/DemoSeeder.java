package com.russianresale.app.data;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.CartItem;
import com.russianresale.app.data.entity.Chat;
import com.russianresale.app.data.entity.Complaint;
import com.russianresale.app.data.entity.Favorite;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.Message;
import com.russianresale.app.data.entity.Order;
import com.russianresale.app.data.entity.OrderItem;
import com.russianresale.app.data.entity.Review;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.Category;
import com.russianresale.app.data.model.ComplaintStatus;
import com.russianresale.app.data.model.Condition;
import com.russianresale.app.data.model.DeliveryMethod;
import com.russianresale.app.data.model.Gender;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.data.model.PaymentMethod;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.data.repo.BaseRepository;
import com.russianresale.app.util.ImageLoader;
import com.russianresale.app.util.PasswordHasher;
import com.russianresale.app.util.PriceCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Демо-данные: аккаунты всех ролей, объявления трёх продавцов, заказы в разных статусах,
 * отзывы, переписка, избранное, корзина и жалобы. Пароль у всех демо-аккаунтов — 123456.
 */
public class DemoSeeder extends BaseRepository {
    public static final String PASSWORD = "123456";
    private static final long DAY = 24L * 60 * 60 * 1000;
    private long now;

    public DemoSeeder(AppDatabase db) {
        super(db);
    }

    public void seedIfEmpty() {
        exec(() -> {
            if (db.users().count() > 0) return false;
            seed();
            return true;
        }, null);
    }

    private void seed() {
        now = System.currentTimeMillis();

        // ------------------------------------------------------------ пользователи
        User admin = user("admin@resale.ru", "Администратор площадки", "+79000000000", "Москва", Role.ADMIN, null, null, 120);
        User anna = user("buyer@resale.ru", "Анна Смирнова", "+79161234567", "Москва", Role.BUYER, null, null, 60);
        User dmitry = user("buyer2@resale.ru", "Дмитрий Орлов", "+79217654321", "Санкт-Петербург", Role.BUYER, null, null, 45);
        User nit = user("seller@resale.ru", "Мария Ковалёва", "+79031112233", "Москва", Role.SELLER,
                "Ателье «Нить»",
                "Шьём платья, костюмы и верхнюю одежду по вашим меркам. Натуральные ткани, ручная отделка, "
                        + "две примерки. Работаем с 2012 года.", 300);
        User vintage = user("seller2@resale.ru", "Игорь Белов", "+79112223344", "Санкт-Петербург", Role.SELLER,
                "Vintage Room",
                "Винтаж и ресейл из Европы: денима, кожи и культовых брендов 80–00-х. Каждая вещь проходит "
                        + "чистку и проверку перед продажей.", 200);
        User busina = user("seller3@resale.ru", "Елена Ахмедова", "+79053334455", "Казань", Role.SELLER,
                "Мастерская «Бусина»",
                "Украшения из серебра и натуральных камней, вязаные вещи и аксессуары ручной работы. "
                        + "Беру заказы по эскизам.", 150);

        // ------------------------------------------------------------ объявления «Нить»
        Listing dress = listing(nit, "Платье-миди из льна на заказ", Category.DRESSES, Gender.WOMEN,
                "40–56 (по меркам)", "Ателье «Нить»", "Молочный", "Лён 100%", Condition.NEW, 8900, 10, 10, 2, 340,
                "Свободное платье-миди с поясом и карманами в боковых швах. Шьём по вашим меркам из "
                        + "предварительно постиранного льна — вещь не сядет после стирки. Длину и рукав можно "
                        + "изменить: напишите пожелания в комментарии к заказу.",
                "#B89B72", "#9C7F58");
        Listing suit = listing(nit, "Мужской костюм-тройка по меркам", Category.SUITS, Gender.MEN,
                "44–60 (по меркам)", "Ателье «Нить»", "Тёмно-синий", "Шерсть 100% (Италия)", Condition.NEW, 32000, 5, 21, 5, 410,
                "Классический костюм-тройка: пиджак на двух пуговицах, жилет и брюки со стрелками. "
                        + "Две примерки в ателье или по видеосвязи, ткань на выбор из 30 образцов.",
                "#2C3E66", "#3D5285");
        Listing coat = listing(nit, "Пальто оверсайз из шерсти", Category.OUTERWEAR, Gender.WOMEN,
                "M (46)", "Ателье «Нить»", "Кэмел", "Шерсть 80%, кашемир 20%", Condition.NEW_WITH_TAG, 24500, 1, 0, 8, 520,
                "Готовое пальто прямого кроя с отложным воротником. Подклад из вискозы, внутренний карман. "
                        + "Длина 112 см, рукав 62 см.",
                "#B07A4A", "#8E5E33");
        Listing skirt = listing(nit, "Юбка-плиссе миди", Category.DRESSES, Gender.WOMEN,
                "S (42)", "Ателье «Нить»", "Изумрудный", "Полиэстер", Condition.NEW_WITH_TAG, 5400, 3, 0, 12, 230,
                "Летящая юбка-плиссе на широком поясе с потайной молнией. Длина 78 см.",
                "#2E7D5B");
        listing(nit, "Шёлковая блуза с бантом", Category.TOPS, Gender.WOMEN,
                "M (44)", "Ателье «Нить»", "Пудровый", "Натуральный шёлк", Condition.NEW, 7600, 2, 0, 6, 190,
                "Блуза свободного кроя с бантом и манжетами на пуговицах. Подходит для офиса и вечера.",
                "#C98B8B", "#B37474");
        listing(nit, "Брюки палаццо на заказ", Category.TROUSERS, Gender.WOMEN,
                "40–56 (по меркам)", "Ателье «Нить»", "Чёрный", "Вискоза", Condition.NEW, 6800, 10, 7, 4, 150,
                "Широкие брюки с высокой посадкой и защипами. Шьём по меркам, длину подгоняем под ваш каблук.",
                "#333333");
        Listing jacket = listing(nit, "Жакет приталенный", Category.SUITS, Gender.WOMEN,
                "S (42)", "Ателье «Нить»", "Бордовый", "Шерсть", Condition.NEW, 12900, 0, 0, 30, 310,
                "Приталенный жакет с острыми лацканами.",
                "#7A2E3B");
        setStatus(jacket, ListingStatus.SOLD);

        // ------------------------------------------------------------ объявления Vintage Room
        listing(vintage, "Джинсовая куртка Levi's 90-х", Category.OUTERWEAR, Gender.UNISEX,
                "L", "Levi's", "Голубой", "Хлопок 100%", Condition.GOOD, 6500, 1, 0, 3, 610,
                "Оригинальная trucker jacket, сделана в США. Естественные потёртости на манжетах, все пуговицы на месте.",
                "#5B7DB1", "#4A6A9C");
        listing(vintage, "Джинсы Wrangler прямого кроя", Category.TROUSERS, Gender.MEN,
                "W32 L32", "Wrangler", "Синий", "Хлопок", Condition.EXCELLENT, 3900, 1, 0, 5, 280,
                "Плотный деним, прямой крой. Без дефектов. ПОТ 42 см, длина по внутреннему шву 81 см.",
                "#34558B");
        Listing boots = listing(vintage, "Ботинки Dr. Martens 1460", Category.SHOES, Gender.UNISEX,
                "41 EU", "Dr. Martens", "Чёрный", "Натуральная кожа", Condition.GOOD, 8900, 1, 0, 2, 740,
                "Классические 8-дырочные ботинки. Подошва без износа, на носке лёгкие потёртости. Сделаны в Англии.",
                "#2B2B2B", "#454545");
        listing(vintage, "Кожаная сумка-портфель", Category.BAGS, Gender.UNISEX,
                "Без размера", "Винтаж", "Коньячный", "Натуральная кожа", Condition.EXCELLENT, 5200, 1, 0, 9, 330,
                "Портфель на два отделения, формат А4. Ремень через плечо в комплекте.",
                "#8B5A2B");
        Listing sweater = listing(vintage, "Свитер с косами из шерсти", Category.KNITWEAR, Gender.MEN,
                "XL", "Benetton", "Серый меланж", "Шерсть 100%", Condition.EXCELLENT, 2900, 1, 0, 11, 175,
                "Тёплый свитер крупной вязки. Без катышков, после химчистки.",
                "#8C8C8C");
        Listing sneakers = listing(vintage, "Кроссовки New Balance 574", Category.SHOES, Gender.WOMEN,
                "38 EU", "New Balance", "Серый", "Замша, текстиль", Condition.NEW_WITH_TAG, 7400, 2, 0, 7, 390,
                "Новые кроссовки в коробке. Стелька 24,5 см.",
                "#9AA3AD");
        Listing bomber = listing(vintage, "Бомбер Alpha Industries MA-1", Category.OUTERWEAR, Gender.MEN,
                "M", "Alpha Industries", "Оливковый", "Нейлон", Condition.EXCELLENT, 7900, 0, 0, 25, 450,
                "Культовый бомбер с оранжевой подкладкой.",
                "#5C6B3C");
        setStatus(bomber, ListingStatus.SOLD);
        listing(vintage, "Винтажная футболка с принтом", Category.TOPS, Gender.UNISEX,
                "M", "Fruit of the Loom", "Песочный", "Хлопок", Condition.GOOD, 1500, 1, 0, 14, 95,
                "Футболка начала 2000-х, принт без трещин. Одиночный шов.",
                "#C9A27E");
        Listing glasses = listing(vintage, "Солнцезащитные очки в стиле 70-х", Category.ACCESSORIES, Gender.UNISEX,
                "Без размера", "Винтаж", "Чёрный", "Ацетат", Condition.GOOD, 2100, 1, 0, 4, 120,
                "Крупная оправа, линзы без царапин.",
                "#3A3A3A");
        Listing replica = listing(vintage, "Сумка «как оригинал»", Category.BAGS, Gender.WOMEN,
                "Без размера", "—", "Бежевый", "Экокожа", Condition.NEW, 3500, 1, 0, 10, 60,
                "Копия известного бренда.",
                "#C2A27C");
        setStatus(replica, ListingStatus.BLOCKED);

        // ------------------------------------------------------------ объявления «Бусина»
        Listing earrings = listing(busina, "Серьги с жемчугом ручной работы", Category.JEWELRY, Gender.WOMEN,
                "Без размера", "Мастерская «Бусина»", "Жемчужный", "Серебро 925, речной жемчуг", Condition.NEW, 3200, 6, 0, 3, 260,
                "Лёгкие серьги-капли из серебра 925 пробы с речным жемчугом. Длина 3,5 см. Упакуем в подарочную коробочку.",
                "#A89F91");
        Listing necklace = listing(busina, "Колье из бирюзы на заказ", Category.JEWELRY, Gender.WOMEN,
                "40–50 см", "Мастерская «Бусина»", "Бирюзовый", "Бирюза, латунь", Condition.NEW, 4500, 10, 5, 1, 140,
                "Соберу колье из натуральной бирюзы под вашу длину и стиль. Пришлю фото перед отправкой.",
                "#2A9D8F");
        Listing beret = listing(busina, "Берет из ангоры", Category.HATS, Gender.WOMEN,
                "Универсальный", "Мастерская «Бусина»", "Бордовый", "Ангора 70%, шерсть 30%", Condition.NEW_WITH_TAG, 1900, 4, 0, 8, 210,
                "Мягкий тёплый берет, не колется. Ручная вязка.",
                "#8E2C48");
        listing(busina, "Шёлковый платок с росписью", Category.ACCESSORIES, Gender.WOMEN,
                "90×90 см", "Мастерская «Бусина»", "Терракотовый", "Натуральный шёлк", Condition.NEW, 4200, 3, 7, 5, 160,
                "Платок ручной росписи в технике холодного батика. Можно заказать роспись по вашему эскизу.",
                "#C0603E", "#A84E2F");
        Listing cardigan = listing(busina, "Вязаный кардиган оверсайз", Category.KNITWEAR, Gender.WOMEN,
                "Oversize (42–50)", "Мастерская «Бусина»", "Горчичный", "Мохер, шёлк", Condition.NEW, 6900, 1, 0, 2, 300,
                "Воздушный кардиган из кид-мохера на шёлке. Длина 68 см.",
                "#C99A2E");
        Listing panama = listing(busina, "Панама из льна", Category.HATS, Gender.UNISEX,
                "57–58", "Мастерская «Бусина»", "Оливковый", "Лён", Condition.NEW, 1600, 5, 0, 6, 85,
                "Летняя панама из плотного льна, двусторонняя.",
                "#6B7A3A");

        // ------------------------------------------------------------ заказы и отзывы
        Order o1 = order(anna, nit, jacket, 1, OrderStatus.DELIVERED, DeliveryMethod.CDEK, PaymentMethod.CARD, 20,
                "CDEK-10457823", null);
        review(o1, anna, 5, "Жакет сидит идеально, ткань плотная и приятная. Упаковано аккуратно, спасибо ателье!",
                "Анна, спасибо за отзыв! Будем рады сшить для вас что-то ещё.", 17);
        Order o2 = order(dmitry, vintage, bomber, 1, OrderStatus.DELIVERED, DeliveryMethod.POST, PaymentMethod.SBP, 12,
                "RA123456789RU", null);
        review(o2, dmitry, 4, "Бомбер в хорошем состоянии, как на фото. Доставка заняла чуть дольше обещанного.", null, 8);
        Order o7 = order(dmitry, nit, skirt, 1, OrderStatus.DELIVERED, DeliveryMethod.COURIER, PaymentMethod.CARD, 9,
                "CR-7781", null);
        review(o7, dmitry, 5, "Брал в подарок жене — размер подошёл, юбка очень красивая.", null, 6);
        order(anna, vintage, sneakers, 1, OrderStatus.SHIPPED, DeliveryMethod.CDEK, PaymentMethod.CARD, 3,
                "CDEK-10499120", null);
        order(anna, nit, dress, 1, OrderStatus.NEW, DeliveryMethod.PICKUP, PaymentMethod.CASH, 0, null,
                "Рост 168, грудь 88, талия 70, бёдра 96");
        order(dmitry, busina, earrings, 1, OrderStatus.ACCEPTED, DeliveryMethod.POST, PaymentMethod.CARD, 1, null, null);
        order(anna, busina, beret, 1, OrderStatus.CANCELLED, DeliveryMethod.CDEK, PaymentMethod.CASH, 5, null, null);

        // ------------------------------------------------------------ переписка
        chat(anna, nit, dress, 1,
                new long[]{anna.id, nit.id, anna.id},
                new String[]{
                        "Здравствуйте! Можно сшить платье на 10 см длиннее, ниже колена?",
                        "Добрый день! Конечно. Укажите длину в комментарии к заказу — сошьём по вашим меркам.",
                        "Спасибо, оформила заказ!"});
        chat(dmitry, vintage, boots, 2,
                new long[]{dmitry.id, vintage.id},
                new String[]{
                        "Добрый вечер. Подошва сильно стёрта?",
                        "Подошва в хорошем состоянии, на носке лёгкие потёртости. Могу прислать ещё фото."});

        // ------------------------------------------------------------ избранное и корзина
        favorite(anna, coat);
        favorite(anna, boots);
        favorite(anna, necklace);
        favorite(anna, cardigan);
        favorite(dmitry, suit);
        favorite(dmitry, sweater);
        db.cart().upsert(new CartItem(anna.id, cardigan.id, 1));
        db.cart().upsert(new CartItem(anna.id, panama.id, 1));

        // ------------------------------------------------------------ жалобы
        complaint(dmitry, glasses, "Подозрение на подделку", "На фото не видно маркировки, похоже на реплику.",
                ComplaintStatus.OPEN, 1);
        complaint(anna, replica, "Подделка / реплика", "В описании прямо написано, что это копия бренда.",
                ComplaintStatus.RESOLVED, 9);

        // ------------------------------------------------------------ уведомления
        db.notifications().insert(new AppNotification(anna.id, AppNotification.TYPE_SYSTEM, 0,
                "Добро пожаловать в «Русский ресейл»!", "Выбирайте одежду и аксессуары от ателье и продавцов"));
        db.notifications().insert(new AppNotification(nit.id, AppNotification.TYPE_SYSTEM, 0,
                "Добро пожаловать в «Русский ресейл»!", "Ваши объявления видят покупатели по всей России"));
        db.notifications().insert(new AppNotification(admin.id, AppNotification.TYPE_LISTING, glasses.id,
                "Новая жалоба", "Подозрение на подделку: «" + glasses.title + "»"));
    }

    // ================================================================ помощники

    private User user(String email, String name, String phone, String city, Role role,
                      String shopName, String shopDescription, int daysAgo) {
        User u = new User();
        u.email = email;
        u.fullName = name;
        u.phone = phone;
        u.city = city;
        u.role = role.name();
        u.shopName = shopName;
        u.shopDescription = shopDescription;
        u.salt = PasswordHasher.newSalt();
        u.passwordHash = PasswordHasher.hash(PASSWORD, u.salt);
        u.createdAt = now - daysAgo * DAY;
        u.id = db.users().insert(u);
        return u;
    }

    private Listing listing(User seller, String title, Category category, Gender gender, String size, String brand,
                            String color, String material, Condition condition, int price, int quantity,
                            int productionDays, int daysAgo, int views, String description, String... colors) {
        Listing l = new Listing();
        l.sellerId = seller.id;
        l.title = title;
        l.category = category.name();
        l.gender = gender.name();
        l.size = size;
        l.brand = brand;
        l.color = color;
        l.material = material;
        l.condition = condition.name();
        l.price = price;
        l.quantity = quantity;
        l.madeToOrder = productionDays > 0;
        l.productionDays = productionDays;
        l.city = seller.city;
        l.description = description;
        l.views = views;
        l.status = ListingStatus.ACTIVE.name();
        l.createdAt = now - daysAgo * DAY - (long) (Math.random() * DAY / 2);
        l.updatedAt = l.createdAt;
        List<String> photos = new ArrayList<>();
        for (String hex : colors) photos.add(ImageLoader.demo(category, hex));
        l.setPhotoList(photos);
        l.updateSearchText();
        l.id = db.listings().insert(l);
        return l;
    }

    private void setStatus(Listing l, ListingStatus status) {
        l.status = status.name();
        db.listings().update(l);
    }

    private Order order(User buyer, User seller, Listing listing, int quantity, OrderStatus status,
                        DeliveryMethod delivery, PaymentMethod payment, int daysAgo, String track, String measurements) {
        Order o = new Order();
        o.buyerId = buyer.id;
        o.sellerId = seller.id;
        o.status = status.name();
        o.deliveryMethod = delivery.name();
        o.itemsTotal = listing.price * quantity;
        o.deliveryCost = PriceCalculator.deliveryCost(delivery, o.itemsTotal);
        o.total = o.itemsTotal + o.deliveryCost;
        o.address = delivery.needsAddress ? (buyer.city + ", ул. Тверская, д. 7, кв. 12") : null;
        o.phone = buyer.phone;
        o.paymentMethod = payment.name();
        o.paid = payment != PaymentMethod.CASH || status == OrderStatus.DELIVERED;
        o.trackNumber = track;
        o.measurements = measurements;
        if (status == OrderStatus.CANCELLED) o.cancelReason = "Покупатель передумал";
        o.createdAt = now - daysAgo * DAY - 3 * 60 * 60 * 1000L;
        o.updatedAt = o.createdAt + (status == OrderStatus.NEW ? 0 : DAY);
        o.id = db.orders().insert(o);
        OrderItem item = new OrderItem();
        item.orderId = o.id;
        item.listingId = listing.id;
        item.title = listing.title;
        item.photo = listing.mainPhoto();
        item.size = listing.size;
        item.price = listing.price;
        item.quantity = quantity;
        item.madeToOrder = listing.madeToOrder;
        db.orders().insertItems(Collections.singletonList(item));
        if (status != OrderStatus.CANCELLED && listing.quantity > 0) {
            listing.quantity -= quantity;
            db.listings().update(listing);
        }
        return o;
    }

    private void review(Order order, User buyer, int rating, String text, String reply, int daysAgo) {
        OrderItem item = db.orders().getItems(order.id).get(0);
        Review r = new Review();
        r.orderItemId = item.id;
        r.orderId = order.id;
        r.listingId = item.listingId;
        r.sellerId = order.sellerId;
        r.buyerId = buyer.id;
        r.rating = rating;
        r.text = text;
        r.createdAt = now - daysAgo * DAY;
        if (reply != null) {
            r.sellerReply = reply;
            r.replyAt = r.createdAt + DAY / 3;
        }
        db.reviews().insert(r);
        item.reviewed = true;
        db.orders().updateItem(item);
    }

    private void chat(User buyer, User seller, Listing listing, int daysAgo, long[] senders, String[] texts) {
        Chat c = new Chat();
        c.buyerId = buyer.id;
        c.sellerId = seller.id;
        c.listingId = listing.id;
        c.id = db.chats().insert(c);
        long time = now - daysAgo * DAY;
        for (int i = 0; i < texts.length; i++) {
            Message m = new Message();
            m.chatId = c.id;
            m.senderId = senders[i];
            m.text = texts[i];
            m.createdAt = time + i * 25 * 60 * 1000L;
            db.chats().insertMessage(m);
            c.lastMessage = m.text;
            c.updatedAt = m.createdAt;
        }
        if (senders[senders.length - 1] == buyer.id) c.unreadSeller = 1;
        else c.unreadBuyer = 1;
        db.chats().update(c);
    }

    private void favorite(User user, Listing listing) {
        db.favorites().insert(new Favorite(user.id, listing.id));
    }

    private void complaint(User reporter, Listing listing, String reason, String text, ComplaintStatus status, int daysAgo) {
        Complaint c = new Complaint();
        c.listingId = listing.id;
        c.reporterId = reporter.id;
        c.reason = reason;
        c.text = text;
        c.status = status.name();
        c.createdAt = now - daysAgo * DAY;
        db.complaints().insert(c);
    }
}
