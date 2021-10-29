package org.eea.validation.util;

import static org.junit.Assert.assertTrue;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class GeometryValidationUtilsTest {

  @InjectMocks
  private GeometryValidationUtils geometryValidationUtils;

  @Before
  public void initMocks() {}

  @Test
  public void isGeometryPointTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.POINT);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryLinestringTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.LINESTRING);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryPolygonTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.POLYGON);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryMultiPointTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.MULTIPOINT);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryMultilinestringTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.MULTILINESTRING);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryMultipolygongTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.MULTIPOLYGON);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryCollectionTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.GEOMETRYCOLLECTION);
    fieldValue.setValue("");
    assertTrue("not true", geometryValidationUtils.isGeometry(fieldValue));
  }

}
