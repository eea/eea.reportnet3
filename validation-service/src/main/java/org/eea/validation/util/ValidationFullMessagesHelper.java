package org.eea.validation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.eea.utils.LiteralConstants.DASH;
import static org.eea.utils.LiteralConstants.S3_PREPARATION_VALIDATION_TABLE_PATH;
import static org.eea.utils.LiteralConstants.S3_VALIDATION_TABLE_PATH;
import static org.eea.utils.LiteralConstants.SLASH;

/**
 * Keeps the full text of validation messages that were shortened before saving (Big Data).
 *
 * <p>Dremio cannot read or group very long values (field size limit, hash aggregation), so a long
 * QC message breaks "Show validations" and the validation export for the whole dataset
 * (ATA2-3359). The saved message is shortened; the full text goes to a JSON file per rule in S3,
 * in a folder next to the validation folder (e.g. {@code .../current/validation-full-messages/}),
 * which Dremio never reads. The export reads it back directly from S3.
 */
@Component
public class ValidationFullMessagesHelper {

  /** Added to a message that was shortened before saving it. */
  public static final String SHORTENED_MESSAGE_SUFFIX =
      " [shortened, use Download validations for the full text]";

  /** Suffix of the S3 folder, next to the validation folder, that holds the full messages. */
  private static final String FULL_MESSAGES_FOLDER_SUFFIX = "-full-messages";

  /** Extension of the full messages files. */
  private static final String FULL_MESSAGES_FILE_EXTENSION = ".json";

  private static final Logger LOG = LoggerFactory.getLogger(ValidationFullMessagesHelper.class);

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private S3Helper s3Helper;

  /**
   * Shortens a message longer than maxLength: keeps the first maxLength characters and adds
   * {@link #SHORTENED_MESSAGE_SUFFIX}. The full text is stored in fullMessages, by short message.
   *
   * @param message the message
   * @param fullMessages the full messages of the rule, by short message
   * @param maxLength the maximum message length; 0 or less disables shortening
   * @return the message to save
   */
  public static String shortenMessage(String message, Map<String, String> fullMessages,
      int maxLength) {
    if (message == null || maxLength <= 0 || message.length() <= maxLength) {
      return message;
    }
    String shortMessage = message.substring(0, maxLength) + SHORTENED_MESSAGE_SUFFIX;
    fullMessages.putIfAbsent(shortMessage, message);
    return shortMessage;
  }

  /**
   * Gives back the full text of a shortened message. Anything after the suffix, e.g. " (OMISSION)",
   * is kept.
   *
   * @param message the saved message
   * @param fullMessages the full messages, by short message
   * @return the full message, or the saved message if it was not shortened or is not found
   */
  public static String restoreMessage(String message, Map<String, String> fullMessages) {
    if (message == null || fullMessages == null || fullMessages.isEmpty()) {
      return message;
    }
    int suffixIndex = message.indexOf(SHORTENED_MESSAGE_SUFFIX);
    if (suffixIndex < 0) {
      return message;
    }
    String shortMessage = message.substring(0, suffixIndex + SHORTENED_MESSAGE_SUFFIX.length());
    String fullMessage = fullMessages.get(shortMessage);
    return fullMessage == null ? message : fullMessage + message.substring(shortMessage.length());
  }

  /**
   * Tells if a saved message was shortened.
   *
   * @param message the message
   * @return true if shortened
   */
  public static boolean isShortened(String message) {
    return message != null && message.contains(SHORTENED_MESSAGE_SUFFIX);
  }

  /**
   * Gets the S3 folder that holds the full messages of a dataset, next to the validation folder.
   *
   * @param validationResolver the validation resolver
   * @return the folder key, ending with a slash
   */
  public String getFullMessagesFolderKey(S3PathResolver validationResolver) {
    final String path = StringUtils.isNotBlank(validationResolver.getPreparationCode())
        ? S3_PREPARATION_VALIDATION_TABLE_PATH
        : S3_VALIDATION_TABLE_PATH;
    return s3Helper.getS3Service().getTableAsFolderQueryPath(validationResolver, path)
        + FULL_MESSAGES_FOLDER_SUFFIX + SLASH;
  }

