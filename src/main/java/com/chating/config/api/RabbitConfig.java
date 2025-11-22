package com.chating.config.api;

import java.util.UUID;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    
    @Value("${spring.rabbitmq.host}") private String host;
    @Value("${spring.rabbitmq.port}") private int port;
    @Value("${spring.rabbitmq.username}") private String username;
    @Value("${spring.rabbitmq.password}") private String password;
    @Value("${spring.rabbitmq.virtual-host}") private String vhost;

    public static final String CHAT_FANOUT_EXCHANGE = "chat.fanout";

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        return factory;
    }

    // Fanout Exchange: 모든 바인딩된 큐에 브로드캐스트
    @Bean
    public FanoutExchange chatFanoutExchange() {
        return new FanoutExchange(CHAT_FANOUT_EXCHANGE, true, false);
    }

    // 서버별 고유 큐 (서버 재시작 시 자동 삭제)
    @Bean
    public Queue serverChatQueue() {
        // 랜덤 UUID로 서버별 고유 큐 생성
        String queueName = "chat.server." + UUID.randomUUID().toString();
        return new Queue(queueName, false, true, true);
    }

    // Fanout Exchange와 서버 큐 바인딩
    @Bean
    public Binding bindingServerChatQueue(Queue serverChatQueue, FanoutExchange chatFanoutExchange) {
        return BindingBuilder.bind(serverChatQueue).to(chatFanoutExchange);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}