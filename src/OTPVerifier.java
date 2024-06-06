import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class OTPVerifier {
    private static final byte[] key = "1234567890123456".getBytes(); // Same key as used in Android app
    public Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        // Example OTP and time to verify

        long time = System.currentTimeMillis() / 1000; // Current time in seconds

        if (verifyOTP(otpToVerify, time)) {
            System.out.println("OTP is correct.");
        } else {
            System.out.println("OTP is incorrect.");
        }
    }

    private static boolean verifyOTP(String otp, long time) {
        String generatedOTP = generateOTP(time);
        return otp.equals(generatedOTP);
    }

    private static String generateOTP(long time) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] timeBytes = ByteBuffer.allocate(8).putLong(time).array();
            byte[] encrypted = cipher.doFinal(timeBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : encrypted) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
