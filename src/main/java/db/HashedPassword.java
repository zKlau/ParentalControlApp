package db;

import java.util.Arrays;

/**
 * The {@code HashedPassword} class encapsulates a password hash and its associated salt.
 * It provides utility methods for converting between the object and its byte array representation,
 * which is useful for secure storage and retrieval from a database.
 * <p>
 * Instances of this class are immutable.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class HashedPassword {
    /**
     * The salt used for hashing the password.
     */
    private final byte[] salt;

    /**
     * The hashed password.
     */
    private final byte[] hash;

    /**
     * Constructs a new {@code HashedPassword} with the specified salt and hash.
     *
     * @param salt The salt used for hashing.
     * @param hash The hashed password.
     */
    public HashedPassword(byte[] salt, byte[] hash) {
        this.salt = salt;
        this.hash = hash;
    }

    /**
     * Returns the salt used for hashing.
     *
     * @return The salt as a byte array.
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Returns the hashed password.
     *
     * @return The hash as a byte array.
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Combines the salt and hash into a single byte array for storage.
     *
     * @return A byte array containing the salt followed by the hash.
     */
    public byte[] toBytes() {
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);
        return combined;
    }

    /**
     * Creates a {@code HashedPassword} object from a combined byte array.
     * Assumes the first 16 bytes are the salt and the remainder is the hash.
     *
     * @param combined The combined salt and hash byte array.
     * @return A new {@code HashedPassword} instance.
     */
    public static HashedPassword fromBytes(byte[] combined) {
        byte[] salt = Arrays.copyOfRange(combined, 0, 16);
        byte[] hash = Arrays.copyOfRange(combined, 16, combined.length);
        return new HashedPassword(salt, hash);
    }
}
