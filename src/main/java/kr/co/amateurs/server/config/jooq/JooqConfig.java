package kr.co.amateurs.server.config.jooq;

import org.jooq.conf.ExecuteWithoutWhere;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.StatementType;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {
    @Bean
    public DefaultConfigurationCustomizer jooqDefaultConfigurationCustomizer() {
        return configuration -> configuration.settings()
                .withExecuteDeleteWithoutWhere(ExecuteWithoutWhere.THROW)
                .withExecuteUpdateWithoutWhere(ExecuteWithoutWhere.THROW)
                .withRenderSchema(false)
                .withRenderFormatted(true)
                .withBatchSize(50)
                .withFetchSize(100)
                .withQueryTimeout(30)
                .withRenderSchema(false)
                .withRenderFormatted(false)
                .withRenderQuotedNames(RenderQuotedNames.NEVER)
                .withStatementType(StatementType.PREPARED_STATEMENT)
                .withExecuteLogging(false);
    }
}
