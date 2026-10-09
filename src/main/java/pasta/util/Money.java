package pasta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Money {

    private Money() {
    }

    public static BigDecimal of(String text) {
        return new BigDecimal(text.trim()).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }

    public static String plain(BigDecimal value) {
        return round(value).toPlainString();
    }

    public static String format(BigDecimal value) {
        return String.format(Locale.US, "%,.2f EGP", round(value));
    }
}
