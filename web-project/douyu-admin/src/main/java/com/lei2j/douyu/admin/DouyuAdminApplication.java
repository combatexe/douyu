package com.lei2j.douyu.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


/**
 * @author lei2j
 */
@SpringBootApplication(scanBasePackages = "com.lei2j.douyu")
@Configuration
public class DouyuAdminApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DouyuAdminApplication.class);

	private static final String FILE_PATH = "/opt/douyu/admin/prod.properties";

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(DouyuAdminApplication.class);
		springApplication.addInitializers((ctx)->{
			ConfigurableEnvironment environment = ctx.getEnvironment();
			try {
				DefaultPropertySourceFactory defaultPropertySourceFactory = new DefaultPropertySourceFactory();
				FileSystemResource fileResource = new FileSystemResource(FILE_PATH);
				if(!fileResource.exists()){
					LOGGER.warn("不存在此配置文件,location:{},将使用默认配置文件",FILE_PATH);
					return;
				}
				EncodedResource encodedResource = new EncodedResource(fileResource, Charset.forName("utf-8"));
				PropertySource<?> propertySource = defaultPropertySourceFactory.createPropertySource(null,
						encodedResource);
				environment.getPropertySources().addFirst(propertySource);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
		springApplication.addListeners((listener)->
			LOGGER.info("Receive msg:{},timestamp:{}",listener.getSource(), LocalDateTime.ofInstant(Instant.ofEpochMilli(listener.getTimestamp()), ZoneId.of("+8")))
		);
		springApplication.run(args);
//		SpringApplication.run(DouyuApplication.class, args);
	}
}
