package com.pushnpray.projectpi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProperties {

	/* logback class field */
	final static Logger logger = LoggerFactory.getLogger(ConfigurationProperties.class.getName());

	private static String logToken = Scraper.logToken;

	public static void main(String[] args) {
		logger.info("instance={}, action={}, value={}", logToken, "init_read_properties", 0);
		try {
			InputStream is = ConfigurationProperties.class.getResourceAsStream("/config.properties");
			Properties properties = new Properties();
			properties.load(is);
			is.close();

			Enumeration<?> enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				logger.info("instance={}, action={}, value={}", logToken, "init_" + key.toString(), value.toString());
			}
		} catch (FileNotFoundException e) {
			logger.error("instance={}, action={}, value={}", logToken, "throw_exception", e.toString());
		} catch (IOException e) {
			logger.error("instance={}, action={}, value={}", logToken, "throw_exception", e.toString());
		}
	}
}
