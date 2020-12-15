package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.pams.SinglePaMsVO;


/**
 * The Interface PaMsService.
 */
public interface PaMsService {

  /**
   * Gets the single pa ms.
   *
   * @return the single pa ms
   */
  List<SinglePaMsVO> getSinglePaMs();

}
