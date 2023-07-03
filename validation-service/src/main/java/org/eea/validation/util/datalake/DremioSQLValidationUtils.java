package org.eea.validation.util.datalake;

import org.eea.validation.configuration.DremioConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Import(DremioConfiguration.class)
@Component
public class DremioSQLValidationUtils {

    private JdbcTemplate dremioJdbcTemplate;

    public List<String> isfieldFK(Long datasetId, String idFieldSchema, String idRule, boolean pkMustBeUsed) {

        return new ArrayList<>();
    }

    public List<String> isSQLSentenceWithCode(String sql) {
        StringBuilder query = new StringBuilder();
        query.append("select record_id from(").append(sql).append(")");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    public List<String> isUniqueConstraint(String fieldName, String tablePath) {
        StringBuilder query = new StringBuilder();
        query.append("with tableAux as (select ").append(fieldName).append(", count(").append(fieldName).append(") from ")
                .append(tablePath).append(" group by ").append(fieldName).append(" having count(").append(fieldName)
                .append(")>1)").append(" select t.record_id from ").append(tablePath).append(" t where t.")
                .append(fieldName).append(" in (select tab.").append(fieldName).append(" from tableAux tab)");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }
}


















