export const webformRecordReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'ON_FILL_FIELD':
      const inmNewRecord = { ...state.newRecord };

      const newRecordField = inmNewRecord.dataRow.find(data => Object.keys(data.fieldData)[0] === payload.option);

      if (newRecordField) {
        newRecordField.fieldData[payload.option] = payload.value;
      }

      const inmRecord = { ...state.record };

      // Recursively search for the field inside nested containers (e.g. BLOCK, SECTION)
      const findElement = elements => {
        for (const element of elements) {
          if (
            element.fieldId === payload.option ||
            element.fieldSchema === payload.option ||
            element.fieldSchemaId === payload.option
          ) {
            return element;
          }

          // Search inside record-based containers (BLOCK)
          if (element.elementsRecords) {
            for (const elementRecord of element.elementsRecords) {
              if (elementRecord.recordId === payload.field.recordId) {
                const nestedElement = findElement(elementRecord.elements);

                if (nestedElement) {
                  return nestedElement;
                }
              }
            }
          }

          // Search inside simple containers (SECTION)
          if (element.elements) {
            const nestedElement = findElement(element.elements);

            if (nestedElement) {
              return nestedElement;
            }
          }
        }

        return undefined;
      };

      const recordElement = findElement(inmRecord.elements);

      if (recordElement) {
        recordElement.value = payload.value;
      }

      return {
        ...state,
        selectedField: payload.field,
        newRecord: inmNewRecord,
        record: inmRecord,
        conditionalFieldChange: payload.conditional ? !state.conditionalFieldChange : state.conditionalFieldChange
      };

    case 'GET_DELETE_ROW_ID':
      return { ...state, selectedRecordId: payload.selectedRecordId };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };

    default:
      return state;
  }
};
