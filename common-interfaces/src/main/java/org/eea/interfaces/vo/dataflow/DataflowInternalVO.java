package org.eea.interfaces.vo.dataflow;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class DataflowInternalVO extends DataFlowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataflowLink;

    private List<Date> releasedDates;
}
