# Enabling Java 8 algorithms with FIPS enabled 

To enable the `PBKDF2WithHmacSHA512` algorithm in Java 8 when FIPS mode is enabled, you need to modify the `java.security` file to explicitly allow this algorithm, as FIPS mode restricts the available cryptographic algorithms to those compliant with FIPS 140-2 standards. In Java 8, `PBKDF2WithHmacSHA512` is supported by the SunJCE provider but may be disabled in FIPS mode due to stricter algorithm restrictions. Since you don’t want to use Bouncy Castle and need to work within the `java.security` file, I’ll guide you through enabling `PBKDF2WithHmacSHA512` by configuring the security providers and algorithm settings.

## Prompt

How to enable specific algorithms in java.security in Java 8?
I don't want to add bouncy castle, I would like to enable PBKDF2WithHmacSHA512.
It needs to be done with the java.security file because when you enable FIPS, Java 8 specifically, disables a lot of algorithms, leaving PBKDF2WithHmacSHA512 out.

## Background

- **FIPS Mode in Java 8**: When FIPS mode is enabled (e.g., by setting `com.sun.security.enableFIPS=true` or using a FIPS-compliant security provider), Java restricts cryptographic algorithms to those approved by FIPS 140-2. `PBKDF2WithHmacSHA512` is not FIPS-approved (FIPS allows `PBKDF2` with SHA-1 or SHA-256, but SHA-512 may be excluded in strict FIPS configurations).
- **Java Security File**: The `java.security` file (typically located in `$JAVA_HOME/jre/lib/security/java.security` for Java 8) controls security providers and algorithm settings.
- **SunJCE Provider**: In Java 8, `PBKDF2WithHmacSHA512` is provided by the `SunJCE` provider, but FIPS mode may disable it unless explicitly allowed or reconfigured.
- **Goal**: Modify `java.security` to ensure `PBKDF2WithHmacSHA512` is available, even in FIPS mode, by adjusting provider settings or overriding algorithm restrictions.

## Steps to Enable PBKDF2WithHmacSHA512 in `java.security`

1. **Locate the `java.security` File**:
   - Find the file in your Java 8 installation:
     - On Linux/Unix: `$JAVA_HOME/jre/lib/security/java.security`
     - On Windows: `%JAVA_HOME%\jre\lib\security\java.security`
   - For example, if Java 8 is installed at `/usr/lib/jvm/java-8-openjdk`, the file is at `/usr/lib/jvm/java-8-openjdk/jre/lib/security/java.security`.
   - Make a backup before editing:
     ```bash
     sudo cp $JAVA_HOME/jre/lib/security/java.security $JAVA_HOME/jre/lib/security/java.security.bak
     ```

2. **Edit the `java.security` File**:
   - Open the file with a text editor (e.g., `sudo nano $JAVA_HOME/jre/lib/security/java.security`).
   - Ensure the `SunJCE` provider is enabled and prioritized for `PBKDF2` algorithms. The `SunJCE` provider supports `PBKDF2WithHmacSHA512` in Java 8.

3. **Check Security Providers**:
   - Look for the `security.provider.X` lines in `java.security`. By default, Java 8 includes:
     ```
     security.provider.1=sun.security.provider.Sun
     security.provider.2=sun.security.rsa.SunRsaSign
     security.provider.3=com.sun.net.ssl.internal.ssl.Provider
     security.provider.4=com.sun.crypto.provider.SunJCE
     ...
     ```
   - Ensure `com.sun.crypto.provider.SunJCE` (SunJCE) is listed and not commented out. It’s typically `security.provider.4`.
   - If you’re using a FIPS-compliant provider (e.g., `SunPKCS11` with NSS for FIPS mode), it may take precedence and restrict algorithms. You may need to adjust its position or configuration.

