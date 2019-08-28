package org.eea.validation.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DateDroolsUtilsTest {


  @InjectMocks
  private static DateDroolsUtils dateDroolsUtils;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void isIntervalYear() {
    DateDroolsUtils.isIntervalYear("3", 4, 5, false);
  }

  @Test
  public void isIntervalYear2() {
    DateDroolsUtils.isIntervalYear("3", 6, 4, false);
  }

  @Test
  public void isIntervalYearThrow() {
    DateDroolsUtils.isIntervalYear("", null, null, false);
  }

  @Test
  public void isIntervalYearGood() {
    DateDroolsUtils.isIntervalYear("3", 6, 4, true);
  }

  @Test
  public void isIntervalYearGood2() {
    DateDroolsUtils.isIntervalYear("3", 3, 4, true);
  }

  @Test
  public void actualDateCompareTest() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "MORE");
  }

  @Test
  public void actualDateCompareTest2() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "LESS");
  }

  @Test
  public void actualDateCompareTest3() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "MORE/EQUALS");
  }

  @Test
  public void actualDateCompareTest4() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "LESS/EQUALS");
  }

  @Test
  public void actualDateCompareTest5() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "MORE");

  }
}
