package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.pams.SinglePaMVO;


/**
 * The Interface PaMsService.
 */
public interface PaMService {

  /**
   * Gets the single pa ms.
   *
   * @return the single pa ms
   */
  List<SinglePaMVO> getSinglePaMs();

}