4. **Disable or Adjust FIPS Restrictions**:
   - In FIPS mode, the `SunJCE` provider may be restricted to FIPS-approved algorithms. To enable `PBKDF2WithHmacSHA512`, you need to ensure the `SunJCE` provider is used without FIPS restrictions for this algorithm.
   - Add or modify the following property to explicitly allow `PBKDF2WithHmacSHA512`:
     ```
     jdk.tls.disabledAlgorithms=
     ```
     Or, if other algorithms are listed, ensure `PBKDF2WithHmacSHA512` is not disabled. By default, Java 8’s `java.security` may include a `disabledAlgorithms` line like:
     ```
     jdk.tls.disabledAlgorithms=SSLv3, RC4, MD5withRSA, DH keySize < 768
     ```
     Ensure `PBKDF2WithHmacSHA512` is not listed here. If it is, remove it from the list.
   - Alternatively, if FIPS mode is enforced via a `SunPKCS11` provider, you may need to configure the PKCS11 provider to allow non-FIPS algorithms or prioritize `SunJCE` for `PBKDF2`.

5. **Configure SunJCE for PBKDF2**:
   - The `SunJCE` provider supports `PBKDF2WithHmacSHA512`. Ensure it’s not overridden by a FIPS provider. If you see a `SunPKCS11` provider configured for FIPS (e.g., `security.provider.X=sun.security.pkcs11.SunPKCS11 ${java.home}/lib/security/nss.cfg`), move `SunJCE` higher in the provider list (e.g., change `security.provider.4` to `security.provider.1`):
     ```
     security.provider.1=com.sun.crypto.provider.SunJCE
     security.provider.2=sun.security.provider.Sun
     security.provider.3=sun.security.rsa.SunRsaSign
     security.provider.4=com.sun.net.ssl.internal.ssl.Provider
     security.provider.5=sun.security.pkcs11.SunPKCS11 ${java.home}/lib/security/nss.cfg
     ```
   - This ensures `SunJCE` is checked first for cryptographic operations.

6. **Disable FIPS Mode (if Possible)**:
   - If your application doesn’t strictly require FIPS compliance, disable FIPS mode to allow `PBKDF2WithHmacSHA512` without restrictions:
     - Find the line:
       ```
       com.sun.security.enableFIPS=true
       ```
     - Change it to:
       ```
       com.sun.security.enableFIPS=false
       ```
     - Or comment it out:
       ```
       #com.sun.security.enableFIPS=true
       ```
   - Disabling FIPS mode allows `SunJCE` to use `PBKDF2WithHmacSHA512` without restrictions, as it’s supported in Java 8.

7. **Save and Test**:
   - Save the `java.security` file.
   - Test with a simple Java program to verify `PBKDF2WithHmacSHA512` works:
     ```java
     import javax.crypto.SecretKeyFactory;
     import javax.crypto.spec.PBEKeySpec;

     public class TestPBKDF2 {
         public static void main(String[] args) throws Exception {
             char[] password = "testpassword".toCharArray();
             byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
             int iterations = 1000;
             int keyLength = 256;
             PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
             SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
             byte[] key = skf.generateSecret(spec).getEncoded();
             System.out.println("PBKDF2WithHmacSHA512 key generated successfully");
         }
     }
     ```
   - Compile and run:
     ```bash
     javac TestPBKDF2.java
     java TestPBKDF2
     ```
   - Expected output: `PBKDF2WithHmacSHA512 key generated successfully`.

## Notes

- **FIPS Mode Limitation**: If FIPS mode is strictly required, `PBKDF2WithHmacSHA512` may not be usable, as it’s not FIPS 140-2 approved. FIPS allows `PBKDF2` with SHA-1 or SHA-256 (e.g., `PBKDF2WithHmacSHA1`). If you must use `PBKDF2WithHmacSHA512`, disabling FIPS mode (step 6) is the most reliable option without adding providers like Bouncy Castle.
- **SunJCE Support**: Java 8’s `SunJCE` provider supports `PBKDF2WithHmacSHA512` in non-FIPS mode. Prioritizing `SunJCE` (step 5) should suffice if FIPS restrictions are relaxed.
- **Security Providers Order**: Moving `SunJCE` higher in the provider list ensures it’s used for `PBKDF2` algorithms before a FIPS-compliant provider like `SunPKCS11`.
- **File Permissions**: Ensure the `java.security` file is writable by your user or use `sudo` to edit it.
- **Java 8 Specific**: These instructions are tailored for Java 8. Java 17/21 may handle FIPS differently, but your question specifies Java 8.

