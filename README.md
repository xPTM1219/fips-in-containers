# FIPS in containers

In this repo we test how to get FIPS enabled in a container in Docker.
This test haven't been run in Podman.

## RHEL info

### Enabling FIPS Mode in a Container

To enable FIPS mode in a container running on Red Hat Enterprise Linux :

1. The host system must be switched into FIPS mode.
2. Mount the /etc/system-fips file on the container from the host.
3. Set the FIPS cryptographic policy level in the container :
   `$ update-crypto-policies --set FIPS`

> If using Red Hat Enterprise Linux 8.2 or later, an alternative method for
> switching a container to FIPS mode was introduced. It requires only using
> the following command in the container :

```txt
mount --bind /usr/share/crypto-policies/back-ends/FIPS /etc/crypto-policies/back-ends
```

## Running the container

`docker run --mount type=bind,src=/usr/share/crypto-policies/back-ends/FIPS,dst=/etc/crypto-policies/back-ends -it rocky8-fips:java17 bash`

## Status

The work is about at 80% because even though the image does run and have
FIPS enabled system-wide, FIPS says that it has inconsistencies when
running `fips-mode-setup --check`.

### Tests

I have tested both code present in this repo. The file `JavaCrypto` uses old
ciphers not allowed under FIPS. The file `JavaCryptoFips` uses accepted ciphers
under FIPS.

To run the code from within the container:

> The code is on `/app`

```bash
# Compile
javac JavaCrypto.java
javac JavaCryptoFips.java

# Run it
java JavaCrypto # Should fail
java JavaCryptoFips # Should work
```

## Rsources

* [Docker FIPS](https://github.com/arhea/docker-fips-library)
* [RHEL Openshift PDF](https://access.redhat.com/system/files/redhat_openshift_security_guide_2_for_screen_0.pdf)

