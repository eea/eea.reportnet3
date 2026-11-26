package org.eea.validation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

/** Tests for {@link ValidationFullMessagesHelper} (ATA2-3359). */
@RunWith(MockitoJUnitRunner.class)
public class ValidationFullMessagesHelperTest {

  private static final String VALIDATION_FOLDER = "df-0000001/dp-0000002/ds-0000005/current/validation";
  private static final String FULL_MESSAGES_FOLDER = VALIDATION_FOLDER + "-full-messages/";

  @InjectMocks
  private ValidationFullMessagesHelper validationFullMessagesHelper;

  @Mock
  private S3Helper s3Helper;

  @Mock
  private S3Service s3Service;

  private S3PathResolver validationResolver;

  private RuleVO ruleVO;

  @Before
  public void setUp() {
    validationResolver = new S3PathResolver(1L, 2L, 5L, "validation");
    ruleVO = new RuleVO();
    ruleVO.setShortCode("BIG01");
    ruleVO.setRuleId("6afc229488bfea12e051e4ba");
    Mockito.lenient().when(s3Helper.getS3Service()).thenReturn(s3Service);
    Mockito.lenient().when(s3Service.getTableAsFolderQueryPath(any(), anyString()))
        .thenReturn(VALIDATION_FOLDER);
  }

  @Test
  public void shortenMessageKeepsShortMessage() {
    Map<String, String> fullMessages = new HashMap<>();
    assertEquals("short", ValidationFullMessagesHelper.shortenMessage("short", fullMessages, 1000));
    assertTrue(fullMessages.isEmpty());
  }

  @Test
  public void shortenMessageCutsLongMessageAndKeepsFullText() {
    Map<String, String> fullMessages = new HashMap<>();
    String fullMessage = "x".repeat(1500);

    String shortMessage =
        ValidationFullMessagesHelper.shortenMessage(fullMessage, fullMessages, 1000);

    assertEquals("x".repeat(1000) + ValidationFullMessagesHelper.SHORTENED_MESSAGE_SUFFIX,
        shortMessage);
    assertEquals(fullMessage, fullMessages.get(shortMessage));
    assertTrue(ValidationFullMessagesHelper.isShortened(shortMessage));
  }

  @Test
  public void shortenMessageDisabledWhenMaxLengthNotPositive() {
    Map<String, String> fullMessages = new HashMap<>();
    String fullMessage = "x".repeat(1500);
    assertEquals(fullMessage, ValidationFullMessagesHelper.shortenMessage(fullMessage, fullMessages, 0));
    assertTrue(fullMessages.isEmpty());
  }

  @Test
  public void shortenMessageNull() {
    assertEquals(null, ValidationFullMessagesHelper.shortenMessage(null, new HashMap<>(), 1000));
    assertFalse(ValidationFullMessagesHelper.isShortened(null));
  }

  @Test
  public void restoreMessageGivesFullTextAndKeepsTrailingSuffix() {
    Map<String, String> fullMessages = new HashMap<>();
    String fullMessage = "y".repeat(1200);
    String shortMessage =
        ValidationFullMessagesHelper.shortenMessage(fullMessage, fullMessages, 1000);

    assertEquals(fullMessage,
        ValidationFullMessagesHelper.restoreMessage(shortMessage, fullMessages));
    assertEquals(fullMessage + " (OMISSION)",
        ValidationFullMessagesHelper.restoreMessage(shortMessage + " (OMISSION)", fullMessages));
  }

  @Test
  public void restoreMessageLeavesUnknownMessages() {
    Map<String, String> fullMessages = new HashMap<>();
    String notShortened = "a normal message";
    String unknownShortened = "z" + ValidationFullMessagesHelper.SHORTENED_MESSAGE_SUFFIX;
    assertEquals(notShortened,
        ValidationFullMessagesHelper.restoreMessage(notShortened, fullMessages));
    fullMessages.put("other", "other full");
    assertEquals(unknownShortened,
        ValidationFullMessagesHelper.restoreMessage(unknownShortened, fullMessages));
  }

  @Test
  public void fullMessagesKeysAreNextToTheValidationFolder() {
    assertEquals(FULL_MESSAGES_FOLDER,
        validationFullMessagesHelper.getFullMessagesFolderKey(validationResolver));
    assertEquals(FULL_MESSAGES_FOLDER + "BIG01-4ba.json",
        validationFullMessagesHelper.getFullMessagesFileKey(ruleVO, validationResolver));
  }