## Troubleshooting

- **Algorithm Still Unavailable**:
  - If `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")` throws `NoSuchAlgorithmException`, verify `SunJCE` is in the provider list and not overridden:
    ```bash
    java -XshowSettings:properties -version
    ```
    Check the `java.security` properties loaded.
  - Ensure FIPS mode is disabled (`com.sun.security.enableFIPS=false`) or `SunPKCS11` is not restricting algorithms.
- **Provider Conflict**:
  - If a `SunPKCS11` provider is enforcing FIPS, temporarily comment it out in `java.security` (e.g., `#security.provider.5=sun.security.pkcs11.SunPKCS11 ...`) and test again.
- **File Not Found**: Confirm the `java.security` file path matches your Java 8 installation. Check:
  ```bash
  find / -name java.security 2>/dev/null
  ```
- **Test Failure**: If the test program fails, share the error output for further diagnosis.

If you encounter errors or need help with a specific FIPS setup (e.g., NSS configuration for `SunPKCS11`), share the error message or your `java.security` provider list, and I’ll provide a tailored fix. Let me know how it goes!

## Continuation

### Propmt

Sorry to go back, but to continue the conversation of "How to enable specific algorithms in java.security in Java 8?". I found today that if I enable the line `fips.provider.4=com.sun.crypto.provider.SunJCE`in `$JAVA_HOME/jre/lib/security/java.security`, it brings back the the missing and needed algorithms. THat is fine but I would like to know how to disable some of them, since there are a couple that should not be there.
I tried to use `jdk.tls.disabledAlgorithms=PBKDF2WithHmacSHA1` for example but it did not worked. Basically, I want to disable most of them but leave PBKDF2WithHmacSHA512 and PBKDF2WithHmacSHA256 since is the algorithms that are going to be used.

## Info

To disable specific algorithms in Java 8’s `java.security` file while keeping `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256` enabled, especially after enabling the `SunJCE` provider in FIPS mode with `fips.provider.4=com.sun.crypto.provider.SunJCE`, you need to correctly configure the `jdk.tls.disabledAlgorithms` or related properties. The issue you’re facing—where `jdk.tls.disabledAlgorithms=PBKDF2WithHmacSHA1` or `jdk.jar.disabledAlgorithms=PBKDF2WithHmacSHA1` didn’t work—likely stems from using the wrong property or incorrect syntax for disabling algorithms in the `SecretKeyFactory` context, as `jdk.tls.disabledAlgorithms` primarily affects TLS protocols, and `jdk.jar.disabledAlgorithms` is for JAR signing, not cryptographic algorithms like PBKDF2.

Since `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256` are supported by the `SunJCE` provider in Java 8 (but may be restricted in FIPS mode), and you’ve enabled `SunJCE` in the FIPS provider list, you can disable unwanted algorithms by configuring the `jdk.security.disabledAlgorithms` property (or provider-specific settings) in the `java.security` file. Below, I’ll provide a solution to disable all `SecretKeyFactory` algorithms except `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256`, focusing on the correct property and syntax.

### Why Your Attempts Didn’t Work
- **`jdk.tls.disabledAlgorithms`**: This property controls algorithms used in TLS/SSL protocols (e.g., ciphers, key exchange mechanisms). It doesn’t apply to `SecretKeyFactory` algorithms like `PBKDF2WithHmacSHA1`. That’s why setting `jdk.tls.disabledAlgorithms=PBKDF2WithHmacSHA1` had no effect.
- **`jdk.jar.disabledAlgorithms`**: This property restricts algorithms for JAR signing/verification (e.g., MD5, SHA1 for signatures). It’s unrelated to `SecretKeyFactory` or PBKDF2 algorithms.
- **FIPS Mode**: Enabling `fips.provider.4=com.sun.crypto.provider.SunJCE` adds `SunJCE` to the FIPS provider list, but FIPS mode (if active) may still restrict algorithms to FIPS 140-2 compliant ones (e.g., `PBKDF2WithHmacSHA1` is allowed, but `PBKDF2WithHmacSHA512` may not be unless explicitly permitted). You need to target `SecretKeyFactory` algorithms specifically.

### Solution: Disable Unwanted Algorithms in `java.security`

