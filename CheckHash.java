import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Matches 123456? " + encoder.matches("123456", "$2a$10$XURPShQNCsLjp1ESc2laoO49BPZfHiITj7rLzJq/3MqwI/p2U4Xz6"));
        System.out.println("Hash for 123456: " + encoder.encode("123456"));
    }
}
