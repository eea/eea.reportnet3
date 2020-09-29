import { RecordUtils } from 'ui/views/_functions/Utils';
import cloneDeep from 'lodash/cloneDeep';
import isUndefined from 'lodash/isUndefined';
import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';

export const recordReducer = (state, { type, payload }) => {
  const getRecordIdByIndex = (tableData, recordIdx) => {
    return tableData
      .map(e => {
        return e.recordId;
      })
      .indexOf(recordIdx);
  };

  switch (type) {
    case 'CANCEL_SAVE_MAP_NEW_POINT':
      return {
        ...state,
        isMapOpen: false,
        newPoint: '',
        newPointCRS: 'EPSG:4326'
      };
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
          },
          payload.reporting
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
    case 'SET_FIELD_IDS':
      return {
        ...state,
        selectedFieldId: payload.fieldId,
        selectedFieldSchemaId: payload.fieldSchemaId,
        selectedValidExtensions: payload.validExtensions,
        selectedMaxSize: payload.maxSize
      };
    case 'OPEN_MAP':
      return { ...state, isMapOpen: true, mapGeoJson: payload.coordinates, selectedMapCells: payload.mapCells };
    case 'SAVE_MAP_COORDINATES':
      const inmMapGeoJson = cloneDeep(state.mapGeoJson);
      const parsedInmMapGeoJson = JSON.parse(inmMapGeoJson);
      parsedInmMapGeoJson.geometry.coordinates = MapUtils.parseCoordinates(payload.split(','));
      parsedInmMapGeoJson.properties.rsid = state.newPointCRS;
      return { ...state, isMapOpen: false, mapGeoJson: JSON.stringify(parsedInmMapGeoJson), newPoint: '' };
    case 'SET_MAP_NEW_POINT':
      return {
        ...state,
        newPoint: `${payload.coordinates[0]}, ${payload.coordinates[1]}`,
        newPointCRS: payload.crs
      };
    case 'SET_MAP_CRS':
      return {
        ...state,
        crs: payload
      };
    case 'TOGGLE_MAP_VISIBILITY':
      return { ...state, isMapOpen: payload };
    default:
      return state;
  }
};
