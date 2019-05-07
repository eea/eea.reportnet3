package org.eea.document.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentConfiguration {

	static {
		System.setProperty("oak.documentMK.disableLeaseCheck", "true");
		System.setProperty("oak.documentMK.leaseDurationSeconds", "5");
	}
}
