import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

export const parseDatasetRelationFromDTO = integrityVO => {
  if (!isNil(integrityVO)) {
    const relations = {
      id: integrityVO.id,
      isDoubleReferenced: !isNil(integrityVO.isDoubleReferenced) ? integrityVO.isDoubleReferenced : false,
      originDatasetSchema: integrityVO.originDatasetSchemaId,
      referencedDatasetSchema: { code: integrityVO.referencedDatasetSchemaId, label: '' },
      links: integrityVO.originFields.map((originField, i) => {
        return {
          linkId: uniqueId(),
          originField: { code: originField, label: '' },
          referencedField: { code: integrityVO.referencedFields[i], label: '' }
        };
      })
    };
    return relations;
  }
  return {};
};
