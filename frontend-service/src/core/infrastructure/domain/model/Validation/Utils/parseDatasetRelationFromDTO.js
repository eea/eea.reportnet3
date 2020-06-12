import isNil from 'lodash/isNil';
import uuid from 'uuid';

export const parseDatasetRelationFromDTO = integrityVO => {
  console.log({ integrityVO });
  if (!isNil(integrityVO)) {
    const relations = {
      isDoubleReferenced: !isNil(integrityVO.isDoubleReferenced) ? integrityVO.isDoubleReferenced : false,
      originDatasetSchema: integrityVO.originDatasetSchemaId,
      referencedDatasetSchema: { code: integrityVO.referencedDatasetSchemaId, label: '' },
      links: integrityVO.originFields.map((originField, i) => {
        return {
          linkId: uuid.v4(),
          originField: { code: originField, label: '' },
          referencedField: { code: integrityVO.referencedFields[i], label: '' }
        };
      })
    };
    return relations;
  }
  return {};
};
