package org.eea.dataset.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface FieldSchemaNoRulesMapper.
 */
@Mapper(componentModel = "spring")
public interface FieldSchemaNoRulesMapper extends IMapper<FieldSchema, FieldSchemaVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the field schema VO
   */
  @Override
  @Mapping(source = "idRecord", target = "idRecord", ignore = true)
  @Mapping(source = "headerName", target = "name")
  @Mapping(source = "referencedField", target = "referencedField", ignore = true)
  FieldSchemaVO entityToClass(FieldSchema entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the field schema
   */
  @Override
  @Mapping(source = "idRecord", target = "idRecord", ignore = true)
  @Mapping(source = "name", target = "headerName")
  @Mapping(source = "referencedField", target = "referencedField", ignore = true)
  FieldSchema classToEntity(FieldSchemaVO entity);

  /**
   * Fill id record.
   *
   * @param fieldSchema the field schema
   * @param fieldSchemaVO the field schema VO
   */
  @AfterMapping
  default void fillIds(FieldSchema fieldSchema, @MappingTarget FieldSchemaVO fieldSchemaVO) {
    fieldSchemaVO.setId(fieldSchema.getIdFieldSchema().toString());
    fieldSchemaVO.setIdRecord(fieldSchema.getIdRecord().toString());
    if (fieldSchema.getReferencedField() != null) {
      ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
      if (fieldSchema.getReferencedField().getIdDatasetSchema() != null) {
        referenced
            .setIdDatasetSchema(fieldSchema.getReferencedField().getIdDatasetSchema().toString());
      }
      if (fieldSchema.getReferencedField().getIdPk() != null) {
        referenced.setIdPk(fieldSchema.getReferencedField().getIdPk().toString());
      }
      if (fieldSchema.getReferencedField().getLabelId() != null) {
        referenced.setLabelId(fieldSchema.getReferencedField().getLabelId().toString());
      }
      if (fieldSchema.getReferencedField().getLinkedConditionalFieldId() != null) {
        referenced.setLinkedConditionalFieldId(
            fieldSchema.getReferencedField().getLinkedConditionalFieldId().toString());
      }
      if (fieldSchema.getReferencedField().getMasterConditionalFieldId() != null) {
        referenced.setMasterConditionalFieldId(
            fieldSchema.getReferencedField().getMasterConditionalFieldId().toString());
      }
      if (fieldSchema.getReferencedField().getDataflowId() != null) {
        referenced.setDataflowId(fieldSchema.getReferencedField().getDataflowId());
      }
      fieldSchemaVO.setReferencedField(referenced);
    }
  }

  /**
   * Fill id record.
   *
   * @param fieldSchemaVO the field schema VO
   * @param fieldSchema the field schema
   */
  @AfterMapping
  default void fillIds(FieldSchemaVO fieldSchemaVO, @MappingTarget FieldSchema fieldSchema) {
    fieldSchema.setIdFieldSchema(new ObjectId(fieldSchemaVO.getId()));
    fieldSchema.setIdRecord(new ObjectId(fieldSchemaVO.getIdRecord()));
    if (fieldSchemaVO.getReferencedField() != null) {
      ReferencedFieldSchema referenced = new ReferencedFieldSchema();
      referenced.setIdDatasetSchema(
          new ObjectId(fieldSchemaVO.getReferencedField().getIdDatasetSchema()));
      referenced.setIdPk(new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));
      if (StringUtils.isNotBlank(fieldSchemaVO.getReferencedField().getLabelId())) {
        referenced.setLabelId(new ObjectId(fieldSchemaVO.getReferencedField().getLabelId()));
      }
      if (StringUtils
          .isNotBlank(fieldSchemaVO.getReferencedField().getLinkedConditionalFieldId())) {
        referenced.setLinkedConditionalFieldId(
            new ObjectId(fieldSchemaVO.getReferencedField().getLinkedConditionalFieldId()));
      }
      if (StringUtils
          .isNotBlank(fieldSchemaVO.getReferencedField().getMasterConditionalFieldId())) {
        referenced.setMasterConditionalFieldId(
            new ObjectId(fieldSchemaVO.getReferencedField().getMasterConditionalFieldId()));
      }
      if (fieldSchemaVO.getReferencedField().getDataflowId() != null) {
        referenced.setDataflowId(fieldSchemaVO.getReferencedField().getDataflowId());
      }
      fieldSchema.setReferencedField(referenced);
    }
  }
}
