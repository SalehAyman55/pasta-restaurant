package pasta.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import pasta.model.Order;
import pasta.model.OrderLine;
import pasta.model.Totals;
import pasta.util.Csv;
import pasta.util.Money;

public final class OrderService {

    private static final String ORDERS = "orders.csv";
    private static final String ORDERS_HEADER = "id,time,cashier,customer,phone,address,type,payment,code,subtotal,discount,tax,delivery,total";
    private static final String ITEMS = "order_items.csv";
    private static final String ITEMS_HEADER = "order_id,item,category,price,qty";

    private OrderService() {
    }

    public static synchronized Order save(String cashier, String customer, String phone, String address, String type, String payment, String code, List<OrderLine> lines) {
        Totals t = PricingService.calculate(lines, type, code);
        String applied = PricingService.validCode(code) ? code.trim().toUpperCase(Locale.ROOT) : "";
        Order order = new Order(nextId(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), cashier, customer, phone, address, type, payment, applied,
                t.subtotal(), t.discount(), t.tax(), t.delivery(), t.total(), List.copyOf(lines));
        Storage.append(ORDERS, ORDERS_HEADER, Csv.join(order.id(), order.time().toString(), order.cashier(), order.customer(), order.phone(), order.address(),
                order.type(), order.payment(), order.code(), Money.plain(order.subtotal()), Money.plain(order.discount()), Money.plain(order.tax()),
                Money.plain(order.delivery()), Money.plain(order.total())));
        for (OrderLine l : lines) {
            Storage.append(ITEMS, ITEMS_HEADER, Csv.join(order.id(), l.name(), l.category(), Money.plain(l.price()), String.valueOf(l.qty())));
        }
        return order;
    }

    public static synchronized List<Order> all() {
        Map<String, List<OrderLine>> items = new HashMap<>();
        for (List<String> r : Storage.read(ITEMS)) {
            if (r.size() < 5) {
                continue;
            }
            try {
                items.computeIfAbsent(r.get(0), k -> new ArrayList<>())
                        .add(new OrderLine(r.get(1), r.get(2), Money.of(r.get(3)), Integer.parseInt(r.get(4))));
            } catch (NumberFormatException ignored) {
            }
        }
        List<Order> out = new ArrayList<>();
        for (List<String> r : Storage.read(ORDERS)) {
            if (r.size() < 14) {
                continue;
            }
            try {
                out.add(new Order(r.get(0), LocalDateTime.parse(r.get(1)), r.get(2), r.get(3), r.get(4), r.get(5), r.get(6), r.get(7), r.get(8),
                        Money.of(r.get(9)), Money.of(r.get(10)), Money.of(r.get(11)), Money.of(r.get(12)), Money.of(r.get(13)),
                        items.getOrDefault(r.get(0), List.of())));
            } catch (NumberFormatException | DateTimeParseException ignored) {
            }
        }
        out.sort(Comparator.comparing(Order::time).reversed());
        return out;
    }

    public static List<Order> filter(Integer day, Integer month, Integer year, String text) {
        String needle = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        List<Order> out = new ArrayList<>();
        for (Order o : all()) {
            if (day != null && o.time().getDayOfMonth() != day) {
                continue;
            }
            if (month != null && o.time().getMonthValue() != month) {
                continue;
            }
            if (year != null && o.time().getYear() != year) {
                continue;
            }
            if (!needle.isEmpty() && !o.id().toLowerCase(Locale.ROOT).contains(needle)
                    && !o.phone().contains(needle) && !o.customer().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            out.add(o);
        }
        return out;
    }

    public static Map<String, Integer> topItems(List<Order> orders, int limit) {
        Map<String, Integer> totals = new HashMap<>();
        for (Order o : orders) {
            for (OrderLine l : o.lines()) {
                totals.merge(l.name(), l.qty(), Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(totals.entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(limit, entries.size()); i++) {
            out.put(entries.get(i).getKey(), entries.get(i).getValue());
        }
        return out;
    }

    public static BigDecimal revenue(List<Order> orders) {
        BigDecimal sum = Money.zero();
        for (Order o : orders) {
            sum = sum.add(o.total());
        }
        return sum;
    }

    public static void exportCsv(List<Order> orders, Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id,time,cashier,customer,phone,address,type,payment,code,subtotal,discount,tax,delivery,total,items");
        for (Order o : orders) {
            StringJoiner items = new StringJoiner("; ");
            for (OrderLine l : o.lines()) {
                items.add(l.qty() + "x " + l.name());
            }
            lines.add(Csv.joinSafe(o.id(), o.time().toString(), o.cashier(), o.customer(), o.phone(), o.address(), o.type(), o.payment(), o.code(),
                    Money.plain(o.subtotal()), Money.plain(o.discount()), Money.plain(o.tax()), Money.plain(o.delivery()), Money.plain(o.total()), items.toString()));
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private static String nextId() {
        int max = 0;
        for (List<String> r : Storage.read(ORDERS)) {
            if (r.isEmpty() || r.get(0).length() < 2) {
                continue;
            }
            try {
                max = Math.max(max, Integer.parseInt(r.get(0).substring(1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format(Locale.US, "R%06d", max + 1);
    }
}
