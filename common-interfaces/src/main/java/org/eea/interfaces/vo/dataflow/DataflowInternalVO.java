package org.eea.interfaces.vo.dataflow;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class DataflowInternalVO extends DataFlowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UserNationalCoordinatorVO> nationalCoordinatorVOList;

}
