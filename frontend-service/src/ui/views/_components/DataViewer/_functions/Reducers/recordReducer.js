import { RecordUtils } from 'ui/views/_functions/Utils';
import { isUndefined } from 'lodash';

export const recordReducer = (state, { type, payload }) => {
  const getRecordIdByIndex = (tableData, recordIdx) => {
    return tableData
      .map(e => {
        return e.recordId;
      })
      .indexOf(recordIdx);
  };

  switch (type) {
    case 'COPY_RECORDS':
      return {
        ...state,
        numCopiedRecords: RecordUtils.getNumCopiedRecords(payload.pastedData),
        pastedRecords: RecordUtils.getClipboardData(
          payload.pastedData,
          !isUndefined(state.pastedRecords) ? [...state.pastedRecords] : [],
          payload.colsSchema,
          {
            ...state.fetchedDataFirstRecord
          }
        )
      };

    case 'DELETE_PASTED_RECORDS': {
      const inmPastedRecords = [...state.pastedRecords];
      inmPastedRecords.splice(getRecordIdByIndex(inmPastedRecords, payload.recordIndex), 1);
      return { ...state, pastedRecords: inmPastedRecords };
    }

    case 'EMPTY_PASTED_RECORDS':
      return { ...state, pastedRecords: [] };

    case 'FIRST_FILTERED_RECORD':
      return { ...state, fetchedDataFirstRecord: payload };

    case 'SET_EDITED_RECORD':
      if (!isUndefined(payload.property)) {
        let updatedRecord = RecordUtils.changeRecordValue({ ...state.editedRecord }, payload.property, payload.value);
        return { ...state, editedRecord: updatedRecord };
      } else {
        return {
          ...state,
          editedRecord: payload.record,
          selectedRecord: payload.record,
          initialRecordValue: RecordUtils.getInitialRecordValues(payload.record, payload.colsSchema)
        };
      }

    case 'SET_FILTERED':
      return { ...state, totalFilteredRecords: payload };

    case 'SET_FIRST_PAGE_RECORD':
      return { ...state, firstPageRecord: payload };

    case 'SET_NEW_RECORD':
      if (!isUndefined(payload.property)) {
        let updatedNewRecord = RecordUtils.changeRecordValue({ ...state.newRecord }, payload.property, payload.value);
        return { ...state, newRecord: updatedNewRecord };
      } else {
        return { ...state, newRecord: payload };
      }

    case 'IS_RECORD_DELETED':
      return { ...state, isRecordDeleted: payload };

    case 'SET_RECORDS_PER_PAGE':
      return { ...state, recordsPerPage: payload };

    case 'SET_TOTAL':
      return { ...state, totalRecords: payload };
    case 'OPEN_MAP':
      return { ...state, isMapOpen: true, mapCoordinates: payload.coordinates, selectedMapCells: payload.mapCells };
    case 'TOGGLE_MAP_VISIBILITY':
      return { ...state, isMapOpen: payload };
    default:
      return state;
  }
};