  @Test
  public void uploadFullMessagesWritesJson() throws Exception {
    Map<String, String> fullMessages = new HashMap<>();
    fullMessages.put("short", "full");
    Mockito.doAnswer(invocation -> {
      String json = new String(Files.readAllBytes(Paths.get((String) invocation.getArgument(1))),
          StandardCharsets.UTF_8);
      assertEquals("{\"short\":\"full\"}", json);
      return null;
    }).when(s3Helper).uploadFileToBucket(eq(FULL_MESSAGES_FOLDER + "BIG01-4ba.json"), anyString());

    validationFullMessagesHelper.uploadFullMessages(ruleVO, validationResolver, fullMessages);

    verify(s3Helper).uploadFileToBucket(eq(FULL_MESSAGES_FOLDER + "BIG01-4ba.json"), anyString());
  }

  @Test
  public void uploadFullMessagesNothingToUpload() throws Exception {
    validationFullMessagesHelper.uploadFullMessages(ruleVO, validationResolver, new HashMap<>());
    verify(s3Helper, never()).uploadFileToBucket(anyString(), anyString());
  }

  @Test
  public void readFullMessagesReadsAllFilesOfTheShortCode() throws Exception {
    String key = FULL_MESSAGES_FOLDER + "BIG01-4ba.json";
    List<ObjectIdentifier> objects = Arrays.asList(ObjectIdentifier.builder().key(key).build(),
        ObjectIdentifier.builder().key(FULL_MESSAGES_FOLDER + "BIG01-4ba.tmp").build());
    when(s3Helper.listObjectsInBucket(FULL_MESSAGES_FOLDER + "BIG01-")).thenReturn(objects);
    when(s3Helper.getBytesFromS3(key)).thenReturn(
        new ObjectMapper().writeValueAsBytes(Collections.singletonMap("short", "full")));

    Map<String, String> fullMessages =
        validationFullMessagesHelper.readFullMessages(validationResolver, "BIG01");

    assertEquals(Collections.singletonMap("short", "full"), fullMessages);
  }

  @Test
  public void readFullMessagesUnreadableFileIsSkipped() {
    String key = FULL_MESSAGES_FOLDER + "BIG01-4ba.json";
    when(s3Helper.listObjectsInBucket(FULL_MESSAGES_FOLDER + "BIG01-"))
        .thenReturn(Collections.singletonList(ObjectIdentifier.builder().key(key).build()));
    when(s3Helper.getBytesFromS3(key)).thenReturn("not json".getBytes(StandardCharsets.UTF_8));

    assertTrue(validationFullMessagesHelper.readFullMessages(validationResolver, "BIG01").isEmpty());
  }

  @Test
  public void readFullMessagesBlankShortCode() {
    assertTrue(validationFullMessagesHelper.readFullMessages(validationResolver, " ").isEmpty());
    verify(s3Helper, never()).listObjectsInBucket(anyString());
  }

  @Test
  public void deleteFullMessagesFolderDeletesEveryFile() {
    when(s3Helper.listObjectsInBucket(FULL_MESSAGES_FOLDER)).thenReturn(
        Arrays.asList(ObjectIdentifier.builder().key(FULL_MESSAGES_FOLDER + "A-001.json").build(),
            ObjectIdentifier.builder().key(FULL_MESSAGES_FOLDER + "B-002.json").build()));

    validationFullMessagesHelper.deleteFullMessagesFolder(validationResolver);

    verify(s3Helper).deleteFileFromS3(FULL_MESSAGES_FOLDER + "A-001.json");
    verify(s3Helper).deleteFileFromS3(FULL_MESSAGES_FOLDER + "B-002.json");
  }

  @Test
  public void deleteFullMessagesFileErrorIsNotThrown() {
    when(s3Helper.listObjectsInBucket(FULL_MESSAGES_FOLDER + "BIG01-4ba.json"))
        .thenThrow(new RuntimeException("S3 down"));

    validationFullMessagesHelper.deleteFullMessagesFile(ruleVO, validationResolver);

    verify(s3Helper, never()).deleteFileFromS3(anyString());
  }
}
