# QWait

[![Build Status](https://travis-ci.org/mvk13ogb/qwait.png?branch=master)](https://travis-ci.org/mvk13ogb/qwait)

This is QWait, the next generation queuing system for KTH CSC.

# Trying out the application

If you want to try out QWait, simply do this:

  - Install Java JDK version 7 or later.
  - Install Apache Maven version 3 or later.
  - Enter the `qwait` directory and type `mvn jetty:run` to test the
    application.  You can reach it at `http://localhost:8080/`.

Note that unless you have configured LDAP correctly, logged-in users
will have names that are not very readable, because the real names of
users are fetched via LDAP queries.

It is also tricky to do anything with this test version of the
application, because you will not be an administrator, and only
administrators can add queues.  It is recommended that you configure a
database such as PostgreSQL as outlined below, so that you can
configure the initial administrator via the database.

# Deployment

QWait can be deployed in various ways; either continuously via a CI/CD
system, or manually.

## Continuous deployment

This repository is being continuously deployed.  This allows for rapid
development since no manual intervention is required while deploying.

The configuration for this system is in the `.travis.yml` file in the
repository.

The automatic deployment process works like this:

  - A commit is made on the `master` branch of this repository.
  - Travis CI picks up the new commit and starts building it.  One
    such build looked like
    [this](https://travis-ci.org/mvk13ogb/qwait/builds/25261291).
    Travis runs the following commands in order:

        # Fetch the source code
        git clone --depth=50 --branch=master git://github.com/mvk13ogb/qwait.git mvk13ogb/qwait
        cd mvk13ogb/qwait

        # Go to the particular commit we want to build (to avoid a race condition
        # where someone might have committed something else in the mean time)
        git checkout -qf <commit id>

        # Download dependencies & build everything
        mvn install -DskipTests=true -B -V

        # Run unit tests and integration tests
        mvn verify

  - If and only if the exit code of `mvn verify` is `0` will the next
    step be performed.  Then, Travis will run the following script:

        if [[ "$TRAVIS_PULL_REQUEST" == false ]] && [[ "$TRAVIS_BRANCH" == master ]]
        then
            mvn -B package cargo:redeploy -DskipTests=true
        fi

    The additional check is there to ensure that the `mvn` command
    only runs if it is the master branch that is being built.  The
    build configuration is used to build pull requests as well, and
    pull requests should not be deployed.

    As can be guessed from the command, it will package it as a .war
    file and deploy the web application using the Codehaus Cargo
    system.  The configuration for Cargo is stored in the `pom.xml`
    file in the root of the repository.  It might look something like
    this:

        <plugin>
            <groupId>org.codehaus.cargo</groupId>
            <artifactId>cargo-maven2-plugin</artifactId>
            <version>1.4.7</version>
            <configuration>
                <container>
                    <containerId>jetty9x</containerId>
                    <type>remote</type>
                </container>

                <configuration>
                    <type>runtime</type>
                    <properties>
                        <cargo.hostname>qwait.csc.kth.se</cargo.hostname>
                        <cargo.servlet.port>8080</cargo.servlet.port>
                        <cargo.remote.username>admin</cargo.remote.username>
                        <cargo.remote.password>${env.CARGO_PASSWORD}</cargo.remote.password>
                    </properties>
                </configuration>

                <deployer>
                    <type>remote</type>
                </deployer>

                <deployables>
                    <deployable>
                        <groupId>${project.groupId}</groupId>
                        <artifactId>${project.artifactId}</artifactId>
                        <type>war</type>
                        <properties>
                            <context>/</context>
                        </properties>
                    </deployable>
                </deployables>
            </configuration>
        </plugin>

    The configuration says that:

     1. The application should be deployed to a remote Jetty 9.x
        container.
     2. The container is located at `http://qwait.csc.kth.se`
        listening on port `8080`.
     3. To authenticate with Cargo, the username `admin` is used.  The
        password is stored in the `CARGO_PASSWORD` environment
        variable.  Travis knows about this password because of the
        `env[0].secure` line in the `.travis.yml` file in the
        repository, which contains encrypted instructions telling
        Travis to export the `CARGO_PASSWORD` environment variable
        with the right contents.
     4. The deployer is also running remotely.
     5. The artifacts to be deployed include our current project in
        the POM, which should be deployed to the `/` (root) context on
        the server.

  - On the remote server, Jetty 9.x is installed, with a webapp called
    the `cargo-jetty-7-and-onwards-deployer`.  It authenticates the
    deployment attempt and receives the `.war` file.  The file is
    renamed to `ROOT.war` (to put it in the `/` context which has the
    special name `ROOT`) and saved in the `webapps` folder of the Jetty
    container, where Jetty picks it up and starts serving it.

## Manual deployment

The webapp can be manually deployed like so:

  - Run `mvn clean verify package` in the source repository.
  - Copy the resulting `.war` file from the `target/` directory onto
    the server machine into the `webapps/` directory of Jetty, for
    example into `/opt/jetty/webapps/`.  The name of the file will
    decide the context path, so naming the file `foo.war` will deploy
    it to `http://server.se/foo/`.  The special name `ROOT.war` will
    deploy the webapp to `http://server.se/`.

## Caveats

Deploying new versions of a web application without restarting Jetty
for a long time might yield Permanent Generation memory issues in
Jetty's JVM.  The reason for this is that the web application's
classes are reloaded each time the application is deployed, and
sometimes old classes are not successfully evicted from memory.  It is
therefore recommended to restart Jetty after each deploy, but this is
not necessary most of the time.

## Setting it up

### The easy way

Simply checking out the repository and running `mvn jetty:run` is
enough to get a fully functional web application up and running,
listening to port `8080`.  See point 4 below on how to forward port
`8080`.

### The hard way

To set up deployment, a few configuration steps are necessary:

 1. Download and install a Java 7 JRE.  On Ubuntu, this is simply done
    with:

        sudo aptitude install openjdk-7-jre

    Then, switch to that Java installation by default with:

        sudo update-java-alternatives -s java-1.7.0-openjdk-amd64

    To confirm that everything worked, try this and confirm that the
    version is `1.7.0`:

        $ java -version
        java version "1.7.0_55"
        OpenJDK Runtime Environment (IcedTea 2.4.7) (7u55-2.4.7-1ubuntu1~0.12.04.2)
        OpenJDK 64-Bit Server VM (build 24.51-b03, mixed mode)

 2. Download and install a recent version of Jetty or some other
    Servlet container.  The webapp has so far been tested with Jetty,
    so your mileage might vary.  The container must support the Servlet
    v3.0 specification and the Java Websockets API.  For the master
    version at `qwait.csc.kth.se`, Jetty version 9.1.1.v20140108 from
    the 8th of January 2014 was used.  Versions of Jetty can be
    downloaded from [here](http://download.eclipse.org/jetty/).

    Actually installing Jetty is dependent on your particular
    distribution and the version of Jetty chosen.  One way to do it for
    Ubuntu is outlined in
    [this blog post](http://pietervogelaar.nl/ubuntu-12-04-install-jetty-9/).

 3. If you want to use continuous deployment (so that the application
    can simply be deployed with `mvn package cargo:redeploy` from the
    source repository on any machine), download
    `cargo-jetty-7-and-onwards-deployer` from
    [here](http://cargo.codehaus.org/Downloads) under the "Tools"
    section, and put it in your Jetty webapps folder
    (e.g. `/opt/jetty/webapps/`).  Configure the webapp according to
    [the official instructions](http://cargo.codehaus.org/Jetty+Remote+Deployer).

 4. Jetty listens to port `8080` by default.  If the server should
    serve pages on port `80`, it is recommended to either use a
    front-end proxy server such as Apache HTTPD or Nginx, or to
    introduce an `iptables` rule in Linux to forward port `80` to
    `8080`, like so:

        /sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080

    Configuring this rule to be applied on each boot is
    distro-specific.

# Configuration

The default QWait configuration "works" but is not optimal.  You
should change the settings of the application to suit your needs.

The web application is configured using the Spring settings API.  This
API allows settings to be overridden via different sources.

## Before deployment

The easiest way to change a setting is to modify the file
`src/main/resources/settings.properties`.  This is a simple
`key=value` settings file, with the additional property that Maven
variables can be interpolated with `${...}` syntax.  A typical
configuration file for a production environment, along with comments
explaining each setting, would be:

    # The name of the application to display in various places.  Defaults to
    # <project><name>Whatever this is</name></project> from the pom.xml file.
    product.name=${project.name}

    # The version of the application to display in various places.  Defaults to
    # <project><version>Whatever this is</version></project> from the pom.xml
    # file.
    product.version=${project.version}

    # The driver to use when connecting to a database.  Here,
    # PostgreSQL has been configured.  Note that a large number of
    # databases are supported, but you need to make sure that the
    # relevant JDBC driver has been added to the <dependencies> in
    # pom.xml (For PostgreSQL, it is org.postgresql:postgresql)
    dataSource.driverClassName=org.postgresql.Driver

    # The URL to connect to, which is database specific.  In this
    # case, we connect to PostgreSQL on localhost, with the database
    # name qwait.
    dataSource.url=jdbc:postgresql://localhost/qwait

    # The user and password to use when connecting to the database.
    dataSource.username=qwait
    dataSource.password=<secret>

    # Which SQL dialect to use when communicating with the database.
    # Supported dialects are listed here: http://docs.jboss.org/hibernate/orm/4.0/javadocs/org/hibernate/dialect/package-summary.html
    hibernate.dialect=org.hibernate.dialect.PostgreSQL9Dialect

    # How to generate the schema for the database.  Supported values are:
    # - validate: Don't do anything, just verify that the tables are
    #   compatible with the webapp and error out otherwise.
    # - update: Create new tables and columns if they are missing.
    # - create: Overwrites existing tables with empty tables.
    # - create-drop: Like create, but also drops all tables at application
    #   shutdown.
    hibernate.hbm2ddl.auto=update

    # The CAS call-back service URI to use when doing CAS authentication
    security.cas.service=http://qwait.csc.kth.se:8080/authenticate
    # The CAS login URL to use
    security.cas.loginUrl=https://login.kth.se/login
    # The CAS logout URL to use
    security.cas.logoutUrl=https://login.kth.se/logout
    # The actual CAS ticket validator service
    security.cas.ticketValidator=https://login.kth.se
    # Our auth provider key for this webapp
    security.cas.authProviderKey=<secret>

    # The LDAP service to use when doing LDAP lookups
    security.ldap.url=ldaps://ldap0.csc.kth.se
    # The LDAP base query to constrain results
    security.ldap.base=ou=unix,dc=csc,dc=kth,dc=se
    # LDAP auth user if needed
    security.ldap.userDn=
    # LDAP auth password if needed
    security.ldap.password=
    # Enforce read-only operations via anonymous log-in
    security.ldap.anonymousReadOnly=true

    # Application configuration profiles to activate. If the "ldap"
    # profile is activated, the application will try to use LDAP to
    # look up user names.
    spring.profiles.active=default,ldap

## After deployment

When the application has been packaged as a `.war`, it is tedious to
modify the settings inside of it (but possible: simply open the file
as a ZIP archive).

Instead, settings can be overridden using container-specific
mechanisms.  For example, one way is to set Servlet context
parameters.  In Jetty, this is done globally by changing
`/opt/jetty/etc/webdefault.xml`.  A configuration setting might look
like:

    <context-param>
        <param-name>hibernate.dialect</param-name>
        <param-value>org.hibernate.dialect.PostgreSQL9Dialect</param-value>
    </context-param>

It is also possible to do per-application overrides in Jetty using the
`override-web.xml` file as outlined
[here](https://wiki.eclipse.org/Jetty/Reference/override-web.xml).
