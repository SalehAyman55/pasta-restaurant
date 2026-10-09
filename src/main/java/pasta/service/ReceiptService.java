package pasta.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import pasta.model.Order;
import pasta.model.OrderLine;
import pasta.util.Money;

public final class ReceiptService {

    private static final int WIDTH = 44;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);

    private ReceiptService() {
    }

    public static String format(Order o) {
        StringBuilder sb = new StringBuilder();
        String rule = "=".repeat(WIDTH);
        String dash = "-".repeat(WIDTH);
        sb.append(rule).append('\n');
        sb.append(center("PASTA RESTAURANT")).append('\n');
        sb.append(rule).append('\n');
        sb.append(pair("Receipt", o.id())).append('\n');
        sb.append(pair("Date", o.time().format(FORMAT))).append('\n');
        sb.append(pair("Cashier", o.cashier())).append('\n');
        sb.append(pair("Customer", o.customer())).append('\n');
        sb.append(pair("Phone", o.phone())).append('\n');
        sb.append(pair("Type", o.type())).append('\n');
        sb.append(pair("Payment", o.payment())).append('\n');
        if (!o.address().isBlank()) {
            sb.append(wrap("Address", o.address()));
        }
        sb.append(dash).append('\n');
        sb.append(String.format(Locale.US, "%-20s %3s %8s %9s%n", "Item", "Qty", "Price", "Total"));
        sb.append(dash).append('\n');
        for (OrderLine l : o.lines()) {
            String name = l.name().length() > 20 ? l.name().substring(0, 20) : l.name();
            sb.append(String.format(Locale.US, "%-20s %3d %8s %9s%n", name, l.qty(), Money.plain(l.price()), Money.plain(l.total())));
        }
        sb.append(dash).append('\n');
        sb.append(kv("Subtotal", Money.plain(o.subtotal()))).append('\n');
        if (o.discount().signum() > 0) {
            sb.append(kv("Discount (" + o.code() + ")", "-" + Money.plain(o.discount()))).append('\n');
        }
        sb.append(kv("Tax 14%", Money.plain(o.tax()))).append('\n');
        if (o.delivery().signum() > 0) {
            sb.append(kv("Delivery fee", Money.plain(o.delivery()))).append('\n');
        }
        sb.append(rule).append('\n');
        sb.append(kv("TOTAL (EGP)", Money.plain(o.total()))).append('\n');
        sb.append(rule).append('\n');
        sb.append(center("Thank you for dining with us!")).append('\n');
        return sb.toString();
    }

    public static void save(Order order, Path path) throws IOException {
        Files.writeString(path, format(order), StandardCharsets.UTF_8);
    }

    private static String center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(pad) + text;
    }

    private static String pair(String key, String value) {
        return String.format(Locale.US, "%-10s: %s", key, value);
    }

    private static String kv(String key, String value) {
        return key + " ".repeat(Math.max(1, WIDTH - key.length() - value.length())) + value;
    }

    private static String wrap(String key, String value) {
        StringBuilder sb = new StringBuilder();
        int chunk = WIDTH - 12;
        for (int i = 0; i < value.length(); i += chunk) {
            String part = value.substring(i, Math.min(value.length(), i + chunk));
            sb.append(i == 0 ? String.format(Locale.US, "%-10s: ", key) : " ".repeat(12)).append(part).append('\n');
        }
        return sb.toString();
    }
}
