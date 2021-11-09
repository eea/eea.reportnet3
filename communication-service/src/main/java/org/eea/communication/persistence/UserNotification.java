package org.eea.communication.persistence;

import java.util.Date;
import java.util.Map;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UserNotification.
 */
@Getter
@Setter
@ToString
@Document(collection = "UserNotification")
public class UserNotification {

  /** The notification schema id. */
  @Id
  @Field(value = "_id")
  private ObjectId notificationSchemaId;

  /** The user id. */
  @Field(value = "userId")
  private String userId;

  /** The event type. */
  @Field(value = "eventType")
  private String eventType;

  /** The insert date. */
  @Field(value = "insertDate")
  private Date insertDate;

  /** The dataflow id. */
  @Field(value = "dataflowId")
  private Long dataflowId;

  /** The dataflow name. */
  @Field(value = "dataflowName")
  private String dataflowName;

  /** The provider id. */
  @Field(value = "providerId")
  private Long providerId;

  /** The data provider name. */
  @Field(value = "dataProviderName")
  private String dataProviderName;

  /** The dataset id. */
  @Field(value = "datasetId")
  private Long datasetId;

  /** The dataset name. */
  @Field(value = "datasetName")
  private String datasetName;

  /** The type status. */
  @Field(value = "typeStatus")
  private TypeStatusEnum typeStatus;

  /** The custom content. */
  @Field(value = "customContent")
  private Map<String, String> customContent;

  /** The type. */
  @Field(value = "type")
  private DatasetTypeEnum type;

  /** The table schema name. */
  @Field(value = "tableSchemaName")
  private String tableSchemaName;

  /** The file name. */
  @Field(value = "fileName")
  private String fileName;
}
