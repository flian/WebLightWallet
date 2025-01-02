package co.yiiu.pybbs.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@Configuration
@Slf4j
public class FlywayConfig {

    @Resource
    private DataSource dataSource;

    @Value("${spring.flyway.enabled:false}")
    private boolean flyEnabled;

    @PostConstruct
    @DependsOn("dataSourceHelper")
    public void migrate() {
        if(!flyEnabled){
            log.info("disabled flyway migration.. skip db init.");
            return;
        }
        Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration",
                "filesystem:db/migration").baselineOnMigrate(true).load();
        flyway.migrate();
    }

}
