package com.c4soft.bdp.labs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RestApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(RestApiApplication.class, args);
  }

}