To disable all `SecretKeyFactory` algorithms except `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256`, you can use the `security.provider.X.disabledAlgorithms` property to restrict algorithms for the `SunJCE` provider or adjust the FIPS configuration. Since Java 8 doesn’t directly support disabling specific `SecretKeyFactory` algorithms via a global `disabledAlgorithms` property for all providers, we’ll focus on provider-specific restrictions and ensure FIPS mode doesn’t interfere.

#### Steps

1. **Locate the `java.security` File**:
   - Path: `$JAVA_HOME/jre/lib/security/java.security` (e.g., `/usr/lib/jvm/java-8-openjdk/jre/lib/security/java.security` on Linux).
   - Back up the file:
     ```bash
     sudo cp $JAVA_HOME/jre/lib/security/java.security $JAVA_HOME/jre/lib/security/java.security.bak
     ```

2. **Verify FIPS Mode and Provider Configuration**:
   - Since you enabled `fips.provider.4=com.sun.crypto.provider.SunJCE`, the `SunJCE` provider is active in FIPS mode, allowing `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256` (as you confirmed).
   - Check if FIPS mode is enabled:
     ```
     com.sun.security.enableFIPS=true
     ```
   - If FIPS mode is enabled, `SunJCE` operates in a restricted mode, and only FIPS-compliant algorithms (e.g., `PBKDF2WithHmacSHA1`) are allowed unless overridden.

3. **Disable Unwanted Algorithms**:
   - Java 8 doesn’t support a direct `disabledAlgorithms` property for `SecretKeyFactory` algorithms globally, but you can restrict algorithms at the provider level or programmatically verify usage.
   - Since `SunJCE` supports `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256`, `PBKDF2WithHmacSHA384`, and `PBKDF2WithHmacSHA512` (among others), you can’t directly disable specific algorithms in `java.security` for `SecretKeyFactory` without affecting other providers or services. Instead, you can:
     - **Option 1**: Use a custom security provider order to prioritize `SunJCE` and limit its algorithms programmatically.
     - **Option 2**: Disable FIPS mode to allow all `SunJCE` algorithms and enforce restrictions in your application code.
     - **Option 3**: Configure `jdk.tls.disabledAlgorithms` or `jdk.certpath.disabledAlgorithms` for unrelated services (e.g., TLS, certificates) but rely on code-level checks for `SecretKeyFactory`.

   **Recommended Approach**: Since `java.security` doesn’t support fine-grained disabling of `SecretKeyFactory` algorithms, disable FIPS mode to allow all `SunJCE` algorithms and enforce the use of only `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256` in your application code. Alternatively, if FIPS mode is required, use a custom provider configuration.

4. **Modify `java.security`**:
   - Open `$JAVA_HOME/jre/lib/security/java.security` with a text editor (e.g., `sudo nano $JAVA_HOME/jre/lib/security/java.security`).
   - **Disable FIPS Mode (if acceptable)**:
     - Find:
       ```
       com.sun.security.enableFIPS=true
       ```
     - Change to:
       ```
       com.sun.security.enableFIPS=false
       ```
     - This allows `SunJCE` to use all its algorithms, including `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256`, without FIPS restrictions.
   - **Ensure `SunJCE` is Prioritized**:
     - Verify or add `SunJCE` in the provider list:
       ```
       security.provider.1=com.sun.crypto.provider.SunJCE
       security.provider.2=sun.security.provider.Sun
       security.provider.3=sun.security.rsa.SunRsaSign
       security.provider.4=com.sun.net.ssl.internal.ssl.Provider
       ```
     - If `SunPKCS11` is present (e.g., for NSS in FIPS mode), move it lower (e.g., `security.provider.5`) to prioritize `SunJCE`.
   - **Avoid Disabling Unrelated Algorithms**:
     - Check `jdk.tls.disabledAlgorithms` and `jdk.certpath.disabledAlgorithms` to ensure they don’t include `PBKDF2WithHmacSHA1` or other PBKDF2 variants. For example:
       ```
       jdk.tls.disabledAlgorithms=SSLv3, RC4, MD5withRSA, DH keySize < 768
       ```
       - Remove any PBKDF2-related entries if present (they’re unlikely to be there by default).

