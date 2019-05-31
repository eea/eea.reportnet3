package org.eea.dataset.persistence.data;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SortFieldsHelperTest {

  @Test
  public void test() {
    SortFieldsHelper.setSortingField("test");
    Assert.assertEquals("not the same value", "test", SortFieldsHelper.getSortingField());
  }
}