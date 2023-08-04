package org.eea.dataset.persistence.data.repository;

import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;

public class TableRepositoryImpl implements TableExtendedQueriesRepository {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TableRepositoryImpl.class);

    /** The record store controller zuul. */
    @Autowired
    private RecordStoreController.RecordStoreControllerZuul recordStoreControllerZuul;


    @Override
    public Long findIdByIdTableSchema(String idTableSchema, Long datasetId) throws SQLException {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String query = "select distinct id from dataset_%s.table_value where id_table_schema = ?";

            ConnectionDataVO connectionDataVO = recordStoreControllerZuul
                .getConnectionToDataset(LiteralConstants.DATASET_PREFIX + datasetId);

            connection = DriverManager.getConnection(connectionDataVO.getConnectionString(),
                connectionDataVO.getUser(), connectionDataVO.getPassword());
            query = String.format(query, datasetId);
            LOG.info("findIdByIdTableSchema query: {}", query);

            pstmt = connection.prepareStatement(query);
            pstmt.setString(1, idTableSchema);
            LOG.info("findIdByIdTableSchema query ps: {}", pstmt);

            rs = pstmt.executeQuery();
            rs.next();

            return rs.getLong(1);
        } catch (Exception e) {
            LOG.error(
                "Unexpected error! Error in countByTableSchema for datasetId {} with filter_value {}",
                datasetId, e);
        } finally {
            if (rs != null)
                rs.close();
            if (pstmt != null)
                pstmt.close();
            if (connection != null)
                connection.close();
        }

        return 0L;
    }
}
