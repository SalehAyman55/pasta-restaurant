package pasta.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import pasta.model.Dish;
import pasta.util.Csv;
import pasta.util.Money;

public final class MenuService {

    private static final String FILE = "menu.csv";
    private static final String HEADER = "id,category,name,price";

    private MenuService() {
    }

    public static synchronized List<Dish> all() {
        if (!Storage.exists(FILE)) {
            List<Dish> seeded = defaults();
            persist(seeded);
            return seeded;
        }
        List<Dish> items = new ArrayList<>();
        for (List<String> r : Storage.read(FILE)) {
            if (r.size() < 4) {
                continue;
            }
            try {
                items.add(new Dish(r.get(0), r.get(1), r.get(2), Money.of(r.get(3))));
            } catch (NumberFormatException ignored) {
            }
        }
        return items;
    }

    public static List<String> categories() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Dish d : all()) {
            set.add(d.category());
        }
        return new ArrayList<>(set);
    }

    public static List<Dish> byCategory(String category) {
        List<Dish> out = new ArrayList<>();
        for (Dish d : all()) {
            if (d.category().equals(category)) {
                out.add(d);
            }
        }
        return out;
    }

    public static synchronized void add(String category, String name, BigDecimal price) {
        List<Dish> items = all();
        int next = 1;
        for (Dish d : items) {
            try {
                next = Math.max(next, Integer.parseInt(d.id()) + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        items.add(new Dish(String.valueOf(next), category, name, Money.round(price)));
        persist(items);
    }

    public static synchronized void update(String id, String category, String name, BigDecimal price) {
        List<Dish> items = all();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id().equals(id)) {
                items.set(i, new Dish(id, category, name, Money.round(price)));
            }
        }
        persist(items);
    }

    public static synchronized void delete(String id) {
        List<Dish> items = all();
        items.removeIf(d -> d.id().equals(id));
        persist(items);
    }

    private static void persist(List<Dish> items) {
        List<String> lines = new ArrayList<>();
        for (Dish d : items) {
            lines.add(Csv.join(d.id(), d.category(), d.name(), Money.plain(d.price())));
        }
        Storage.write(FILE, HEADER, lines);
    }

    private static List<Dish> defaults() {
        List<Dish> items = new ArrayList<>();
        items.add(new Dish("1", "Pasta", "Spaghetti", Money.of("80")));
        items.add(new Dish("2", "Pasta", "Penne", Money.of("85")));
        items.add(new Dish("3", "Pasta", "Fettuccine", Money.of("95")));
        items.add(new Dish("4", "Sauces", "Pesto", Money.of("25")));
        items.add(new Dish("5", "Sauces", "Alfredo", Money.of("30")));
        items.add(new Dish("6", "Sauces", "Bolognese", Money.of("35")));
        items.add(new Dish("7", "Sides", "Garlic Bread", Money.of("35")));
        items.add(new Dish("8", "Sides", "Caesar Salad", Money.of("45")));
        items.add(new Dish("9", "Desserts", "Tiramisu", Money.of("55")));
        items.add(new Dish("10", "Drinks", "Soft Drinks", Money.of("20")));
        items.add(new Dish("11", "Drinks", "Juice", Money.of("25")));
        items.add(new Dish("12", "Drinks", "Water", Money.of("10")));
        return items;
    }
}
