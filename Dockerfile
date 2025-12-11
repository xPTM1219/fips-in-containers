FROM public.ecr.aws/docker/library/rockylinux:8.9

# Install needed programs and update system
RUN yum update -y \
    && yum install -y dracut-fips openssl openssl-1.1.1k java-17-openjdk-devel wget vim \
    && yum clean all \
    && rm -rf /var/cache/yum

# Prepare FIPS
#RUN update-crypto-policies --set FIPS \
RUN fips-mode-setup --enable

# Prepare SBT and Scala
RUN rm -f /etc/yum.repos.d/bintray-rpm.repo || true \
    && curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo \
    && mv sbt-rpm.repo /etc/yum.repos.d/ \
    && yum install sbt -y \
    && wget -P /opt/ https://github.com/scala/scala3/releases/download/3.3.7/scala3-3.3.7.tar.gz \
    && tar -zxvf /opt/scala3-3.3.7.tar.gz --directory=/opt/

# Final steps
RUN mkdir /app \
    && echo ":colorscheme evening" > /root/.vimrc \
    && echo ":syntax on" >> /root/.vimrc \
    && echo "export PATH=$PATH:'/opt/scala3-3.3.7/bin'" >> /root/.bashrc

COPY *.java /app/

ENV OPENSSL_FIPS=1

ENTRYPOINT ["/bin/bash"]

