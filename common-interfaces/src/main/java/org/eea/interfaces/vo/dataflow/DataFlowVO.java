package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Data flow vo.
 */
@Getter
@Setter
@ToString
public class DataFlowVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8073212422480973637L;

  /** The id. */
  private Long id;

  /** The datasets. */
  private List<ReportingDatasetVO> reportingDatasets;

  /** The design datasets. */
  private List<DesignDatasetVO> designDatasets;

  /** The data collections. */
  private List<DataCollectionVO> dataCollections;

  /** The eu datasets. */
  private List<EUDatasetVO> euDatasets;

  /** The test dataset. */
  private List<TestDatasetVO> testDatasets;

  /** The reference datasets. */
  private List<ReferenceDatasetVO> referenceDatasets;

  /** The description. */
  private String description;

  /** The name. */
  private String name;

  /** The deadline date. */
  private Date deadlineDate;

  /** The deadline date. */
  private Date creationDate;

  /** The status. */
  private TypeStatusEnum status;

  /** The documents. */
  private List<DocumentVO> documents;

  /** The weblinks. */
  private List<WeblinkVO> weblinks;

  /** The type. */
  private TypeDataflowEnum type;


  /** The request id. */
  private Long requestId;

  /** The contributors. */
  private List<ContributorVO> contributors;

  /** The representatives. */
  private List<RepresentativeVO> representatives;

  /** The obligation. */
  private ObligationVO obligation;

  /** The manual acceptance. */
  private boolean manualAcceptance;

  /** The reporting status. */
  private DatasetStatusEnum reportingStatus;

  /** The releasable. */
  private boolean releasable;

  /** The show public info. */
  private boolean showPublicInfo;

  /** The any schema available in public. */
  private boolean anySchemaAvailableInPublic;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataFlowVO that = (DataFlowVO) o;
    return id.equals(that.id);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, name, deadlineDate, status, reportingDatasets,
        designDatasets);
  }

}
