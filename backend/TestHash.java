import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean match = encoder.matches("manager123", "$2a$10$16OkPWnzTJFt0CSCpraOruOVTpmm2M0s3Az419zbeVUP1UGOIXumq");
        System.out.println("Matches: " + match);
    }
}
