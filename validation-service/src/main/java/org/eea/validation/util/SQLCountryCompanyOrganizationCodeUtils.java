package org.eea.validation.util;

import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SQLCountryCompanyOrganizationCodeUtils.
 */
@Component
public class SQLCountryCompanyOrganizationCodeUtils {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(SQLCountryCompanyOrganizationCodeUtils.class);

  /** The data set metabase controller zuul. */
  @Autowired
  private DatasetMetabaseController.DataSetMetabaseControllerZuul datasetMetabaseController;

  @Autowired
  private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;

  /**
   * Check if the message error contains one of the 3 codes
   *
   * @param sqlCode The sql sentence to be validated
   * @return True if contains
   */
  private boolean sqlCodeContainCodes(String sqlCode) {
    return sqlCode.contains("{%R3_COUNTRY_CODE%}")
            || sqlCode.contains("{%R3_COMPANY_CODE%}")
            || sqlCode.contains("{%R3_ORGANIZATION_CODE%}");
  }

  /**
   * If the error message contains the 3 codes, replace them with the real provider code
   *
   * @param datasetId
   * @param sqlCode The sql sentence to be validated
   */
  private String replaceCodes(Long datasetId, String sqlCode) {
    DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseController.findDatasetMetabaseById(datasetId);
    String providerCode = "XX";
    if (dataSetMetabaseVO.getDataProviderId()!=null && dataSetMetabaseVO.getDataProviderId()!=0) {
      DataProviderVO provider = representativeControllerZuul.findDataProviderById(dataSetMetabaseVO.getDataProviderId());
      providerCode = provider.getCode();
    }
    return sqlCode
            .replace("{%R3_COUNTRY_CODE%}", providerCode)
            .replace("{%R3_COMPANY_CODE%}", providerCode)
            .replace("{%R3_ORGANIZATION_CODE%}", providerCode);
  }

  /**
   * Applies provider-related code replacements to the SQL string when needed
   * If the SQL contains any of the predefined R3 codes, this method
   * retrieves the provider code for the given dataset and replaces all matching
   * placeholders. If no placeholders are present, the original SQL is returned.
   *
   * @param datasetId
   * @param sqlCode   sql string to check and optionally modify
   * @return the updated or original SQL sentence (with provider codes applied if placeholders exist)
   */
  public String replaceCodesIfNeeded(Long datasetId, String sqlCode) {
    if (sqlCodeContainCodes(sqlCode)) {
      return replaceCodes(datasetId, sqlCode);
    }
    return sqlCode;
  }
}
