package org.eea.validation.persistence.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SortFieldsHelperTest {

  @Test
  public void testSetSortingField() {
    SortFieldsHelper.setSortingField("");
    assertEquals("assertion error", "", SortFieldsHelper.getSortingField());
  }

  @Test
  public void testGetSortingField() {
    SortFieldsHelper.setSortingField("");
    assertEquals("assertion error", "", SortFieldsHelper.getSortingField());
  }

  @Test
  public void testCleanSortingField() {
    SortFieldsHelper.cleanSortingField();
    assertNull("assertion error", SortFieldsHelper.getSortingField());
  }

}
