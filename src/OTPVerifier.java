import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class OTPVerifier {
    private static final byte[] key = "1234567890123456".getBytes(); // Same key as used in Android app
    public static Scanner scanner = new Scanner(System.in);

    // Hashmap for userids, keys, and delta-time
    private static Map<Integer, User> users = new HashMap<>();

    static {
        // Populate the users map
        for (int i = 0; i <= 255; i++) {
            byte[] secretKey = "1234567890123456".getBytes(); // Replace this with secret key generation logic later
            int deltaTime = 0;
            users.put(i, new User(secretKey, deltaTime));
        }
    }

    private static int uid; // User ID
    private static int enc; // Encoding type
    public static String otpToVerify;
    private static Cipher aes;
    private static int otpLength; // Length of the OTP
    private static int interval; // Interval in seconds
    private static String hashedOtp;
    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        // Example OTP and time to verify

        ServerSocket serverSocket = new ServerSocket(12345); // Listen on port 12345

        while (true) {
            Socket clientSocket = serverSocket.accept(); // Accept a client connection
            new Thread(() -> handleClient(clientSocket)).start(); // Handle the client in a new thread
        }

    }
    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String otpToVerify = in.readLine(); // Read the OTP from the client
            System.out.println("Received OTP: " + otpToVerify);

            long time = System.currentTimeMillis() / 1000; // Current time in seconds

            // use AES encryption with ECB mode and no padding --> will produce the same result as the Android app
            aes = Cipher.getInstance("AES/ECB/NoPadding");

            String[] otpComponents = otpToVerify.split("\\|");
            uid = Integer.parseInt(otpComponents[0]);

            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(users.get(uid).getSecretKey(), "AES"));

            if (verifyOTP(otpToVerify, time)) {

                // if  users.get(uid).getUsedOtps() contains more than 100 elements, remove the oldest element and add the new one (like a FIFO)
                if (users.get(uid).getUsedOtps().size() >= 100) {
                    users.get(uid).getUsedOtps().remove(0);
                }
                users.get(uid).getUsedOtps().add(otpToVerify); // Add the OTP to the list of used OTPs
                out.println("OTP is correct."); // Send the response to the client
                System.out.println("OTP is correct.");
            } else {
                out.println("OTP is incorrect."); // Send the response to the client
                System.out.println("OTP is incorrect.");
            }

            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean verifyOTP(String otp, long time) {

        if (users.get(uid).getUsedOtps().contains(otp)) {
            return false;
        }

        String[] otpComponents = otp.split("\\|");
        enc = Integer.parseInt(otpComponents[0]);
        uid = Integer.parseInt(otpComponents[1]);
        otpLength = Integer.parseInt(otpComponents[2]);
        interval = Integer.parseInt(otpComponents[3]);
        hashedOtp = otpComponents[4];

        interval = interval - users.get(uid).getDeltaTime();

        // Get the user data from the users hashmap
        User user = users.get(uid);

        for (int i = user.getDeltaTime(); i < interval; i++) {

            // positive delta-time
            String generatedOTP = generateOTP(time + i);
            if (otp.equals(generatedOTP)) {
                user.setDeltaTime((user.getDeltaTime()+i)/2);
                return true;
            }

            // negative delta-time
            generatedOTP = generateOTP(time - i);
            if (otp.equals(generatedOTP)) {
                user.setDeltaTime((user.getDeltaTime()+i)/2);
                return true;
            }
        }

        return false;
    }


    private static String generateOTP(long time) {
        // Create a ByteBuffer to store the OTP data
        ByteBuffer otpData = ByteBuffer.allocate(16);
        // Add the UID and the current time to the OTP data
        otpData.put((byte) uid);        // 1 byte
        otpData.put((byte)otpLength);   // 1 byte
        otpData.put((byte) interval);   // 1 byte
        otpData.putLong(time);           // 8 bytes
        // in sum: 1 + 1 + 1 + 8 = 11 bytes

        // Add padding to the OTP data (at least 5 bytes) --> done automatically by PKCS5Padding
        otpData.put(new byte[5]);       // 5 bytes


        byte[] otpBytes = otpData.array();
        try {
            byte[] encrypted = aes.doFinal(otpBytes);
            if (enc == 0) {
                // Use Base64 encoding
                String base64Otp = Base64.getEncoder().encodeToString(encrypted);

                return "0|"+uid+"|"+otpLength+"|"+interval+"|"+base64Otp.substring(base64Otp.length() - otpLength-1);

            } else {
                // Use hexadecimal encoding
                StringBuilder hexString = new StringBuilder();
                for (int i = encrypted.length - 1; i >= encrypted.length - otpLength; i--) {
                    hexString.append(Integer.toHexString(0xFF & encrypted[i]));
                }
                hexString.reverse();

                hexString.insert(0, "|");
                hexString.insert(0, interval);
                hexString.insert(0, "|");
                hexString.insert(0, otpLength);
                hexString.insert(0, "|");
                hexString.insert(0, uid);
                hexString.insert(0, "1|");


                // String format: uid|otpLength|interval|otp

//                    for (byte b : encrypted) {
//                        hexString.append(Integer.toHexString(0xFF & b));
//                    }
                String otpString = hexString.toString();
                return otpString;
            }

            // TODO: Implement Base58 encoding istead of Base64
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
