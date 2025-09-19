import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

export const webformRecordReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'ON_FILL_FIELD': {
      const inmNewRecord = { ...state.newRecord };

      // Keep original newRecord update (used by “save new record” flow)
      if (!isNil(inmNewRecord.dataRow)) {
        const recordToUpdate = inmNewRecord.dataRow.find(
          dataRow => Object.keys(dataRow.fieldData)[0] === payload.option
        );
        if (recordToUpdate) {
          recordToUpdate.fieldData[payload.option] = payload.value;
        }
      }

      const inmRecord = { ...state.record };

      // Determine if we should reset dependents for LINK fields.
      let shouldTriggerConditional = payload.conditional;
      if (!shouldTriggerConditional && payload.field.fieldType === 'LINK') {
        const blockElementWithDependents = inmRecord.elements.find(
          el =>
            el.type === 'BLOCK' &&
            (el.elements || []).some(be => be?.referencedField?.masterConditionalFieldId === payload.option)
        );
        shouldTriggerConditional = !!blockElementWithDependents;
      }

      // Is the edit inside a BLOCK row?
      const blockElement = inmRecord.elements.find(
        el =>
          el.type === 'BLOCK' && (el.elements || []).some(be => (be.fieldSchemaId || be.fieldSchema) === payload.option)
      );

      if (blockElement && payload.field?.recordId) {
        // Update only the edited BLOCK row
        const rowIndex = blockElement.elementsRecords.findIndex(er => er.recordId === payload.field.recordId);
        const safeRowIndex = rowIndex === -1 ? 0 : rowIndex;
        const elementsRecord = blockElement.elementsRecords[safeRowIndex];

        if (elementsRecord) {
          // 2a) Update the edited field in the rendered structure
          const edited = elementsRecord.elements.find(be => (be.fieldSchemaId || be.fieldSchema) === payload.option);
          if (edited) {
            edited.value = payload.value;
            edited.fieldSchemaId = edited.fieldSchema || edited.fieldSchemaId;
          }

          // 2b) Keep this row’s fields in sync (so filters/render don’t drop values)
          if (!Array.isArray(elementsRecord.fields)) elementsRecord.fields = [];
          const fIdx = elementsRecord.fields.findIndex(f => (f.fieldSchemaId || f.fieldSchema) === payload.option);
          if (fIdx > -1) {
            elementsRecord.fields[fIdx] = { ...elementsRecord.fields[fIdx], value: payload.value };
          } else {
            elementsRecord.fields.push({
              fieldSchemaId: payload.option,
              value: payload.value,
              name: payload.field.name
            });
          }

          // 2c) If the master LINK changed, clear dependents in THIS row only
          if (shouldTriggerConditional && payload.field.fieldType === 'LINK') {
            const dependentSchemaIds = (blockElement.elements || [])
              .filter(el => el?.referencedField?.masterConditionalFieldId === payload.option)
              .map(el => el.fieldSchema || el.fieldSchemaId);

            elementsRecord.elements = elementsRecord.elements.map(el =>
              dependentSchemaIds.includes(el.fieldSchemaId || el.fieldSchema) ? { ...el, value: '' } : el
            );

            if (Array.isArray(elementsRecord.fields)) {
              elementsRecord.fields = elementsRecord.fields.map(f =>
                dependentSchemaIds.includes(f.fieldSchemaId || f.fieldSchema) ? { ...f, value: '' } : f
              );
            }
          }
        }
      } else {
        // 3) Non-BLOCK field: original behavior
        const topLevel = inmRecord.elements.find(field => field.fieldSchemaId === payload.option);
        if (topLevel) topLevel.value = payload.value;
      }

      // 4) For non-BLOCK master LINKs, keep original reset behavior
      let conditionalFieldsRecord;
      if (!blockElement && shouldTriggerConditional && payload.field.fieldType === 'LINK') {
        conditionalFieldsRecord = {
          ...inmRecord,
          elements: inmRecord.elements.map(element =>
            !((element.fieldSchema || element.fieldSchemaId) === payload.option)
              ? { ...element, value: '' }
              : { ...element, value: payload.value }
          )
        };
      }

      return {
        ...state,
        selectedField: payload.field,
        newRecord: inmNewRecord,
        record: conditionalFieldsRecord || inmRecord,
        isConditionalChanged: shouldTriggerConditional ? !state.isConditionalChanged : state.isConditionalChanged
      };
    }
    case 'GET_DELETE_ROW_ID':
      return { ...state, selectedRecordId: payload.selectedRecordId };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };

    default:
      return state;
  }
};
