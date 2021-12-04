# spring-svc-layer-util
To identify the list of J2EE Spring Service Layer methods not having Pre-Authorise Annotation. This can be used in the CI Pipeline for better quality and security.

# Sonatype JIRA ID
https://issues.sonatype.org/browse/OSSRH-75706

# How to Publish Your Artifacts to Maven Central
If you're wondering how to publish your artifacts to Maven central, then this step-by-step guide is for you. Heads up: you'll need to have Github.

## Prerequisite Steps
* Step 0: Maven settings [help](https://www.baeldung.com/maven-settings-xml)
* Step 1: Make sure if you have installed JDK, Maven, Github, etc.
* Step 2: Create a Github account if you haven't already.
* Step 3: Create a new Github repository.
* Step 4: Add a new SSH key to your Github account

```
1. Open Terminal.
2. Paste the text below, substituting in your GitHub email address.
# ssh-keygen -t ed25519 -C "your_email@example.com"
```
* Step 5: Push the code to Github. 
* Step 6: Sign up for a Sonatype Jira account.
* Step 7: Create a [new Jira issue](https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134) for new project hosting. [Click here](https://issues.sonatype.org/browse/OSSRH-24465) for a sample request. 
* Step 8:Install [GNU PG](https://www.gnupg.org/download/). Install in your OS and verify as follows:

```
C:\Users\Nadeem>gpg --version
gpg (GnuPG) 2.1.15
libgcrypt 1.7.3
Copyright (C) 2016 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <https://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Home: C:/Users/Nadeem/AppData/Roaming/gnupg
Supported algorithms:
Pubkey: RSA, ELG, DSA, ECDH, ECDSA, EDDSA
Cipher: IDEA, 3DES, CAST5, BLOWFISH, AES, AES192, AES256, TWOFISH,
        CAMELLIA128, CAMELLIA192, CAMELLIA256
Hash: SHA1, RIPEMD160, SHA256, SHA384, SHA512, SHA224
Compression: Uncompressed, ZIP, ZLIB, BZIP2

C:\Users\Nadeem>
```
* Step 9: Generate the key pair.

```
C:\Users\Nadeem>gpg --full-gen-key
gpg (GnuPG) 2.1.15; Copyright (C) 2016 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

gpg: keybox 'C:/Users/Nadeem/AppData/Roaming/gnupg/pubring.kbx' created
Please select what kind of key you want:
   (1) RSA and RSA (default)
   (2) DSA and Elgamal
   (3) DSA (sign only)
   (4) RSA (sign only)
Your selection? 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048)
Requested keysize is 2048 bits
Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0)
Key does not expire at all
Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: Nadeem Mohammad
Email address: coolmind182006@gmail.com
Comment:
You selected this USER-ID:
    "Nadeem Mohammad <coolmind182006@gmail.com>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? o
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.

We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
gpg: C:/Users/Nadeem/AppData/Roaming/gnupg/trustdb.gpg: trustdb created
gpg: key 27835B3BD2A2061F marked as ultimately trusted
gpg: directory 'C:/Users/Nadeem/AppData/Roaming/gnupg/openpgp-revocs.d' created
gpg: revocation certificate stored as 'C:/Users/Nadeem/AppData/Roaming/gnupg/openpgp-revocs.d\5694AA563793429557F1727835B3BD2A223A.rev'
public and secret key created and signed.

pub   rsa2048 2016-08-29 [SC]
      5694AA563793429557F1727835B3BD2A223A
uid                      Nadeem Mohammad <coolmind182006@gmail.com>
sub   rsa2048 2016-08-29 [E]


C:\Users\Nadeem>
```

* Step 10: Enter a passphrase.
Enter passphrase for the keypair

## Publishing Steps
### Step 1: Add a distributed management section to your POM.

Add the deploy plugin.

```
<plugin>
<artifactId>maven-deploy-plugin</artifactId>
<version>2.8.2</version>
<executions>
<execution>
<id>default-deploy</id>
<phase>deploy</phase>
<goals>
<goal>deploy</goal>
</goals>
</execution>
</executions>
</plugin>
```
Here is where you can add the distribution management section to your POM:

```
<distributionManagement>
<snapshotRepository>
<id>ossrh</id>
<url>https://oss.sonatype.org/content/repositories/snapshots</url>
</snapshotRepository>
<repository>
<id>ossrh</id>
<url>https://oss.sonatype.org/service/local/staging/deploy/maven2/
</url>
</repository>
</distributionManagement>
```
### Step 2: Add the ossrh server detail into your settings.xml under M2_REPO home.

```
<settings>
<servers>
<server>
<id>ossrh</id>
<username>your-jira-id</username>
<password>your-jira-pwd</password>
</server>
</servers>
</settings>
```

Note: the ID element of your servers/server in settings.xml should be identical to the ID element of snapshotRepository and the repository in your POM file.


### Step 3: Add the SCM section to your POM.

```
<scm>
<connection>scm:git:git://github.com/dexecutor/dependent-tasks-executor.git</connection>
<developerConnection>scm:git:git@github.com:dexecutor/dexecutor.git</developerConnection>
<url>https://github.com/dexecutor/dependent-tasks-executor</url>
<tag>HEAD</tag>
</scm>
```

### Step 4: Add the Maven release plugin.

```
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-release-plugin</artifactId>
<version>2.5.3</version>
<configuration>
<localCheckout>true</localCheckout>
<pushChanges>false</pushChanges>
<mavenExecutorId>forked-path</mavenExecutorId>
<arguments>-Dgpg.passphrase=${gpg.passphrase}</arguments>
</configuration>
<dependencies>
<dependency>
<groupId>org.apache.maven.scm</groupId>
<artifactId>maven-scm-provider-gitexe</artifactId>
<version>1.9.5</version>
</dependency>
</dependencies>
</plugin>
```
Add a GPG passphrase as with your profile in Maven settings.xml.

```
<settings>
<profiles>
<profile>
<id>ossrh</id>
<activation>
<activeByDefault>true</activeByDefault>
</activation>
<properties>
<gpg.passphrase>[your_gpg_passphrase]</gpg.passphrase>
</properties>
</profile>
</profiles>
</settings>
```
Add the Nexus staging Maven plugin.

```
<plugin>
<groupId>org.sonatype.plugins</groupId>
<artifactId>nexus-staging-maven-plugin</artifactId>
<version>1.6.7</version>
<extensions>true</extensions>
<configuration>
<serverId>ossrh</serverId>
<nexusUrl>https://oss.sonatype.org/</nexusUrl>
<autoReleaseAfterClose>true</autoReleaseAfterClose>
</configuration>
</plugin>
```
### Step 5: Add the source and the javadoc plugin.

```
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-source-plugin</artifactId>
<version>3.0.1</version>
<executions>
<execution>
<id>attach-sources</id>
<goals>
<goal>jar</goal>
</goals>
</execution>
</executions>
</plugin>
```

```
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-source-plugin</artifactId>
<version>2.10.4</version>
<configuration>
<encoding>UTF-8</encoding>
</configuration>
<execuations>
<execution>
<id>attach-javadoc</id>
<goals>
<execution>  
</executions>
</plugin>
```

### Step 6: Configure to sign artifacts while releasing.

```
<profiles>
<!-- GPG Signature on release -->
<profile>
<id>release-sign-artifacts</id>
<activation>
<property>
<name>performRelease</name>
<value>true</value>
</property>
</activation>
<build>
<plugins>
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-gpg-plugin</artifactId>
<version>1.6</version>
<executions>
<execution>
<id>sign-artifacts</id>
<phase>verify</phase>
<goals>
<goal>sign</goal>
</goals>
</execution>
</executions>
</plugin>
</plugins>
</build>
</profile>
</profiles>
```

### Step 7: Publish the GPG key pair and distribute your key to GPG servers:
Please find help [here](https://central.sonatype.org/publish/requirements/gpg/)

```
gpg --keyserver keyserver.ubuntu.com --send-keys 2B18641C2C8FAEC3FFBD7597A40A580D3962BCB8
```

### Step 8: Do the release!

```
mvn clean
mvn release:prepare
mvn release:perform
```
### Step 9: Push the tag and code to your remote repo.

```
git push–tags
git push origin master
```

### Step 10: Verify the sonatype repository.

```

```

### Step 11: Update the Sonatype Jira ticket.
