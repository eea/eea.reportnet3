package org.eea.dataset.service.file;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A factory for creating FileParser objects.
 */
@Component
public class FileParserFactory implements IFileParserFactory {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  /**
   * The parse common.
   */
  @Autowired
  private FileCommonUtils fileCommon;

  /**
   * The delimiter.
   */
  @Value("${loadDataDelimiter}")
  private char delimiter;

  /**
   * The field max length.
   */
  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

  /** The batch record save. */
  @Value("${dataset.import.batchRecordSave}")
  private int batchRecordSave;

  /**
   * the field dataset Metabase Service
   */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;
  /**
   * the field representative Controller Zuul
   */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;



  /**
   * Creates a new FileParser object.
   *
   * @param mimeType the mime type
   * @param datasetId the dataset id
   *
   * @return the i file parse contextd
   */
  @Override
  public IFileParseContext createContext(String mimeType, Long datasetId, String delimiterValue) {
    FileParseContextImpl context = null;

    // Obtain the data provider code to insert into the record
    Long providerId = 0L;
    DataSetMetabaseVO metabase = datasetMetabaseService.findDatasetMetabase(datasetId);
    if (metabase.getDataProviderId() != null) {
      providerId = metabase.getDataProviderId();
    }
    DataProviderVO provider = representativeControllerZuul.findDataProviderById(providerId);

    try {
      switch (FileTypeEnum.getEnum(mimeType.toLowerCase())) {
        case CSV:

          context = new FileParseContextImpl(
              new CSVReaderStrategy(delimiterValue != null ? delimiterValue.charAt(0) : delimiter,
                  fileCommon, datasetId, fieldMaxLength, provider.getCode(), batchRecordSave));
          break;
        case XML:
          // Fill it with the xml strategy
          break;
        case XLS:
        case XLSX:
          context = new FileParseContextImpl(
              new ExcelReaderStrategy(fileCommon, datasetId, fieldMaxLength, provider.getCode()));
          break;
        default:
          break;
      }
    } catch (NullPointerException e) {
      LOG_ERROR.error("Bad mimeType: {}", mimeType, e);
    }
    return context;
  }
}
