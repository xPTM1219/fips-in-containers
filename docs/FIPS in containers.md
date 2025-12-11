# FIPS in containers

## Description

The Federal Information Processing Standard (FIPS) 140, which governs cryptographic modules, is enforced at the kernel level on your Rocky Linux 8 host when FIPS mode is enabled. Since Docker containers share the host kernel, the kernel's FIPS restrictions (e.g., disallowing non-approved algorithms like MD5 for certain operations) apply to processes running inside the container. This means that if your application attempts to use non-FIPS-compliant cryptography, it may fail at runtime due to kernel enforcement, providing some baseline protection against using insecure algorithms. However, this does not make your application fully "safe" or compliant in terms of FIPS standards simply because the host is FIPS-enabled.

## Requirements

For full FIPS compliance within the container:
- The container's userspace components (e.g., libraries like OpenSSL) must be configured to operate in FIPS mode, using only validated modules and approved algorithms. If your Docker image is based on a distribution without FIPS-enabled packages or policies (e.g., a standard non-FIPS base image), its crypto libraries may not automatically enter FIPS mode or could attempt self-tests/workloads that fail under the host's kernel restrictions.
- Compliance also requires the entire stack—including the container's cryptographic modules—to be validated (e.g., via FIPS 140-2 or 140-3 certifications), which isn't guaranteed by host enforcement alone. Running a non-FIPS image on a FIPS host might lead to runtime errors (as seen in cases like self-test failures) or incomplete adherence to FIPS requirements.

## Preparing a container

To achieve proper FIPS safety for your application, you should enable FIPS within the Docker image itself. For a Rocky Linux 8-based image (or similar RHEL 8 clone), this typically involves:
1. Ensuring the host is FIPS-enabled (which it already is).
2. Building or modifying the image to include FIPS-capable packages (e.g., install `crypto-policies-scripts`, `fipscheck`, and related deps if not present).
3. Setting the crypto policies to FIPS mode inside the container (e.g., via `update-crypto-policies --set FIPS` in the Dockerfile or at runtime).
4. Optionally bind-mounting host FIPS configurations into the container for consistency (e.g., mount `/etc/crypto-policies` from host to container).
5. Verifying FIPS status inside the running container with commands like `cat /proc/sys/crypto/fips_enabled` (should return 1, inherited from host) and `update-crypto-policies --show` (should return "FIPS").

## Testing

```bash
touch test
openssl md5 test
```

## Note

If your image is not based on Rocky/RHEL (e.g., Ubuntu or Alpine), you may need to use a FIPS-specific base image or add custom FIPS modules, as compatibility varies. Test thoroughly, as some applications or dependencies may break if they rely on non-FIPS algorithms. If formal compliance certification is needed, consult Rocky Linux's FIPS roadmap or use validated images from vendors like Red Hat or Docker.

## References

* [RHEL8 security guide PDF](https://access.redhat.com/system/files/redhat_openshift_security_guide_2_for_screen_0.pdf)

