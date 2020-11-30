import isEmpty from 'lodash/isEmpty';

export const webformRecordReducer = (state, { type, payload }) => {
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
          field.elementsRecords[0].elements.filter(field => field.fieldSchemaId === payload.option)[0].value =
            payload.value;
        }

        return field.fieldSchemaId === payload.option;
      });

      if (!isEmpty(filteredRecord))
        inmRecord.elements.filter(field => field.fieldSchemaId === payload.option)[0].value = payload.value;

      return {
        ...state,
        selectedField: payload.field,
        newRecord: inmNewRecord,
        record: inmRecord,
        isConditionalChanged: payload.conditional ? !state.isConditionalChanged : state.isConditionalChanged
      };

    case 'GET_DELETE_ROW_ID':
      return { ...state, selectedRecordId: payload.selectedRecordId };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };

    default:
      return state;
  }
};
