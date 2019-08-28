package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ForeingKeyDroolsTest {

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void isQueryDataWDFProtect() {
    ForeingKeyDrools.isQueryDataWDFProtect("1234");
  }

  @Test
  public void isQueryDataWDFProtectPart1() {
    ForeingKeyDrools.isQueryDataWDFProtect("AL202");
  }

  @Test
  public void isQueryDataWDFProtectPart2() {
    ForeingKeyDrools.isQueryDataWDFProtect("ES614M0062204");
  }

  @Test
  public void isQueryDataWDFProtectPart3() {
    ForeingKeyDrools.isQueryDataWDFProtect("IEWEBWC160_0000_0100");
  }

  @Test
  public void isQueryDataWDFProtectPart4() {
    ForeingKeyDrools.isQueryDataWDFProtect("SE0441262000000324");
  }

  @Test
  public void isSameRecordTrue() {
    RecordValue recordValue = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    List<FieldValue> fieldValueList = new ArrayList<>();
    fieldValue.setValue("value");
    fieldValueList.add(fieldValue);
    recordValue.setFields(fieldValueList);
    ForeingKeyDrools.isInSameRecord("value", recordValue, 0, "value");
  }

  @Test
  public void isSameRecordFalse() {
    RecordValue recordValue = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    List<FieldValue> fieldValueList = new ArrayList<>();
    fieldValue.setValue("value");
    fieldValueList.add(fieldValue);
    recordValue.setFields(fieldValueList);
    ForeingKeyDrools.isInSameRecord("value", recordValue, 0, "value not equals");
  }
}
