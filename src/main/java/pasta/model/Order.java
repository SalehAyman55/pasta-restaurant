package pasta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Order(
        String id,
        LocalDateTime time,
        String cashier,
        String customer,
        String phone,
        String address,
        String type,
        String payment,
        String code,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal delivery,
        BigDecimal total,
        List<OrderLine> lines) {
}
