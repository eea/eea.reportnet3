package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.RecordValue;

import java.sql.SQLException;
import java.util.List;

public interface TableExtendedQueriesRepository {


    Long findIdByIdTableSchema(String idTableSchema, Long datasetId) throws SQLException;
}
