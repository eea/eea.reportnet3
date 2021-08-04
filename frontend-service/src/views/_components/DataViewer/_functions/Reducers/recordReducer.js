import cloneDeep from 'lodash/cloneDeep';
import isUndefined from 'lodash/isUndefined';

import { MapUtils } from 'views/_functions/Utils/MapUtils';
import { RecordUtils } from 'views/_functions/Utils';

import { TextUtils } from 'repositories/_utils/TextUtils';

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

    case 'DISABLE_SAVE_BUTTON': {
      return { ...state, isSaveDisabled: payload.disable };
    }

    case 'EMPTY_PASTED_RECORDS':
      return { ...state, pastedRecords: [] };

    case 'FIRST_FILTERED_RECORD':
      return { ...state, fetchedDataFirstRecord: payload };

    case 'IS_RECORD_DELETED':
      return { ...state, isRecordDeleted: payload };

    case 'RESET_CONDITIONAL_FIELDS':
      const inmRecord = payload.isNewRecord ? { ...state.newRecord } : { ...state.editedRecord };
      let recordChanged = false;
      payload.referencedFields.forEach(referencedField => {
        const recordField = inmRecord.dataRow.find(r => Object.keys(r.fieldData)[0] === referencedField.field);
        if (recordField.fieldData[referencedField.field] !== '') {
          RecordUtils.changeRecordValue(inmRecord, referencedField.field, '');
          recordChanged = true;
        }
      });
      if (recordChanged) {
        return {
          ...state,
          editedRecord: !payload.isNewRecord ? inmRecord : state.editedRecord,
          newRecord: payload.isNewRecord ? inmRecord : state.newRecord
        };
      } else {
        return { ...state };
      }

    case 'RESET_DRAW_ELEMENTS':
      return {
        ...state,
        drawElements: {
          circle: false,
          circlemarker: false,
          polyline: false,
          marker: false,
          point: false,
          polygon: false,
          rectangle: false
        }
      };

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

    case 'OPEN_MAP':
      const inmDrawElements = { ...state.drawElements };
      switch (payload.fieldType.toLowerCase()) {
        case 'linestring':
          inmDrawElements['polyline'] = true;
          break;
        case 'point':
          inmDrawElements['point'] = false;
          break;
        default:
          inmDrawElements[payload.fieldType.toLowerCase()] = true;
          break;
      }
      return {
        ...state,
        drawElements: inmDrawElements,
        geometryType: payload.fieldType.toUpperCase(),
        isMapOpen: true,
        mapGeoJson: payload.coordinates,
        selectedMapCells: payload.mapCells
      };
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

    case 'SAVE_MAP_COORDINATES':
      const inmMapGeoJson = cloneDeep(state.mapGeoJson);
      const parsedInmMapGeoJson = JSON.parse(inmMapGeoJson);
      parsedInmMapGeoJson.geometry.coordinates = MapUtils.parseCoordinates(TextUtils.splitByChar(payload));
      parsedInmMapGeoJson.properties.srid = state.newPointCRS;
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
