public class User {
    private byte[] secretKey;
    private int deltaTime;
    private int counter;

    public User(byte[] secretKey, int deltaTime) {
        this.secretKey = secretKey;
        this.deltaTime = deltaTime;
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
}