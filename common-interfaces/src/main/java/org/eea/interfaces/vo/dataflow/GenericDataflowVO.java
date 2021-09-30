package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class GenericDataflowVO.
 */
@Getter
@Setter
@ToString
public class GenericDataflowVO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected Long id;

  /** The description. */
  protected String description;

  /** The name. */
  protected String name;

  /** The deadline date. */
  protected Date deadlineDate;

  /** The obligation. */
  protected ObligationVO obligation;

  /** The status. */
  protected TypeStatusEnum status;

  /** The releasable. */
  protected boolean releasable;

  /** The type. */
  protected TypeDataflowEnum type;

  /** The manual acceptance. */
  protected boolean manualAcceptance;

  /** The documents. */
  protected List<DocumentVO> documents;

  /** The weblinks. */
  protected List<WeblinkVO> weblinks;


}
