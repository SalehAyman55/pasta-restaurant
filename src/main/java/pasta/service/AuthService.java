package pasta.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import pasta.model.Role;
import pasta.model.User;
import pasta.util.Csv;
import pasta.util.Validator;

public final class AuthService {

    private static final String FILE = "users.csv";
    private static final String HEADER = "username,salt,hash,role,phone,address,created";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 30_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final byte[] DUMMY_SALT = new byte[16];
    private static final Map<String, Integer> FAILURES = new HashMap<>();
    private static final Map<String, Long> LOCKED = new HashMap<>();

    private AuthService() {
    }

    public static synchronized List<User> users() {
        List<User> out = new ArrayList<>();
        for (List<String> r : Storage.read(FILE)) {
            if (r.size() < 7) {
                continue;
            }
            try {
                out.add(new User(r.get(0), r.get(1), r.get(2), Role.valueOf(r.get(3)), r.get(4), r.get(5), r.get(6)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return out;
    }

    public static synchronized User register(String username, char[] password, String phone, String address) {
        String name = username == null ? "" : username.trim();
        if (!Validator.username(name)) {
            throw new IllegalArgumentException("Username must be 3-20 letters, digits or underscore.");
        }
        String normalizedPhone = Validator.normalizePhone(phone);
        if (!Validator.phone(normalizedPhone)) {
            throw new IllegalArgumentException("Enter a valid Egyptian mobile number.");
        }
        String where = address == null ? "" : address.trim();
        if (where.length() < 5) {
            throw new IllegalArgumentException("Address is too short.");
        }
        if (!Validator.passwordValid(password)) {
            throw new IllegalArgumentException("Password needs 8+ characters with letters and digits.");
        }
        List<User> existing = users();
        for (User u : existing) {
            if (u.username().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Username is already taken.");
            }
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        Role role = existing.isEmpty() ? Role.ADMIN : Role.CASHIER;
        User user = new User(name, encode(salt), encode(hash(password, salt)), role, normalizedPhone, where, LocalDateTime.now().withNano(0).toString());
        Storage.append(FILE, HEADER, Csv.join(user.username(), user.salt(), user.hash(), user.role().name(), user.phone(), user.address(), user.created()));
        return user;
    }

    public static synchronized User login(String username, char[] password) {
        String name = username == null ? "" : username.trim();
        String key = name.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        Long until = LOCKED.get(key);
        if (until != null && until > now) {
            throw new IllegalArgumentException("Too many attempts. Try again in " + ((until - now + 999) / 1000) + "s.");
        }
        User found = null;
        for (User u : users()) {
            if (u.username().equalsIgnoreCase(name)) {
                found = u;
                break;
            }
        }
        byte[] salt = found == null ? DUMMY_SALT : decode(found.salt());
        byte[] computed = hash(password, salt);
        boolean ok = found != null && MessageDigest.isEqual(computed, decode(found.hash()));
        if (!ok) {
            int count = FAILURES.merge(key, 1, Integer::sum);
            if (count >= MAX_ATTEMPTS) {
                LOCKED.put(key, now + LOCK_MILLIS);
                FAILURES.remove(key);
            }
            throw new IllegalArgumentException("Invalid username or password.");
        }
        FAILURES.remove(key);
        LOCKED.remove(key);
        return found;
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] decode(String text) {
        try {
            return Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            return new byte[0];
        }
    }
}
