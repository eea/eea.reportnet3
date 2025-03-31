import isEmpty from 'lodash/isEmpty';

export const pamsWebformRecordReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'ON_FILL_FIELD':
      const inmNewRecord = { ...state.newRecord };

      inmNewRecord.dataRow.filter(data => Object.keys(data.fieldData)[0] === payload.option)[0].fieldData[
        payload.option
      ] = payload.value;

      const inmRecord = { ...state.record };

      const filteredRecord = inmRecord.elements.filter(field => {
        if (field.type === 'BLOCK') {
          return field.elements.filter(
            fieldRecord =>
              fieldRecord.fieldId === payload.option ||
              fieldRecord.fieldSchema === payload.option ||
              fieldRecord.fieldSchemaId === payload.option
          );
        }
        return field.fieldSchemaId === payload.option;
      });

      if (!isEmpty(filteredRecord)) {
        if (!isEmpty(inmRecord.elements.filter(field => field.fieldSchemaId === payload.option))) {
          inmRecord.elements.filter(
            field =>
              field.fieldId === payload.option ||
              field.fieldSchema === payload.option ||
              field.fieldSchemaId === payload.option
          )[0].value = payload.value;
        } else {
          inmRecord.elements
            .find(field => {
              if (field.type === 'BLOCK') {
                return field.elements.find(
                  blockField => (blockField.fieldId || blockField.fieldSchema) === payload.option
                );
              }
              return undefined;
            })
            .elementsRecords.find(elementRecord => elementRecord.recordId === payload.field.recordId)
            .elements.find(
              blockElement =>
                blockElement.fieldId === payload.option ||
                blockElement.fieldSchema === payload.option ||
                blockElement.fieldSchemaId === payload.option
            ).value = payload.value;
        }
      }

      let dependantConditionalFieldId;
      let isDependantConditionalField = false;

      if (payload.conditional && payload.field.fieldType === 'LINK') {
        if (!isEmpty(payload.field?.dependency)) {
          isDependantConditionalField = true;
          dependantConditionalFieldId = payload.field.fieldSchema || payload.field.fieldSchemaId;
        }
      }

      return {
        ...state,
        selectedField: payload.field,
        newRecord: inmNewRecord,
        record: inmRecord,
        conditionalFieldChange: payload.conditional ? !state.conditionalFieldChange : state.conditionalFieldChange,
        isConditionalChanged: payload.conditional,
        isDependantConditionalField,
        dependantConditionalFieldId
      };

    case 'GET_DELETE_ROW_ID':
      return { ...state, selectedRecordId: payload.selectedRecordId };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };

    default:
      return state;
  }
};
