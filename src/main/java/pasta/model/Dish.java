package pasta.model;

import java.math.BigDecimal;

public record Dish(String id, String category, String name, BigDecimal price) {
}