5. **Enforce Algorithm Restrictions in Code**:
   - Since `java.security` can’t directly disable `PBKDF2WithHmacSHA1` for `SecretKeyFactory`, enforce the use of `PBKDF2WithHmacSHA512` or `PBKDF2WithHmacSHA256` in your application code. Example:
     ```java
     import javax.crypto.SecretKeyFactory;
     import javax.crypto.spec.PBEKeySpec;

     public class TestPBKDF2 {
         public static void main(String[] args) throws Exception {
             String algorithm = "PBKDF2WithHmacSHA512"; // or "PBKDF2WithHmacSHA256"
             char[] password = "testpassword".toCharArray();
             byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
             int iterations = 1000;
             int keyLength = 256;
             PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
             SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
             byte[] key = skf.generateSecret(spec).getEncoded();
             System.out.println(algorithm + " key generated successfully");
         }
     }
     ```
   - Compile and run:
     ```bash
     javac TestPBKDF2.java
     java TestPBKDF2
     ```
   - Explicitly use only `PBKDF2WithHmacSHA512` or `PBKDF2WithHmacSHA256` in your code to avoid `PBKDF2WithHmacSHA1`.

6. **Optional: Restrict Algorithms Programmatically**:
   - If you need to ensure other algorithms (e.g., `PBKDF2WithHmacSHA1`) are not used, wrap `SecretKeyFactory.getInstance` in a check:
     ```java
     public class SecurePBKDF2 {
         private static final String[] ALLOWED_ALGORITHMS = {
             "PBKDF2WithHmacSHA512",
             "PBKDF2WithHmacSHA256"
         };

         public static SecretKeyFactory getSecretKeyFactory(String algorithm) throws Exception {
             for (String allowed : ALLOWED_ALGORITHMS) {
                 if (allowed.equals(algorithm)) {
                     return SecretKeyFactory.getInstance(algorithm);
                 }
             }
             throw new IllegalArgumentException("Algorithm " + algorithm + " is not allowed");
         }

         public static void main(String[] args) throws Exception {
             String algorithm = "PBKDF2WithHmacSHA512";
             char[] password = "testpassword".toCharArray();
             byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
             int iterations = 1000;
             int keyLength = 256;
             PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
             SecretKeyFactory skf = getSecretKeyFactory(algorithm);
             byte[] key = skf.generateSecret(spec).getEncoded();
             System.out.println(algorithm + " key generated successfully");

             // This will fail
             try {
                 getSecretKeyFactory("PBKDF2WithHmacSHA1");
             } catch (IllegalArgumentException e) {
                 System.out.println(e.getMessage());
             }
         }
     }
     ```
   - This ensures only allowed algorithms are used, throwing an exception for others like `PBKDF2WithHmacSHA1`.

7. **Test the Configuration**:
   - Test with the above code to verify `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256` work:
     ```bash
     javac SecurePBKDF2.java
     java SecurePBKDF2
     ```
   - Expected output:
     ```
     PBKDF2WithHmacSHA512 key generated successfully
     Algorithm PBKDF2WithHmacSHA1 is not allowed
     ```
   - If `PBKDF2WithHmacSHA1` is still accessible, it’s because `SunJCE` supports it, and FIPS mode or `java.security` isn’t restricting it.

8. **If FIPS Mode Must Remain Enabled**:
   - If `com.sun.security.enableFIPS=true` is required, `SunJCE` in FIPS mode may still allow `PBKDF2WithHmacSHA1` (FIPS-compliant) but restrict `PBKDF2WithHmacSHA512` unless explicitly enabled. Since you’ve confirmed `fips.provider.4=com.sun.crypto.provider.SunJCE` enables `PBKDF2WithHmacSHA512`, you can try specifying a custom `SunPKCS11` configuration to exclude `PBKDF2WithHmacSHA1`:
     - Create a `nss.cfg` file (e.g., `$JAVA_HOME/jre/lib/security/nss.cfg`):
       ```
       name = NSS
       nssLibraryDirectory = /usr/lib64
       nssDbMode = noDb
       attributes = compatibility
       disabledMechanisms = {
           CKM_SHA_1_HMAC
       }
       ```
     - Update `java.security` to use this:
       ```
       security.provider.5=sun.security.pkcs11.SunPKCS11 ${java.home}/lib/security/nss.cfg
       ```
     - This disables SHA-1-based mechanisms (like `PBKDF2WithHmacSHA1`) in the `SunPKCS11` provider, but `SunJCE` will still handle `PBKDF2WithHmacSHA512` and `PBKDF2WithHmacSHA256`.
   - Test again with the `SecurePBKDF2` code.

