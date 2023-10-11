package org.eea.interfaces.vo.dataflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataProvidersVO {

    /** The providers list. */
    private List<DataProviderVO> providersList;

    /** The total records. */
    private Long totalRecords;

    /** The filtered records. */
    private Long filteredRecords;
}

