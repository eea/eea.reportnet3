import { RecordUtils } from 'ui/views/_functions/Utils';
import { isUndefined } from 'lodash';

export const recordReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_TOTAL':
      return { ...state, totalRecords: payload };
    case 'SET_FILTERED':
      return { ...state, totalFilteredRecords: payload };
    case 'SET_FIRST_PAGE_RECORD':
      return { ...state, firstPageRecord: payload };
    case 'SET_RECORDS_PER_PAGE':
      return { ...state, recordsPerPage: payload };
    case 'SET_EDITED_RECORD':
      if (!isUndefined(payload.property)) {
        let updatedRecord = RecordUtils.changeRecordValue({ ...state.editedRecord }, payload.property, payload.value);
        return { ...state, editedRecord: updatedRecord };
      } else {
        return { ...state, editedRecord: payload, selectedRecord: payload };
      }
    case 'SET_NEW_RECORD':
      if (!isUndefined(payload.property)) {
        let updatedNewRecord = RecordUtils.changeRecordValue({ ...state.newRecord }, payload.property, payload.value);
        return { ...state, newRecord: updatedNewRecord };
      } else {
        return { ...state, newRecord: payload };
      }
    case 'IS_RECORD_DELETED':
      return { ...state, isRecordDeleted: payload };
    default:
      return state;
  }
};
