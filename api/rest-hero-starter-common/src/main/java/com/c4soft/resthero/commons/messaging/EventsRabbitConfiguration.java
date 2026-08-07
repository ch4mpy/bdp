package com.c4soft.resthero.commons.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
// LAB:2.7:TODO:START cette configuration référence des types de spring-boot-starter-amqp, une dépendance optionnelle du starter : elle ne doit s'activer que si RabbitMQ est effectivement sur le classpath du module consommateur
@ConditionalOnClass(RabbitTemplate.class)
// LAB:2.7:TODO:END
@EnableConfigurationProperties(RestHeroEventsProperties.class)
public class EventsRabbitConfiguration {

  @Bean
  TopicExchange eventsExchange(RestHeroEventsProperties properties) {
    return new TopicExchange(properties.getExchangeName());
  }

  @Bean
  MessageConverter eventsMessageConverter(JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(jsonMapper);
  }
}
