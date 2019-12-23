import { RecordUtils } from 'ui/views/_functions/Utils';
import { isUndefined } from 'lodash';

export const codelistReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SAVE_INITIAL_STATE':
      return { ...state, totalRecords: payload };

    // case 'SET_NEW_RECORD':
    //   if (!isUndefined(payload.property)) {
    //     let updatedNewRecord = RecordUtils.changeRecordValue({ ...state.newRecord }, payload.property, payload.value);
    //     return { ...state, newRecord: updatedNewRecord };
    //   } else {
    //     return { ...state, newRecord: payload };
    //   }
    // case 'IS_RECORD_DELETED':
    //   return { ...state, isRecordDeleted: payload };
    // case 'COPY_RECORDS':
    //   return {
    //     ...state,
    //     numCopiedRecords: RecordUtils.getNumCopiedRecords(payload.pastedData),
    //     pastedRecords: RecordUtils.getClipboardData(
    //       payload.pastedData,
    //       !isUndefined(state.pastedRecords) ? [...state.pastedRecords] : [],
    //       payload.colsSchema,
    //       {
    //         ...state.fetchedDataFirstRecord
    //       }
    //     )
    //   };
    // case 'FIRST_FILTERED_RECORD':
    //   return { ...state, fetchedDataFirstRecord: payload };
    // case 'EMPTY_PASTED_RECORDS':
    //   return { ...state, pastedRecords: [] };
    // case 'DELETE_PASTED_RECORDS': {
    //   const inmPastedRecords = [...state.pastedRecords];
    //   inmPastedRecords.splice(getRecordIdByIndex(inmPastedRecords, payload.recordIndex), 1);
    //   return { ...state, pastedRecords: inmPastedRecords };
    // }
    default:
      return state;
  }
};
