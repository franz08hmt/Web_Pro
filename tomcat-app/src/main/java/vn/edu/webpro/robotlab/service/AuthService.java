package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;

public final class AuthService {
    private final UserDao users; private final PasswordService passwords;
    public AuthService(UserDao users, PasswordService passwords){this.users=users;this.passwords=passwords;}
    public User register(String fullName,String email,String password)throws SQLException{validate(fullName,email,password);if(users.findByEmail(email)!=null)throw new IllegalStateException("exists");return users.create(fullName.trim(),email,passwords.hash(password));}
    public User login(String email,String password)throws SQLException{validate(null,email,password);String hash=users.passwordHash(email);if(hash==null||!passwords.verify(password,hash))throw new IllegalArgumentException("credentials");return users.findByEmail(email);}
    private void validate(String fullName,String email,String password){if(fullName!=null&&(fullName.trim().length()<2||fullName.trim().length()>100))throw new IllegalArgumentException("validation");if(email==null||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||email.length()>254)throw new IllegalArgumentException("validation");if(password==null||password.length()<8||password.length()>72)throw new IllegalArgumentException("validation");}
}
