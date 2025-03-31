package space.typro.typicallauncher.models;


import space.typro.typicallauncher.utils.PasswordHasher;

public class Password {
    private final String password;

    public Password(String password) {
        this.password = PasswordHasher.hashPassword(password);
    }

    @Override
    public String toString() {
        return password;
    }
}
