package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

@Getter
@Setter
@ToString
public class ImportStatisticsVO {

    /** The date of the last import in UTC. */
    private Date lastImportDate;

    /** The number of records imported. */
    private Long numberOfRecordsImported;
}
