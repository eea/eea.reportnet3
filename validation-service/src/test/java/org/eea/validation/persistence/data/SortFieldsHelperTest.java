package org.eea.validation.persistence.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SortFieldsHelperTest {

  @Test
  public void testSetSortingField() {
    SortFieldsHelper.setSortingField("");
  }

  @Test
  public void testGetSortingField() {
    SortFieldsHelper.getSortingField();
  }

  @Test
  public void testCleanSortingField() {
    SortFieldsHelper.cleanSortingField();
  }

}
