package pasta.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pasta.model.OrderLine;
import pasta.model.Totals;
import pasta.util.Money;

public final class PricingService {

    public static final BigDecimal TAX_RATE = new BigDecimal("0.14");
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("25.00");
    public static final BigDecimal FREE_DELIVERY_FROM = new BigDecimal("300.00");
    private static final Map<String, Integer> CODES = Map.of("PASTA10", 10, "STUDENT15", 15);

    private PricingService() {
    }

    public static boolean validCode(String code) {
        return code != null && CODES.containsKey(code.trim().toUpperCase(Locale.ROOT));
    }

    public static int percent(String code) {
        return validCode(code) ? CODES.get(code.trim().toUpperCase(Locale.ROOT)) : 0;
    }

    public static Totals calculate(List<OrderLine> lines, String type, String code) {
        BigDecimal subtotal = Money.zero();
        for (OrderLine line : lines) {
            subtotal = subtotal.add(line.total());
        }
        BigDecimal discount = Money.round(subtotal.multiply(BigDecimal.valueOf(percent(code))).divide(BigDecimal.valueOf(100)));
        BigDecimal net = subtotal.subtract(discount);
        BigDecimal tax = Money.round(net.multiply(TAX_RATE));
        boolean charge = "Delivery".equals(type) && subtotal.signum() > 0 && net.compareTo(FREE_DELIVERY_FROM) < 0;
        BigDecimal delivery = charge ? DELIVERY_FEE : Money.zero();
        BigDecimal total = net.add(tax).add(delivery);
        return new Totals(subtotal, discount, tax, delivery, total);
    }
}
