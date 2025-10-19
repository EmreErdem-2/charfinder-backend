package com.example.charfinder.rsql_tools;

import cz.jirutka.rsql.parser.RSQLParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RsqlConfig {
    @Bean
    public MongoRsqlVisitor mongoRsqlVisitor() {
        return new MongoRsqlVisitor();
    }
    @Bean
    public RSQLParser rsqlParser() {
        return new RSQLParser();
    }
}
