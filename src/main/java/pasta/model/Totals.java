package pasta.model;

import java.math.BigDecimal;

public record Totals(BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal delivery, BigDecimal total) {
}
