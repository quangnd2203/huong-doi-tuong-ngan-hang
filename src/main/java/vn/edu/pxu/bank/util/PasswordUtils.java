package vn.edu.pxu.bank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/** Băm mật khẩu cho demo desktop, không lưu mật khẩu gốc trong JSON. */
public final class PasswordUtils {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TEMP_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private PasswordUtils() {
    }

    /** Sinh mật khẩu tạm ngẫu nhiên cho nhân viên cấp cho khách, không lưu dạng rõ. */
    public static char[] temporaryPassword() {
        char[] password = new char[12];
        for (int i = 0; i < password.length; i++) {
            password[i] = TEMP_ALPHABET.charAt(RANDOM.nextInt(TEMP_ALPHABET.length()));
        }
        return password;
    }

    public static String hash(char[] password) {
        Objects.requireNonNull(password);
        if (password.length == 0) throw new IllegalArgumentException("Mật khẩu không được trống");
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2$" + ITERATIONS + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(derived);
    }

    public static boolean verify(char[] password, String encoded) {
        Objects.requireNonNull(password);
        if (encoded == null) return false;
        String[] parts = encoded.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1 || iterations > 1_000_000) return false;
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] salt = decoder.decode(parts[2]);
            byte[] expected = decoder.decode(parts[3]);
            if (salt.length != 16 || expected.length != KEY_BITS / 8) return false;
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể xử lý mật khẩu", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
