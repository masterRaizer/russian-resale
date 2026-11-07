"""Генерирует схемы графической части в формате draw.io: docs/diagrams/*.drawio.

Запуск: python3 docs/tools/diagrams.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from drawio import (Diagram, box_style, db_table, decision_style, edge_style, text_style, uml_class,  # noqa: E402
                    uml_style, FONT)

OUT = os.path.join(os.path.dirname(__file__), "..", "diagrams")


# ============================================================================ БД
def db_diagram():
    d = Diagram("Диаграмма базы данных", 1180, 900)
    I, T = "INTEGER", "TEXT"
    tables = {
        "users": ("users (пользователи)", [
            ("PK", "id", I), ("", "email", T + " UQ"), ("", "passwordHash", T), ("", "salt", T),
            ("", "fullName", T), ("", "phone", T), ("", "city", T), ("", "role", T), ("", "shopName", T),
            ("", "shopDescription", T), ("", "avatarPath", T), ("", "blocked", I), ("", "createdAt", I)]),
        "listings": ("listings (объявления)", [
            ("PK", "id", I), ("FK", "sellerId", I), ("", "title", T), ("", "description", T), ("", "category", T),
            ("", "gender", T), ("", "size", T), ("", "brand", T), ("", "color", T), ("", "material", T),
            ("", "condition", T), ("", "price", I), ("", "quantity", I), ("", "madeToOrder", I),
            ("", "productionDays", I), ("", "city", T), ("", "status", T), ("", "photos", T), ("", "views", I),
            ("", "searchText", T), ("", "createdAt", I), ("", "updatedAt", I)]),
        "favorites": ("favorites (избранное)", [
            ("PK,FK", "userId", I), ("PK,FK", "listingId", I), ("", "createdAt", I)]),
        "cart_items": ("cart_items (корзина)", [
            ("PK,FK", "userId", I), ("PK,FK", "listingId", I), ("", "quantity", I), ("", "addedAt", I)]),
        "orders": ("orders (заказы)", [
            ("PK", "id", I), ("FK", "buyerId", I), ("FK", "sellerId", I), ("", "status", T),
            ("", "deliveryMethod", T), ("", "deliveryCost", I), ("", "address", T), ("", "phone", T),
            ("", "comment", T), ("", "measurements", T), ("", "paymentMethod", T), ("", "paid", I),
            ("", "itemsTotal", I), ("", "total", I), ("", "trackNumber", T), ("", "cancelReason", T),
            ("", "createdAt", I), ("", "updatedAt", I)]),
        "order_items": ("order_items (позиции заказа)", [
            ("PK", "id", I), ("FK", "orderId", I), ("FK", "listingId", I), ("", "title", T), ("", "photo", T),
            ("", "size", T), ("", "price", I), ("", "quantity", I), ("", "madeToOrder", I), ("", "reviewed", I)]),
        "reviews": ("reviews (отзывы)", [
            ("PK", "id", I), ("FK", "orderItemId", I + " UQ"), ("FK", "orderId", I), ("FK", "listingId", I),
            ("FK", "sellerId", I), ("FK", "buyerId", I), ("", "rating", I), ("", "text", T),
            ("", "sellerReply", T), ("", "createdAt", I), ("", "replyAt", I)]),
        "chats": ("chats (чаты)", [
            ("PK", "id", I), ("FK", "buyerId", I), ("FK", "sellerId", I), ("FK", "listingId", I),
            ("", "lastMessage", T), ("", "updatedAt", I), ("", "unreadBuyer", I), ("", "unreadSeller", I)]),
        "messages": ("messages (сообщения)", [
            ("PK", "id", I), ("FK", "chatId", I), ("FK", "senderId", I), ("", "text", T), ("", "createdAt", I)]),
        "notifications": ("notifications (уведомления)", [
            ("PK", "id", I), ("FK", "userId", I), ("", "title", T), ("", "text", T), ("", "type", T),
            ("", "refId", I), ("", "read", I), ("", "createdAt", I)]),
        "complaints": ("complaints (жалобы)", [
            ("PK", "id", I), ("FK", "listingId", I), ("FK", "reporterId", I), ("", "reason", T), ("", "text", T),
            ("", "status", T), ("", "createdAt", I)]),
    }
    pos = {
        "notifications": (20, 60), "favorites": (20, 300), "cart_items": (20, 420), "complaints": (20, 560),
        "users": (320, 60), "listings": (320, 340),
        "orders": (620, 60), "order_items": (620, 420),
        "reviews": (920, 60), "chats": (920, 330), "messages": (920, 520),
    }
    W = 230
    ids, geo = {}, {}
    for key, (title, fields) in tables.items():
        x, y = pos[key]
        h = 22 + 13 * len(fields) + 4
        geo[key] = (x, y, W, h)
        ids[key] = d.vertex(x, y, W, h, db_table(title, fields),
                            "html=1;whiteSpace=wrap;overflow=fill;verticalAlign=top;align=left;spacing=0;"
                            "fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontFamily=%s;fontSize=10;" % FONT)
    er = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=ERmandOne;startArrow=ERmany;endFill=0;"
          "startFill=0;strokeColor=#000000;fontColor=#000000;fontFamily=%s;fontSize=10;labelBackgroundColor=#FFFFFF;" % FONT)

    def port(key, side, coord):
        x, y, w, h = geo[key]
        if side == "left":
            return 0, (coord - y) / h
        if side == "right":
            return 1, (coord - y) / h
        if side == "top":
            return (coord - x) / w, 0
        return (coord - x) / w, 1

    def rel(child, s_side, s_coord, parent, t_side, t_coord, label="", points=None):
        ex, ey = port(child, s_side, s_coord)
        nx, ny = port(parent, t_side, t_coord)
        style = er + ("exitX=%.3f;exitY=%.3f;exitDx=0;exitDy=0;entryX=%.3f;entryY=%.3f;entryDx=0;entryDy=0;"
                      % (ex, ey, nx, ny))
        d.edge(ids[child], ids[parent], style, label, points)

    rel("notifications", "right", 100, "users", "left", 100)
    rel("favorites", "right", 320, "users", "left", 215)
    rel("favorites", "right", 345, "listings", "left", 360)
    rel("cart_items", "right", 440, "users", "left", 235)
    rel("cart_items", "right", 470, "listings", "left", 470)
    rel("complaints", "right", 600, "listings", "left", 600)
    rel("listings", "top", 435, "users", "bottom", 435, "sellerId")
    rel("orders", "left", 110, "users", "right", 110, "buyerId")
    rel("orders", "left", 150, "users", "right", 150, "sellerId")
    rel("order_items", "top", 735, "orders", "bottom", 735)
    rel("order_items", "left", 500, "listings", "right", 500)
    rel("reviews", "left", 200, "order_items", "right", 450, "", [(885, 200), (885, 450)])
    rel("reviews", "left", 120, "orders", "right", 120)
    rel("chats", "left", 370, "listings", "right", 370)
    rel("chats", "left", 345, "users", "right", 235, "", [(590, 345), (590, 235)])
    rel("messages", "top", 1035, "chats", "bottom", 1035)
    return d


# ============================================================================ классы
def class_diagram():
    d = Diagram("Диаграмма классов", 1200, 1060)
    W = 260
    boxes = {}

    def cls(key, x, y, title, attrs, methods=(), stereotype=None, w=W):
        h = 24 + (14 if stereotype else 0) + 14 * len(attrs) + 10 + (14 * len(methods) + 10 if methods else 0)
        boxes[key] = (x, y, w, h)
        d.vertex(x, y, w, h, uml_class(title, attrs, methods, stereotype), uml_style())
        return h

    # левая колонка: связующие классы и перечисления
    cls("role", 20, 20, "Роль", ["BUYER (покупатель)", "SELLER (продавец)", "ADMIN (администратор)"],
        stereotype="enumeration", w=210)
    cls("favorite", 20, 150, "Избранное", ["пользовательId: long", "объявлениеId: long"], ["переключить(): boolean"],
        w=210)
    cls("cart", 20, 260, "ПозицияКорзины", ["пользовательId: long", "объявлениеId: long", "количество: int"],
        ["добавить(): void", "изменитьКоличество(n): void"], w=210)
    cls("category", 20, 430, "Категория", ["OUTERWEAR, DRESSES, TOPS,", "TROUSERS, SUITS, KNITWEAR,",
                                         "SHOES, BAGS, HATS,", "JEWELRY, ACCESSORIES"], stereotype="enumeration", w=210)
    cls("lstatus", 20, 570, "СтатусОбъявления", ["ACTIVE (активно)", "SOLD (продано)", "ARCHIVED (в архиве)",
                                                "BLOCKED (заблокировано)"], stereotype="enumeration", w=210)
    cls("ostatus", 20, 800, "СтатусЗаказа", ["NEW (ждёт подтверждения)", "ACCEPTED (принят)", "SHIPPED (отправлен)",
                                            "DELIVERED (получен)", "CANCELLED (отменён)"],
        stereotype="enumeration", w=210)

    # центральная колонка
    cls("user", 290, 20, "Пользователь", [
        "id: long", "email: String", "хешПароля: String", "соль: String", "ФИО: String", "телефон: String",
        "город: String", "роль: Роль", "названиеАтелье: String", "заблокирован: boolean"], [
        "зарегистрироваться(): User", "войти(email, пароль): User", "изменитьПрофиль(): void",
        "сменитьПароль(): boolean"])
    cls("listing", 290, 360, "Объявление", [
        "id: long", "продавецId: long", "название: String", "описание: String", "категория: Категория",
        "размер: String", "бренд: String", "состояние: String", "цена: int", "количество: int",
        "пошивНаЗаказ: boolean", "срокПошива: int", "статус: СтатусОбъявления", "фото: List<String>"], [
        "сохранить(): long", "снятьСПродажи(): void", "удалить(): void", "добавитьВИзбранное(): boolean",
        "пожаловаться(причина): void"])
    cls("order", 290, 760, "Заказ", [
        "id: long", "покупательId: long", "продавецId: long", "статус: СтатусЗаказа",
        "способДоставки: String", "адрес: String", "мерки: String", "способОплаты: String",
        "оплачен: boolean", "суммаТоваров: int", "итого: int", "трекНомер: String"], [
        "оформить(запрос): List<long>", "принять(): void", "отправить(трек): void",
        "подтвердитьПолучение(): void", "отменить(причина): void"])

    # правые колонки
    cls("notification", 620, 20, "Уведомление", ["id: long", "пользовательId: long", "заголовок: String",
                                                "текст: String", "прочитано: boolean"], ["отметитьПрочитанным(): void"])
    cls("chat", 620, 200, "Чат", ["id: long", "покупательId: long", "продавецId: long", "объявлениеId: long",
                                 "непрочитано: int"], ["открыть(): long", "отправить(текст): void"])
    cls("message", 620, 410, "Сообщение", ["id: long", "чатId: long", "отправительId: long", "текст: String",
                                          "время: long"])
    cls("item", 620, 760, "ПозицияЗаказа", ["id: long", "заказId: long", "объявлениеId: long", "название: String",
                                            "размер: String", "цена: int", "количество: int",
                                            "отзывОставлен: boolean"])
    cls("complaint", 940, 200, "Жалоба", ["id: long", "объявлениеId: long", "авторId: long", "причина: String",
                                         "статус: String"], ["рассмотреть(заблокировать): void"])
    cls("price", 940, 560, "РасчётСтоимости", ["ПОРОГ_БЕСПЛАТНОЙ: int = 7000", "КОМИССИЯ: int = 5 %"], [
        "стоимостьДоставки(способ, сумма): int", "итог(сумма, способ): int", "комиссия(выручка): long"])
    cls("review", 940, 760, "Отзыв", ["id: long", "позицияId: long", "продавецId: long", "покупательId: long",
                                     "оценка: int (1..5)", "текст: String", "ответПродавца: String"], [
        "оставить(оценка, текст): void", "ответить(текст): void"])

    ids = {}
    for c in d.cells:
        pass
    # id ячеек по порядку создания
    keys = list(boxes.keys())
    for k, c in zip(keys, [c for c in d.cells if c["kind"] == "v"]):
        ids[k] = c["id"]

    def port(key, side, coord):
        x, y, w, h = boxes[key]
        if side == "left":
            return 0, (coord - y) / h
        if side == "right":
            return 1, (coord - y) / h
        if side == "top":
            return (coord - x) / w, 0
        return (coord - x) / w, 1

    def link(a, a_side, a_c, b, b_side, b_c, kind="assoc", label="", points=None):
        ex, ey = port(a, a_side, a_c)
        nx, ny = port(b, b_side, b_c)
        if kind == "assoc":
            st = edge_style("none", "none")
        elif kind == "dep":
            st = edge_style("open", "none", dashed=True)
        elif kind == "comp":
            st = edge_style("none", "diamondThin", extra="startFill=1;startSize=14;")
        else:
            st = edge_style("none", "none")
        st += ("exitX=%.3f;exitY=%.3f;exitDx=0;exitDy=0;entryX=%.3f;entryY=%.3f;entryDx=0;entryDy=0;"
               "labelBackgroundColor=#FFFFFF;" % (ex, ey, nx, ny))
        d.edge(ids[a], ids[b], st, label, points)

    # зависимости от перечислений
    link("user", "left", 70, "role", "right", 70, "dep")
    link("listing", "left", 500, "category", "right", 500, "dep")
    link("listing", "left", 620, "lstatus", "right", 620, "dep")
    link("order", "left", 860, "ostatus", "right", 860, "dep")
    # избранное и корзина связывают покупателя и объявление
    link("favorite", "right", 180, "user", "left", 180, "assoc", "0..* — 1")
    link("favorite", "right", 220, "listing", "left", 390, "assoc", "", [(255, 220), (255, 390)])
    link("cart", "right", 290, "user", "left", 290, "assoc", "0..* — 1")
    link("cart", "right", 340, "listing", "left", 420, "assoc", "", [(245, 340), (245, 420)])
    # ассоциации
    link("user", "bottom", 420, "listing", "top", 420, "assoc", "1 размещает 0..*")
    link("user", "right", 60, "notification", "left", 60, "assoc", "1 — 0..*")
    link("chat", "left", 240, "user", "right", 240, "assoc", "0..* — 1")
    link("chat", "left", 320, "listing", "right", 400, "assoc", "0..* — 1", [(585, 320), (585, 400)])
    link("chat", "bottom", 750, "message", "top", 750, "comp", "1 содержит 0..*")
    link("complaint", "bottom", 1060, "listing", "right", 535, "assoc", "0..* — 1", [(1060, 535)])
    link("user", "right", 290, "order", "right", 1000, "assoc", "", [(605, 290), (605, 1000)])
    d.vertex(612, 676, 130, 18, "1 оформляет 0..*", text_style(11))
    link("order", "right", 850, "item", "left", 850, "comp", "1 — 1..*")
    link("item", "top", 750, "listing", "right", 660, "assoc", "0..* — 1", [(750, 660)])
    link("item", "right", 850, "review", "left", 850, "assoc", "1 — 0..1")
    link("order", "right", 920, "price", "bottom", 1060, "dep", "использует",
         [(600, 920), (600, 740), (1060, 740)])
    return d


# ============================================================================ состояния
def state_diagram():
    d = Diagram("Диаграмма состояний", 800, 1180)
    d.vertex(20, 0, 760, 30, "Диаграмма состояний заказа", text_style(16, bold=True))
    lanes = [("Выбор вещи", 40, 330), ("Оформление заказа", 330, 640), ("Обработка продавцом", 640, 900),
             ("Получение и отзыв", 900, 1160)]
    for title, y1, y2 in lanes:
        d.vertex(20, y1, 760, y2 - y1, "", "rounded=0;html=1;fillColor=none;strokeColor=#000000;dashed=1;")
        d.vertex(20, y1, 34, y2 - y1, title,
                 "rounded=0;html=1;whiteSpace=wrap;fillColor=#F2F2F2;strokeColor=#000000;fontColor=#000000;"
                 "horizontal=0;fontFamily=%s;fontSize=12;fontStyle=1;" % FONT)
    st = box_style(rounded=True, size=11)
    dec = decision_style(10)
    start = d.vertex(85, 80, 30, 30, "", "shape=ellipse;perimeter=ellipsePerimeter;html=1;fillColor=#000000;strokeColor=#000000;")
    catalog = d.vertex(150, 70, 170, 50, "Каталог: поиск, категории, фильтры", st)
    card = d.vertex(360, 70, 160, 50, "Карточка вещи", st)
    d1 = d.vertex(560, 50, 180, 90, "Вход выполнен как покупатель?", dec)
    login = d.vertex(565, 210, 170, 56, "Вход или регистрация покупателя", st)

    d2 = d.vertex(565, 345, 170, 80, "Купить сейчас?", dec)
    cart = d.vertex(330, 360, 180, 50, "Корзина: одна или несколько вещей", st)
    checkout = d.vertex(330, 450, 180, 56, "Оформление: доставка, оплата, мерки", st)
    d3 = d.vertex(335, 530, 170, 96, "Вещь в наличии, данные заполнены?", dec)
    error = d.vertex(90, 553, 170, 50, "Сообщение об ошибке", st)

    new = d.vertex(330, 655, 180, 50, "Ждёт подтверждения (NEW)", st)
    d4 = d.vertex(335, 725, 170, 86, "Продавец принял заказ?", dec)
    cancelled = d.vertex(80, 735, 190, 66, "Отменён (CANCELLED), вещь вернулась в продажу", st)
    end1 = d.vertex(160, 835, 30, 30, "", "shape=endState;perimeter=ellipsePerimeter;html=1;fillColor=#000000;strokeColor=#000000;")
    accepted = d.vertex(560, 735, 180, 66, "Принят (ACCEPTED): сборка или пошив по меркам", st)
    shipped = d.vertex(560, 830, 180, 50, "Отправлен (SHIPPED), трек-номер", st)

    confirm = d.vertex(560, 920, 180, 56, "Покупатель подтверждает получение", st)
    delivered = d.vertex(330, 920, 180, 56, "Получен (DELIVERED)", st)
    review = d.vertex(330, 1010, 180, 56, "Отзыв: оценка 1–5 и комментарий", st)
    reply = d.vertex(90, 1010, 180, 56, "Ответ продавца на отзыв", st)
    end2 = d.vertex(165, 1105, 30, 30, "", "shape=endState;perimeter=ellipsePerimeter;html=1;fillColor=#000000;strokeColor=#000000;")

    e = edge_style("block")
    d.edge(start, catalog, e)
    d.edge(catalog, card, e)
    d.edge(card, d1, e)
    d.edge(d1, login, e, "нет")
    d.edge(login, card, e + "exitX=0;exitY=0.5;entryX=0.5;entryY=1;", "вход выполнен", [(440, 238)])
    d.edge(d1, d2, e + "exitX=1;exitY=0.5;entryX=1;entryY=0.5;", "да", [(760, 95), (760, 385)])
    d.edge(d2, cart, e, "нет")
    d.edge(d2, checkout, e + "exitX=0.5;exitY=1;entryX=1;entryY=0.5;", "да", [(650, 478)])
    d.edge(cart, checkout, e)
    d.edge(checkout, d3, e)
    d.edge(d3, error, e, "нет")
    d.edge(error, checkout, e + "exitX=0.5;exitY=0;entryX=0;entryY=0.5;", "", [(175, 478)])
    d.edge(d3, new, e, "да")
    d.edge(new, d4, e)
    d.edge(new, cancelled, e + "exitX=0;exitY=0.5;entryX=0.5;entryY=0;", "покупатель отменил", [(175, 680)])
    d.edge(d4, cancelled, e, "нет")
    d.edge(d4, accepted, e, "да")
    d.edge(cancelled, end1, e)
    d.edge(accepted, shipped, e)
    d.edge(shipped, confirm, e)
    d.edge(confirm, delivered, e)
    d.edge(delivered, review, e)
    d.edge(review, reply, e)
    d.edge(reply, end2, e)
    return d


# ============================================================================ блок-схема
def flow_diagram():
    d = Diagram("Структура разделов и экранов приложения", 980, 1000)
    st = box_style(rounded=False, size=11)
    hdr = box_style(rounded=True, fill="#F2F2F2", bold=True, size=12)
    dec = decision_style(11)
    e = edge_style("block")
    start = d.vertex(390, 10, 200, 44, "Запуск приложения", box_style(rounded=True, size=12, bold=True))
    d1 = d.vertex(400, 80, 180, 84, "Сессия сохранена?", dec)
    login = d.vertex(660, 92, 230, 60, "Вход или регистрация с выбором роли: покупатель или продавец", st)
    d2 = d.vertex(400, 190, 180, 84, "Роль пользователя", dec)
    d.edge(start, d1, e)
    d.edge(d1, login, e, "нет")
    d.edge(login, d2, e + "exitX=0.5;exitY=1;entryX=1;entryY=0.5;", "", [(775, 232)])
    d.edge(d1, d2, e, "да")

    roles = [
        ("Покупатель", 30, [
            ("Каталог", "поиск, категории, фильтры, сортировка"),
            ("Карточка вещи", "фото, характеристики, отзывы, продавец"),
            ("Чат с продавцом", "вопросы о вещи"),
            ("Избранное", "сохранённые вещи"),
            ("Корзина → оформление", "доставка, оплата, мерки"),
            ("Заказы", "статусы, получение, отзыв"),
            ("Профиль", "сообщения, мои отзывы, тема"),
        ]),
        ("Продавец (ателье)", 350, [
            ("Мои объявления", "создание, редактирование, архив"),
            ("Заказы", "принять, отправить, отменить"),
            ("Сообщения", "ответы покупателям"),
            ("Отзывы", "рейтинг, ответ на отзыв"),
            ("Профиль", "публичный профиль, статистика"),
        ]),
        ("Администратор", 670, [
            ("Жалобы", "заблокировать или отклонить"),
            ("Объявления", "поиск и блокировка"),
            ("Пользователи", "блокировка аккаунтов"),
            ("Профиль", "статистика площадки"),
        ]),
    ]
    W = 280
    last_profiles = []
    for title, x, tabs in roles:
        head = d.vertex(x, 320, W, 40, title, hdr)
        d.edge(d2, head, e + "exitX=0.5;exitY=1;entryX=0.5;entryY=0;", "", [(490, 295), (x + W / 2, 295)])
        y = 390
        prev = head
        for name, sub in tabs:
            box = d.vertex(x + 30, y, W - 30, 50, "<b>%s</b><br><font style=\"font-size:10px\">%s</font>" % (name, sub), st)
            d.edge(head, box, e + "exitX=0.04;exitY=1;entryX=0;entryY=0.5;", "", [(x + 12, y + 25)])
            prev = box
            y += 66
        last_profiles.append((prev, x, y))
    d.vertex(30, 940, 330, 44, "Уведомления о заказах, отзывах, жалобах и модерации — у всех ролей",
             box_style(rounded=False, size=11, dashed=True))
    exit_box = d.vertex(390, 940, 200, 44, "Выход из аккаунта", box_style(rounded=True, size=12, bold=True))
    for prof, x, y in last_profiles:
        d.edge(prof, exit_box, edge_style("block", dashed=True) + "exitX=0.6;exitY=1;entryX=0.5;entryY=0;",
               "", [(x + 30 + (W - 30) * 0.6, 928), (490, 928)])
    d.edge(exit_box, login, e + "exitX=1;exitY=0.5;entryX=1;entryY=0.5;", "", [(960, 962), (960, 122)])
    return d


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for name, fn in [("db", db_diagram), ("classes", class_diagram), ("states", state_diagram),
                     ("flow", flow_diagram)]:
        dia = fn()
        with open(os.path.join(OUT, name + ".drawio"), "w", encoding="utf-8") as f:
            f.write(dia.to_drawio())
        print("written", name)
