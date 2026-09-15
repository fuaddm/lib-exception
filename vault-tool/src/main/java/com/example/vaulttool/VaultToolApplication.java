package com.example.vaulttool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Fetches the Nexus {@code deployer} credentials from Vault (AppRole auth,
 * read via Spring Cloud Vault - see application.yml) and prints them as
 * {@code VAULT_TOOL_PROPERTY KEY=VALUE} lines on stdout - a stable, greppable
 * format the root build.gradle's {@code fetchNexusCredentialsFromVault} task
 * parses to feed the Nexus publishing repository's credentials, so a plain
 * {@code ./gradlew publish} does the whole fetch-then-publish flow in one
 * Gradle invocation, one process boundary per run (this JVM, forked once by
 * that task) - see ../docs/publishing.md.
 */
@SpringBootApplication
public class VaultToolApplication implements CommandLineRunner {

    @Value("${base_url}")
    private String nexusUrl;

    @Value("${username}")
    private String nexusUsername;

    @Value("${password}")
    private String nexusPassword;

    public static void main(String[] args) {
        SpringApplication.run(VaultToolApplication.class, args);
        // Force the JVM down instead of waiting on any lingering non-daemon
        // threads the Vault HTTP client may have started.
        System.exit(0);
    }

    @Override
    public void run(String... args) {
        System.out.println("VAULT_TOOL_PROPERTY NEXUS_URL=" + nexusUrl);
        System.out.println("VAULT_TOOL_PROPERTY NEXUS_USERNAME=" + nexusUsername);
        System.out.println("VAULT_TOOL_PROPERTY NEXUS_PASSWORD=" + nexusPassword);
    }
}
