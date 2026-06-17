package org.eea.dataset.persistence.metabase.repository;

import feign.Param;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DatasetTableRepository extends JpaRepository<DatasetTable, Long> {

    /**
     * Retrieves an entry based on datasetId and tableSchemaId
     *
     * @param datasetId     the dataset id
     * @param tableSchemaId the table schema id
     * @return optional entry
     */
    Optional<DatasetTable> findByDatasetIdAndTableSchemaId(Long datasetId, String tableSchemaId);

    /**
     * Retrieves an entry based on datasetId and tableSchemaId
     *
     * @param datasetId     the dataset id
     * @param preparationCode the code that identifies the preparation dataset
     * @param tableSchemaId the table schema id
     * @return optional entry
     */
    Optional<DatasetTable> findByDatasetIdAndPreparationCodeAndTableSchemaId(Long datasetId, String preparationCode, String tableSchemaId);

    /**
     * Retrieves entries by datasetId
     *
     * @param datasetId             the dataset id
     * @param isIcebergTableCreated
     * @return list of entries
     */
    List<DatasetTable> findByDatasetIdAndIsIcebergTableCreated(Long datasetId, Boolean isIcebergTableCreated);

    /**
     * Retrieves entries by datasetId
     *
     * @param datasetId             the dataset id
     * @param isIcebergTableCreated
     * @return list of entries
     */
    List<DatasetTable> findByDatasetIdAndPreparationCodeAndIsIcebergTableCreated(Long datasetId, String preparationCode, Boolean isIcebergTableCreated);

    List<DatasetTable> findByDatasetId(Long datasetId);

    @Query(
            "SELECT DISTINCT t.editingUsername " +
                    "FROM DatasetTable t " +
                    "WHERE t.datasetId = :datasetId " +
                    "AND (t.preparationCode IS NULL OR t.preparationCode = '')" +
                    "AND t.editingUsername IS NOT NULL"
    )
    List<String> findEditors(@Param("datasetId") Long datasetId);

    @Query(
            "SELECT DISTINCT t.editingUsername " +
                    "FROM DatasetTable t " +
                    "WHERE t.datasetId = :datasetId " +
                    "AND t.preparationCode = :preparationCode " +
                    "AND t.editingUsername IS NOT NULL"
    )
    List<String> findEditors(@Param("datasetId") Long datasetId, @Param("preparationCode") String preparationCode);

    @Query(
            "SELECT DISTINCT t.editingUsername " +
                    "FROM DatasetTable t " +
                    "WHERE t.datasetId = :datasetId " +
                    "AND (t.preparationCode IS NULL OR t.preparationCode = '')" +
                    "AND t.tableSchemaId = :tableSchemaId " +
                    "AND t.editingUsername IS NOT NULL"
    )
    List<String> findEditorsOfTable(@Param("datasetId") Long datasetId, @Param("tableSchemaId") String tableSchemaId);

    @Query(
            "SELECT DISTINCT t.editingUsername " +
                    "FROM DatasetTable t " +
                    "WHERE t.datasetId = :datasetId " +
                    "AND t.preparationCode = :preparationCode " +
                    "AND t.tableSchemaId = :tableSchemaId " +
                    "AND t.editingUsername IS NOT NULL"
    )
    List<String> findEditorsOfTable(@Param("datasetId") Long datasetId,
                                    @Param("preparationCode") String preparationCode,
                                    @Param("tableSchemaId") String tableSchemaId);

    @Query(
            "SELECT DISTINCT t.editingUsername " +
                    "FROM DatasetTable t " +
                    "WHERE t.datasetId = :datasetId " +
                    "AND t.editingUsername IS NOT NULL " +
                    "AND t.editLockExpirationDate > CURRENT_TIMESTAMP"
    )
    List<String> findNonExpiredEditors(@Param("datasetId") Long datasetId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value =
                    "UPDATE dataset_table t " +
                            "SET editing_username = :username, " +
                            "    edit_lock_expires_at = CURRENT_TIMESTAMP + (:expirationIntervalInHours * INTERVAL '1 hour') " +
                            "WHERE t.dataset_id = :datasetId " +
                            "AND NOT EXISTS ( " +
                            "   SELECT 1 FROM dataset_table x " +
                            "   WHERE x.dataset_id = :datasetId " +
                            "     AND x.editing_username IS NOT NULL " +
                            "     AND x.editing_username <> :username " +
                            "     AND x.edit_lock_expires_at > CURRENT_TIMESTAMP" +
                            ")"
    )
    int lockEditingForDatasetUser(
            @Param("datasetId") Long datasetId,
            @Param("username") String username,
            @Param("expirationInterval") Long expirationIntervalInHours);


    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value =
                    "UPDATE dataset_table " +
                            "SET editing_username = NULL, edit_lock_expires_at = NULL " +
                            "WHERE dataset_id = :datasetId"
    )
    void unlockEditingForDatasetUser(@Param("datasetId") Long datasetId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value =
                    "INSERT INTO dataset_table (id, dataset_id, dataset_schema_id, table_schema_id, editing_username) " +
                            "SELECT nextval('dataset_table_id_seq'), :datasetId, :datasetSchemaId, table_id, NULL " +
                            "FROM unnest(CAST(:tableSchemaIds AS varchar[])) AS table_id " +
                            "WHERE NOT EXISTS ( " +
                            "    SELECT 1 FROM dataset_table x " +
                            "    WHERE x.dataset_id = :datasetId " +
                            "      AND x.table_schema_id = table_id " +
                            ")"
    )
    void insertMissingDatasetTableEntries(
            @Param("datasetId") Long datasetId,
            @Param("datasetSchemaId") String datasetSchemaId,
            @Param("tableSchemaIds") String tableSchemaIds   // must be a Postgres array literal
    );


    List<DatasetTable> findDatasetTablesByEditingUsername(String editingUsername);

    @Query(
            nativeQuery = true,
            value =
                    "SELECT edit_lock_expires_at " +
                            "FROM dataset_table " +
                            "WHERE dataset_id = :datasetId " +
                            "LIMIT 1"
    )
    Date getLockExpirationDateByDatasetId(@Param("datasetId") Long datasetId);




    List<DatasetTable> findDatasetTableByEditLockExpirationDateBefore(Date date);


    @Query(
            nativeQuery = true,
            value =
                    "SELECT * " +
                            "FROM dataset_table " +
                            "INNER JOIN dataset " +
                            "ON dataset_table.dataset_id = dataset.id " +
                            "WHERE dataset.dataflowid = :dataflowId "
    )
    List<DatasetTable> findDatasetTableByDataflowId(Long dataflowId);
}