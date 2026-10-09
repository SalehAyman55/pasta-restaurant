package pasta.model;

public record User(String username, String salt, String hash, Role role, String phone, String address, String created) {
}
