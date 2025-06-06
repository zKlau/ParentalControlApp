package db;

import java.util.Arrays;

public class HashedPassword {
    private final byte[] salt;
    private final byte[] hash;

    public HashedPassword(byte[] salt, byte[] hash) {
        this.salt = salt;
        this.hash = hash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] toBytes() {
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);
        return combined;
    }

    public static HashedPassword fromBytes(byte[] combined) {
        byte[] salt = Arrays.copyOfRange(combined, 0, 16);
        byte[] hash = Arrays.copyOfRange(combined, 16, combined.length);
        return new HashedPassword(salt, hash);
    }
}
