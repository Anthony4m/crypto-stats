package com.anthony4m.cryptostats;

import com.anthony4m.cryptostats.config.VaultCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(VaultCredential.class)
public class CryptoStatsApplication implements CommandLineRunner {

	private final VaultCredential vaultCredential;

	public CryptoStatsApplication(VaultCredential vaultCredential) {
		this.vaultCredential = vaultCredential;
	}

	public static void main(String[] args) {
		SpringApplication.run(CryptoStatsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("ApiHost "+ vaultCredential.getApiHost() );
	}
}
