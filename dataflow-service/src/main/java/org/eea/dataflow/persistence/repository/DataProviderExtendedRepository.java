package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.DataProvider;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataProviderExtendedRepository {
    /**
     * Retrieves paginated providers
     * @param pageable
     * @param asc
     * @param sortedColumn
     * @param providerCode
     * @param groupId
     * @param label
     * @return
     */
    List<DataProvider> findProvidersPaginated(Pageable pageable, boolean asc, String sortedColumn, String providerCode, Integer groupId, String label);

    /**
     * Count providers paginated.
     * @param asc
     * @param sortedColumn
     * @param providerCode
     * @param groupId
     * @param label
     * @return
     */
    Long countProvidersPaginated(boolean asc, String sortedColumn, String providerCode, Integer groupId, String label);
}