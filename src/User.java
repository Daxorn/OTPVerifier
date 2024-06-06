import java.util.ArrayList;

public class User {
    private byte[] secretKey;
    private int deltaTime;
    private ArrayList<String> usedOtps;

    public User(byte[] secretKey, int deltaTime) {
        this.secretKey = secretKey;
        this.deltaTime = deltaTime;
        usedOtps = new ArrayList<>(30);
    }

    // getters and setters
    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public int getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(int deltaTime) {
        this.deltaTime = deltaTime;
    }

    public ArrayList<String> getUsedOtps() {
        return usedOtps;
    }
}