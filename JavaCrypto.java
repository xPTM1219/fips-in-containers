import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class JavaCrypto {
    public static void main(String[] args) throws Exception {
        String password = "mySecurePassword";
        String plaintext = "Secret message";

        // Generate salt
        byte[] salt = new byte[16];
        byte[] iv = new byte[16]; // AES block size (128 bits)
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        int iterations = 1000; // Iteration count for key derivation

        // Derive key with PBE
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256); // 256-bit key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithHMACSHA256AndAES_256");
        SecretKey secretKey = factory.generateSecret(keySpec);

        // Set up PBE cipher with PBEParameterSpec (salt and iterations)
        Cipher cipher = Cipher.getInstance("PBEWithHMACSHA256AndAES_256");
        PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, iterations, new IvParameterSpec(iv));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeSpec);

        // Encrypt
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        String encryptedBase64 = Base64.getEncoder().encodeToString(ciphertext);
        System.out.println("Encrypted (Base64): " + encryptedBase64);
        System.out.println("Salt (Base64): " + Base64.getEncoder().encodeToString(salt));
        System.out.println("IV (Base64): " + Base64.getEncoder().encodeToString(iv));

        // Decrypt
        cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeSpec);
        byte[] decrypted = cipher.doFinal(ciphertext);
        System.out.println("Decrypted: " + new String(decrypted, StandardCharsets.UTF_8));
    }
}
