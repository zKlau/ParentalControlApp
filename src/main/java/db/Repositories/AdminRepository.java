package db.Repositories;

import db.Database;
import db.HashedPassword;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

public class AdminRepository {
    private Database db;

    public AdminRepository(Database database) {
        this.db = database;
    }

    /**
     * Checks the admin password against the stored hash, or sets it if not present.
     *
     * @param pass The password to check or set.
     * @return {@code true} if the password is correct or was set, {@code false} otherwise.
     */
    public boolean checkPassword(String pass) {
        try (PreparedStatement stmt = db.getCon().prepareStatement("SELECT * FROM ADMIN")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedEncoded = rs.getString("PASSWORD");
                byte[] combined = Base64.getDecoder().decode(storedEncoded);
                HashedPassword stored = HashedPassword.fromBytes(combined);
                KeySpec spec = new PBEKeySpec(pass.toCharArray(), stored.getSalt(), 65536, 128);
                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] computedHash = f.generateSecret(spec).getEncoded();
                return Arrays.equals(stored.getHash(), computedHash);
            } else {
                addPassword(pass);
                return true;
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hashes a password using PBKDF2 with a random salt.
     *
     * @param pass The password to hash.
     * @return A {@link HashedPassword} object containing the salt and hash.
     */
    public HashedPassword hashPassword(String pass) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();

            return new HashedPassword(salt, hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new admin password to the database, securely hashed.
     * This operation is performed asynchronously.
     *
     * @param pass The password to add.
     */
    public synchronized void addPassword(String pass) {
        db.executeDatabaseTask(() -> {
            try (PreparedStatement stmt = db.getCon().prepareStatement("INSERT INTO ADMIN(PASSWORD) VALUES(?)")) {
                HashedPassword hp = hashPassword(pass);
                String encoded = Base64.getEncoder().encodeToString(hp.toBytes());
                stmt.setString(1, encoded);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}

