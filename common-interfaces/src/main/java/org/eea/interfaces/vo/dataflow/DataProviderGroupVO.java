package org.eea.interfaces.vo.dataflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;

import java.io.Serializable;
import java.util.List;

/**
 * The Class DataProviderGroupVO.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DataProviderGroupVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -924509754041958192L;

  private Long id;
  private String name;
  private TypeDataProviderEnum type;
  private List<DataProviderGroupVO> dataProviders;

}
