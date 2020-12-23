package org.eea.interfaces.vo.pams;

import java.util.List;
import lombok.Data;

/**
 * Instantiates a new sector VO.
 */
@Data
public class SectorVO {

  /** The sector affected. */
  private String sectorAffected;

  /** The other sectors. */
  private String otherSectors;

  /** The objectives. */
  private List<String> objectives;

  /** The other objectives. */
  private List<String> otherObjectives;

}
