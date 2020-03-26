package org.eea.rod.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.QueryParam;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Obligation controller.
 */
@RestController
@RequestMapping("/obligation")
public class ObligationControllerImpl implements ObligationController {

  @Override
  @RequestMapping(value = "/findOpenened", method = RequestMethod.GET)
  public List<ObligationVO> findOpenedObligations() {
    List<ObligationVO> result = new ArrayList<>();
    ObligationVO obligationVO1 = new ObligationVO();
    obligationVO1.setObligationId(1);
    obligationVO1.setComment("dummy comment");
    obligationVO1.setDescription("DUMMY OBLIGATION DESCRIPTION 1");
    obligationVO1.setOblTitle("DUMMY OBLIGATION 1");
    obligationVO1.setNextDeadline(new Date());

    ObligationVO obligationVO2 = new ObligationVO();
    obligationVO2.setObligationId(1);
    obligationVO2.setComment("dummy comment");
    obligationVO2.setDescription("DUMMY OBLIGATION DESCRIPTION 2");
    obligationVO2.setOblTitle("DUMMY OBLIGATION 2");
    obligationVO2.setNextDeadline(new Date());

    ObligationVO obligationVO3 = new ObligationVO();
    obligationVO3.setObligationId(1);
    obligationVO3.setComment("dummy comment");
    obligationVO3.setDescription("DUMMY OBLIGATION DESCRIPTION 3");
    obligationVO3.setOblTitle("DUMMY OBLIGATION 3");
    obligationVO3.setNextDeadline(new Date());
    result.add(obligationVO1);
    result.add(obligationVO2);
    result.add(obligationVO3);
    return result;
  }

  @Override
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ObligationVO findObligationById(@QueryParam(value = "id") Long id) {
    ObligationVO obligationVO = new ObligationVO();
    obligationVO.setObligationId(1);
    obligationVO.setComment("dummy comment");
    obligationVO.setDescription("DUMMY OBLIGATION DESCRIPTION 1");
    obligationVO.setOblTitle("DUMMY OBLIGATION 1");
    obligationVO.setNextDeadline(new Date());
    return obligationVO;
  }
}