  /**
   * Gets the S3 key of the full messages file of one rule: "shortCode-last 3 chars of ruleId.json",
   * the same name as the rule's validation folder.
   *
   * @param ruleVO the rule
   * @param validationResolver the validation resolver
   * @return the file key
   */
  public String getFullMessagesFileKey(RuleVO ruleVO, S3PathResolver validationResolver) {
    final int ruleIdLength = ruleVO.getRuleId().length();
    return getFullMessagesFolderKey(validationResolver) + ruleVO.getShortCode() + DASH
        + ruleVO.getRuleId().substring(ruleIdLength - 3, ruleIdLength)
        + FULL_MESSAGES_FILE_EXTENSION;
  }

  /**
   * Uploads the full text of the messages that were shortened for one rule, as a JSON map
   * "short message -> full message". Does nothing if no message was shortened.
   *
   * @param ruleVO the rule
   * @param validationResolver the validation resolver
   * @param fullMessages the full messages, by short message
   * @throws IOException if the temporary file cannot be written
   */
  public void uploadFullMessages(RuleVO ruleVO, S3PathResolver validationResolver,
      Map<String, String> fullMessages) throws IOException {
    if (fullMessages == null || fullMessages.isEmpty()) {
      return;
    }
    final String s3Key = getFullMessagesFileKey(ruleVO, validationResolver);
    final Path tempFile = Files.createTempFile("full-messages-", FULL_MESSAGES_FILE_EXTENSION);
    try {
      objectMapper.writeValue(tempFile.toFile(), fullMessages);
      s3Helper.uploadFileToBucket(s3Key, tempFile.toString());
      LOG.info("Uploaded {} full validation message(s) for rule {} to {}", fullMessages.size(),
          ruleVO.getShortCode(), s3Key);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Reads the full messages of the rules with the given short code: "short message -> full
   * message". Reads S3 directly, not through Dremio, so the Dremio size limits do not apply.
   *
   * @param validationResolver the validation resolver
   * @param shortCode the rule short code
   * @return the full messages, empty if there are none or they cannot be read
   */
  public Map<String, String> readFullMessages(S3PathResolver validationResolver,
      String shortCode) {
    final Map<String, String> fullMessages = new HashMap<>();
    if (StringUtils.isBlank(shortCode)) {
      return fullMessages;
    }
    final String prefix = getFullMessagesFolderKey(validationResolver) + shortCode + DASH;
    for (ObjectIdentifier object : s3Helper.listObjectsInBucket(prefix)) {
      if (!object.key().endsWith(FULL_MESSAGES_FILE_EXTENSION)) {
        continue;
      }
      try {
        fullMessages.putAll(objectMapper.readValue(s3Helper.getBytesFromS3(object.key()),
            new TypeReference<Map<String, String>>() {}));
      } catch (Exception e) {
        LOG.error("Error reading full validation messages from {}. Message: {}", object.key(),
            e.getMessage());
      }
    }
    return fullMessages;
  }

  /**
   * Deletes the full messages file of one rule, if it exists.
   *
   * @param ruleVO the rule
   * @param validationResolver the validation resolver
   */
  public void deleteFullMessagesFile(RuleVO ruleVO, S3PathResolver validationResolver) {
    deleteWithPrefix(getFullMessagesFileKey(ruleVO, validationResolver));
  }

  /**
   * Deletes all the full messages files of a dataset, if any.
   *
   * @param validationResolver the validation resolver
   */
  public void deleteFullMessagesFolder(S3PathResolver validationResolver) {
    deleteWithPrefix(getFullMessagesFolderKey(validationResolver));
  }

  /**
   * Deletes every S3 object whose key starts with the prefix. Errors are logged, not thrown: a
   * left-over file is harmless and must not stop a validation.
   *
   * @param prefix the prefix
   */
  private void deleteWithPrefix(String prefix) {
    try {
      for (ObjectIdentifier object : s3Helper.listObjectsInBucket(prefix)) {
        s3Helper.deleteFileFromS3(object.key());
      }
    } catch (Exception e) {
      LOG.error("Error deleting full validation messages with prefix {}. Message: {}", prefix,
          e.getMessage());
    }
  }
}
