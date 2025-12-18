package dao;

import java.util.Optional;

public interface UserDAO {
    boolean createUser(String email, String password);
    boolean authenticate(String email, String password);
    boolean userExists(String email);
    Optional<String> getUserEmail(String email);
}

