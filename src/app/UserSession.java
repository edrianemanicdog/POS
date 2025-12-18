package app;

public class UserSession {
    private static String currentUserEmail;
    private static String currentUserRole;
    
    public static void setUser(String email, String role) {
        currentUserEmail = email;
        currentUserRole = role;
    }
    
    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }
    
    public static String getCurrentUserRole() {
        return currentUserRole;
    }
    
    public static void clear() {
        currentUserEmail = null;
        currentUserRole = null;
    }
}

