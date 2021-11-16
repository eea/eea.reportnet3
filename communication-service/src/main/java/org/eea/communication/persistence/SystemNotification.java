package org.eea.communication.persistence;

import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.communication.enums.NotificationLevelEnum;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class SystemNotification.
 */
@Getter
@Setter
@ToString
@Document(collection = "SystemNotification")
public class SystemNotification {

  /** The id. */
  @Id
  @Field(value = "_id")
  private ObjectId id;

  /** The message. */
  @Field(value = "message")
  private String message;

  /** The enabled. */
  @Field(value = "enabled")
  private boolean enabled;

  /** The level. */
  @Field(value = "level")
  private NotificationLevelEnum level;
}
