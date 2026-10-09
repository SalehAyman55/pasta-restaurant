package pasta.model;

import java.math.BigDecimal;
import pasta.util.Money;

public record OrderLine(String name, String category, BigDecimal price, int qty) {

    public BigDecimal total() {
        return Money.round(price.multiply(BigDecimal.valueOf(qty)));
    }
}
