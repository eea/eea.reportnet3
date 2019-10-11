/*
 * 
 */
package org.eea.validation.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ForeingKeyDroolsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForeingKeyDroolsTest {

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Test is query data WDF protect 1.
   */
  @Test
  public void testIsQueryDataWDFProtect1() {

    ForeingKeyDrools.isQueryDataWDFProtect("DEBB_PR_0183");
  }

  /**
   * Test is query data WDF protect 2.
   */
  @Test
  public void testIsQueryDataWDFProtect2() {

    ForeingKeyDrools.isQueryDataWDFProtect("value");

  }

  /**
   * Test is query data WDF protect 3.
   */
  @Test
  public void testIsQueryDataWDFProtect3() {

    ForeingKeyDrools.isQueryDataWDFProtect("DKBW1178");
  }

  /**
   * Test is query data WDF protect 4.
   */
  @Test
  public void testIsQueryDataWDFProtect4() {

    ForeingKeyDrools.isQueryDataWDFProtect("ELBW149292185101");
  }

  /**
   * Test is query data WDF protect 5.
   */
  @Test
  public void testIsQueryDataWDFProtect5() {

    ForeingKeyDrools.isQueryDataWDFProtect("FI124980001");
  }

  /**
   * Test is query data WDF protect 6.
   */
  @Test
  public void testIsQueryDataWDFProtect6() {

    ForeingKeyDrools.isQueryDataWDFProtect("FR281202070D030680");
  }

  /**
   * Test is query data WDF protect 7.
   */
  @Test
  public void testIsQueryDataWDFProtect7() {

    ForeingKeyDrools.isQueryDataWDFProtect("HRBWC-COAST-HR3-7146");
  }

  /**
   * Test is query data WDF protect 8.
   */
  @Test
  public void testIsQueryDataWDFProtect8() {

    ForeingKeyDrools.isQueryDataWDFProtect("IT009046013001");
  }

  /**
   * Test is query data WDF protect 10.
   */
  @Test
  public void testIsQueryDataWDFProtect10() {

    ForeingKeyDrools.isQueryDataWDFProtect("IT018078108004");
  }

  /**
   * Test is query data WDF protect 11.
   */
  @Test
  public void testIsQueryDataWDFProtect11() {

    ForeingKeyDrools.isQueryDataWDFProtect("AL101");
  }

  /**
   * Test is query data WDF protect 12.
   */
  @Test
  public void testIsQueryDataWDFProtect12() {

    ForeingKeyDrools.isQueryDataWDFProtect("DENI_PR_TK25_2211_01");
  }

  /**
   * Test is query data WDF protect 13.
   */
  @Test
  public void testIsQueryDataWDFProtect13() {

    ForeingKeyDrools.isQueryDataWDFProtect("ELBW069219036101");
  }

  /**
   * Test is query data WDF protect 14.
   */
  @Test
  public void testIsQueryDataWDFProtect14() {

    ForeingKeyDrools.isQueryDataWDFProtect("ES614C0982051");
  }

  /**
   * Test is query data WDF protect 15.
   */
  @Test
  public void testIsQueryDataWDFProtect15() {

    ForeingKeyDrools.isQueryDataWDFProtect("FR252203044M029108");
  }

  /**
   * Test is query data WDF protect 16.
   */
  @Test
  public void testIsQueryDataWDFProtect16() {

    ForeingKeyDrools.isQueryDataWDFProtect("FRD075001");
  }

  /**
   * Test is query data WDF protect 17.
   */
  @Test
  public void testIsQueryDataWDFProtect17() {

    ForeingKeyDrools.isQueryDataWDFProtect("IETW_SW_2004_0042");
  }

  /**
   * Test is query data WDF protect 18.
   */
  @Test
  public void testIsQueryDataWDFProtect18() {

    ForeingKeyDrools.isQueryDataWDFProtect("IT015063031003");
  }

  /**
   * Test is query data WDF protect 19.
   */
  @Test
  public void testIsQueryDataWDFProtect19() {

    ForeingKeyDrools.isQueryDataWDFProtect("IT020090058004");
  }

  /**
   * Test is query data WDF protect 20.
   */
  @Test
  public void testIsQueryDataWDFProtect20() {

    ForeingKeyDrools.isQueryDataWDFProtect("SE0441261000000076");
  }


}
