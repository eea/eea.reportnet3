package org.eea.validation.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DateDroolsUtilsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DateDroolsUtilsTest {


  /** The date drools utils. */
  @InjectMocks
  private static DateDroolsUtils dateDroolsUtils;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Checks if is interval year.
   */
  @Test
  public void isIntervalYear() {
    DateDroolsUtils.isIntervalYear("3", 4, 5, false);
  }

  /**
   * Checks if is interval year 2.
   */
  @Test
  public void isIntervalYear2() {
    DateDroolsUtils.isIntervalYear("3", 6, 4, false);
  }

  /**
   * Checks if is interval year throw.
   */
  @Test
  public void isIntervalYearThrow() {
    DateDroolsUtils.isIntervalYear("", null, null, false);
  }

  /**
   * Checks if is interval year good.
   */
  @Test
  public void isIntervalYearGood() {
    DateDroolsUtils.isIntervalYear("3", 6, 4, true);
  }

  /**
   * Checks if is interval year good 2.
   */
  @Test
  public void isIntervalYearGood2() {
    DateDroolsUtils.isIntervalYear("3", 3, 4, true);
  }

  /**
   * Actual date compare test.
   */
  @Test
  public void actualDateCompareTest() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "MORE");
  }

  /**
   * Actual date compare test 2.
   */
  @Test
  public void actualDateCompareTest2() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "LESS");
  }

  /**
   * Actual date compare test 3.
   */
  @Test
  public void actualDateCompareTest3() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "MORE/EQUALS");
  }

  /**
   * Actual date compare test 4.
   */
  @Test
  public void actualDateCompareTest4() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "LESS/EQUALS");
  }

  /**
   * Actual date compare test 5.
   */
  @Test
  public void actualDateCompareTest5() {
    DateDroolsUtils.actualDateCompare("2018-11-04", "MORE");

  }
}
