import React, { useReducer } from 'react';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import isNil from 'lodash/isNil';

const validationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_OPEN_QC_CREATION_MODAL':
      return {
        ...state,
        isVisible: true,
        opener: null
      };
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_OPENER':
      return {
        ...state,
        isVisible: true,
        opener: payload.opener,
        level: payload.level
      };
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_FIELD':
      return {
        ...state,
        isVisible: true,
        referenceId: payload.fieldId,
        tableSchemaId: payload.tableSchemaId,
        opener: null,
        level: 'field'
      };
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_ROW':
      return {
        ...state,
        isVisible: true,
        referenceId: payload.recordId,
        opener: null,
        level: 'row'
      };
    case 'ON_OPENER_RESET':
      return {
        ...state,
        opener: null,
        reOpenOpener: false
      };
    case 'ON_CLOSE_QC_CREATION_MODAL':
      return {
        ...state,
        isVisible: false,
        referenceId: null,
        tableSchemaId: null,
        reOpenOpener: !isNil(state.opener) ? true : false,
        ruleEdit: false,
        ruleToEdit: {}
      };
    case 'ON_OPEN_TO_EDIT':
      console.log({ payload });
      return {
        ...state,
        isVisible: true,
        referenceId: payload.referenceId,
        opener: payload.opener,
        ruleEdit: true,
        ruleToEdit: payload.ruleToEdit,
        level: payload.level
      };
    case 'RESET_REOPEN_OPENER':
      return {
        ...state,
        reOpenOpener: false
      };
    default:
      return state;
  }
};

const initialState = {
  isVisible: false,
  referenceId: null,
  opener: null,
  level: null,
  reOpenOpener: false,
  ruleEdit: false
};
export const ValidationProvider = ({ children }) => {
  const [state, dispatch] = useReducer(validationReducer, initialState);
  return (
    <ValidationContext.Provider
      value={{
        ...state,
        onOpenModal: () => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL'
          });
        },
        onOpenModalFromOpener: (level, opener) => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL_FROM_OPENER',
            payload: {
              opener,
              level
            }
          });
        },
        onOpenModalFromField: (fieldId, tableSchemaId) => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL_FROM_FIELD',
            payload: { fieldId, tableSchemaId }
          });
        },
        onOpenModalFromRow: recordId => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL_FROM_ROW',
            payload: { recordId }
          });
        },
        onCloseModal: () => {
          dispatch({
            type: 'ON_CLOSE_QC_CREATION_MODAL'
          });
        },
        onResetOpener: () => {
          dispatch({
            type: 'ON_OPENER_RESET'
          });
        },
        onOpenToEdit: (rule, opener, level) => {
          console.log({ rule });
          dispatch({
            type: 'ON_OPEN_TO_EDIT',
            payload: {
              ruleToEdit: { ...rule },
              referenceId: rule.referenceId,
              opener,
              level
            }
          });
        },
        resetReOpenOpener: () => {
          dispatch({
            type: 'RESET_REOPEN_OPENER'
          });
        }
      }}>
      {children}
    </ValidationContext.Provider>
  );
};
