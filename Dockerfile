#
# QWait Dockerfile
#
# https://github.com/mvk13ogb/qwait
#

# Base image
FROM dockerfile/java:oracle-java8

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Install Jetty
ADD http://eclipse.org/downloads/download.php?file=/jetty/9.2.3.v20140905/dist/jetty-distribution-9.2.3.v20140905.tar.gz&r=1 /opt/jetty.tar.gz

RUN tar -xvf /opt/jetty.tar.gz -C /opt/ && \
    rm /opt/jetty.tar.gz && \
    mv /opt/jetty-distribution-9.2.3.v20140905 /opt/jetty && \
    rm -rf /opt/jetty/webapps.demo && \
    useradd jetty -U -s /bin/false && \
    chown -R jetty:jetty /opt/jetty

# Copy build files
COPY pom.xml /data/
COPY src/ /data/src/

# Build project
RUN mvn verify

# Install WAR
RUN cp target/qwait-*.war /opt/jetty/webapps/ROOT.war

# Expose Jetty HTTP port
EXPOSE 8080

WORKDIR /opt/jetty

# Run Jetty as the entrypoint
ENTRYPOINT ["/usr/bin/java", "-jar", "start.jar"]
