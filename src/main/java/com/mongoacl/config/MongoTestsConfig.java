package com.mongoacl.config;

import com.mongoacl.repository.AclRepository;
import com.mongoacl.repository.ObjectIdentityRepository;
import com.mongoacl.repository.SidRepository;
import com.mongoacl.service.impl.MongodbMutableAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;

/**
 * Created by Satellite on 27.03.2017.
 */
@Configuration
//TODO: почему-то не работает со многими пакетами????
@ComponentScan(basePackages = {"com.mongoacl"})
public class MongoTestsConfig {

    @Bean
    public ConsoleAuditLogger consoleAuditLogger() {
        return new ConsoleAuditLogger();
    }

}
