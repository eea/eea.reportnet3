package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.pams.SinglePaMVO;


/**
 * The Interface PaMsService.
 */
public interface PaMService {

  /**
   * Gets the list single paM.
   *
   * @return the list single paM
   */
  List<SinglePaMVO> getListSinglePaM();

}
