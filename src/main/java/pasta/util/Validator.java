package pasta.util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern PHONE = Pattern.compile("^01[0125][0-9]{8}$");

    private Validator() {
    }

    public static boolean username(String value) {
        return value != null && USERNAME.matcher(value).matches();
    }

    public static String normalizePhone(String value) {
        if (value == null) {
            return "";
        }
        String digits = value.replaceAll("[\\s\\-()]", "");
        if (digits.startsWith("+20")) {
            digits = "0" + digits.substring(3);
        } else if (digits.startsWith("0020")) {
            digits = "0" + digits.substring(4);
        } else if (digits.startsWith("20") && digits.length() == 12) {
            digits = "0" + digits.substring(2);
        } else if (digits.length() == 10 && digits.startsWith("1")) {
            digits = "0" + digits;
        }
        return digits;
    }

    public static boolean phone(String normalized) {
        return normalized != null && PHONE.matcher(normalized).matches();
    }

    public static boolean passwordValid(char[] password) {
        if (password == null || password.length < 8) {
            return false;
        }
        boolean letter = false;
        boolean digit = false;
        for (char c : password) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            }
        }
        return letter && digit;
    }

    public static int passwordScore(char[] password) {
        if (password == null || password.length == 0) {
            return 0;
        }
        boolean lower = false;
        boolean upper = false;
        boolean digit = false;
        boolean symbol = false;
        for (char c : password) {
            if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else {
                symbol = true;
            }
        }
        int score = 0;
        if (password.length >= 8) {
            score++;
        }
        if (password.length >= 12) {
            score++;
        }
        if (lower && upper) {
            score++;
        }
        if (digit) {
            score++;
        }
        if (symbol) {
            score++;
        }
        return score;
    }

    public static String passwordLabel(char[] password) {
        if (password == null || password.length == 0) {
            return " ";
        }
        int score = passwordScore(password);
        if (score <= 2) {
            return "Weak";
        }
        return score == 3 ? "Medium" : "Strong";
    }
}