### Notes
- **Why `jdk.tls.disabledAlgorithms` Failed**: It’s specific to TLS/SSL (e.g., ciphers, key exchange). `SecretKeyFactory` algorithms like PBKDF2 are managed by providers (`SunJCE`), not TLS settings.[](https://www.ibm.com/docs/en/sdk-java-technology/8?topic=customization-disabled-restricted-cryptographic-algorithms)
- **Why `jdk.jar.disabledAlgorithms` Failed**: It applies to JAR signing, not cryptographic algorithms like PBKDF2.[](https://stackoverflow.com/questions/65689605/removing-specific-algorithms-from-java-security-providers)
- **FIPS Mode**: In FIPS mode, `SunJCE` restricts to FIPS-compliant algorithms, but your addition of `fips.provider.4=com.sun.crypto.provider.SunJCE` bypasses some restrictions, enabling `PBKDF2WithHmacSHA512`. Disabling FIPS mode is simpler if compliance isn’t mandatory.[](https://stackoverflow.com/questions/47392965/pbkdf2withhmacsha512-secretkeyfactory-not-available)
- **Programmatic Enforcement**: Since `java.security` doesn’t offer fine-grained `SecretKeyFactory` algorithm disabling, enforcing restrictions in code (step 6) is the most reliable way to prevent `PBKDF2WithHmacSHA1` usage.
- **SunJCE Algorithms**: In Java 8, `SunJCE` supports `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256`, `PBKDF2WithHmacSHA384`, and `PBKDF2WithHmacSHA512`. You can’t disable `PBKDF2WithHmacSHA1` directly in `java.security` without affecting other providers, so code-level checks are key.[](https://stackoverflow.com/questions/19348501/pbkdf2withhmacsha512-vs-pbkdf2withhmacsha1)[](https://stackoverflow.com/questions/47392965/pbkdf2withhmacsha512-secretkeyfactory-not-available)
- **Security**: `PBKDF2WithHmacSHA1` is weaker due to SHA-1’s theoretical vulnerabilities. Using `PBKDF2WithHmacSHA512` or `PBKDF2WithHmacSHA256` is preferred for stronger security.[](https://crypto.stackexchange.com/questions/11016/pbkdf2withhmacsha512-vs-pbkdf2withhmacsha1)

### Troubleshooting
- **Algorithm Still Available**:
  - If `PBKDF2WithHmacSHA1` is still usable, it’s because `SunJCE` supports it natively. Use the `SecurePBKDF2` code to enforce restrictions.
  - Check the provider list:
    ```java
    import java.security.Security;
    public class ListProviders {
        public static void main(String[] args) {
            for (java.security.Provider p : Security.getProviders()) {
                System.out.println(p.getName());
            }
        }
    }
    ```
    Ensure `SunJCE` is listed and prioritized.
- **NoSuchAlgorithmException**:
  - If `PBKDF2WithHmacSHA512` fails, verify `fips.provider.4=com.sun.crypto.provider.SunJCE` is set and FIPS mode is disabled.
  - Run with `-Djava.security.debug=provider` to debug provider loading:
    ```bash
    java -Djava.security.debug=provider SecurePBKDF2
    ```
- **FIPS Conflicts**:
  - If FIPS mode is required, test the `SunPKCS11` configuration with `nss.cfg` to disable SHA-1 mechanisms.
  - Share the error output if `PBKDF2WithHmacSHA512` fails.

If you need to keep FIPS mode enabled or encounter specific errors (e.g., `NoSuchAlgorithmException` or `PBKDF2WithHmacSHA1` still being used), share the error message or your current `java.security` file’s provider section, and I’ll refine the solution further!

