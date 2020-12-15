package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.PaMService;
import org.eea.interfaces.vo.pams.EntitiesPaMsVO;
import org.eea.interfaces.vo.pams.SectorVO;
import org.eea.interfaces.vo.pams.SinglePaMVO;
import org.springframework.stereotype.Service;


/**
 * The Class PaMsServiceImpl.
 */
@Service
public class PaMsServiceImpl implements PaMService {

  /**
   * Gets the list single paM.
   *
   * @return the list single paM
   */
  @Override
  public List<SinglePaMVO> getListSinglePaM() {
    SinglePaMVO singlePaMs = new SinglePaMVO();
    SectorVO sectorVO = new SectorVO();
    EntitiesPaMsVO entities = new EntitiesPaMsVO();
    List<SinglePaMVO> singlesPaMs = new ArrayList<>();
    List<EntitiesPaMsVO> entitiesList = new ArrayList<>();
    List<String> unionPolicyList = new ArrayList<>();
    List<String> objectives = new ArrayList<>();
    List<String> otherObjectives = new ArrayList<>();
    List<SectorVO> sectors = new ArrayList<>();
    unionPolicyList.add("F-gas Regulation 2006/842/EC");
    unionPolicyList.add("Cogeneration Directive 2004/8/EC");
    objectives.add("Increase in renewable energy sources in the electricity sector");
    objectives.add("Switch to less carbon-intensive fuels");
    otherObjectives.add("1111");
    otherObjectives.add("2222");
    sectorVO.setSectorAffected("4");
    sectorVO.setOtherSectors("");
    sectorVO.setOtherObjectives(otherObjectives);
    sectorVO.setObjectives(objectives);
    sectors.add(sectorVO);
    entities.setName("National government");
    entities.setType("Government");
    entitiesList.add(entities);
    singlePaMs.setId("1");
    singlePaMs.setImplementationPeriodComment("implementationPeriodComment");
    singlePaMs.setImplementationPeriodFinish("2020");
    singlePaMs.setImplementationPeriodStart("2008");
    singlePaMs.setIsPolicyMeasureEnvisaged("No");
    singlePaMs.setProjectionsScenario("With existing measures");
    singlePaMs.setStatusImplementation("Implemented");
    singlePaMs.setUnionPolicyList(unionPolicyList);
    singlePaMs.setSectors(sectors);
    singlePaMs.setEntities(entitiesList);
    singlePaMs.setPolicyImpacting("EU ETS");
    singlesPaMs.add(singlePaMs);
    return singlesPaMs;
  }



}
