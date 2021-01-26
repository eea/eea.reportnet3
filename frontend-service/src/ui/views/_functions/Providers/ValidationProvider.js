import React, { useReducer } from 'react';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import isNil from 'lodash/isNil';

const validationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_CLOSE_QC_CREATION_MODAL':
      return {
        ...state,
        isVisible: false,
        referenceId: null,
        reOpenOpener: !isNil(state.opener),
        ruleEdit: false,
        ruleToEdit: {},
        tableSchemaId: null
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
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_OPENER':
      return {
        ...state,
        isVisible: true,
        level: payload.level
      };
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_ROW':
      return {
        ...state,
        isVisible: true,
        referenceId: payload.recordId,
        opener: null,
        level: 'row'
      };
    case 'ON_OPEN_QC_CREATION_MODAL':
      return {
        ...state,
        isVisible: true,
        opener: null
      };
    case 'ON_OPEN_TO_EDIT':
      return {
        ...state,
        isVisible: true,
        referenceId: payload.referenceId,
        ruleEdit: true,
        ruleToEdit: payload.ruleToEdit,
        level: payload.level,
        updatedRuleId: payload.updatedRuleId
      };
    case 'ON_OPENER_RESET':
      return {
        ...state,
        opener: null,
        reOpenOpener: false
      };
    case 'RESET_REOPEN_OPENER':
      return {
        ...state,
        reOpenOpener: false
      };
    case 'ON_AUTOMATIC_RULE_IS_UPDATED':
      return {
        ...state,
        isAutomaticRuleUpdated: payload
      };

    case 'ON_FETCHING_DATA':
      return {
        ...state,
        isFetchingData: payload.isFetchingData,
        updatedRuleId: payload.updatedRuleId
      };
    default:
      return state;
  }
};

const initialState = {
  isFetchingData: false,
  isVisible: false,
  level: null,
  opener: null,
  referenceId: null,
  reOpenOpener: false,
  ruleEdit: false,
  updatedRuleId: null
};

export const ValidationProvider = ({ children }) => {
  const [state, dispatch] = useReducer(validationReducer, initialState);
  return (
    <ValidationContext.Provider
      value={{
        ...state,

        onCloseModal: () => {
          dispatch({ type: 'ON_CLOSE_QC_CREATION_MODAL' });
        },

        onOpenModal: () => {
          dispatch({ type: 'ON_OPEN_QC_CREATION_MODAL' });
        },

        onOpenModalFromField: (fieldId, tableSchemaId) => {
          dispatch({ type: 'ON_OPEN_QC_CREATION_MODAL_FROM_FIELD', payload: { fieldId, tableSchemaId } });
        },

        onOpenModalFromOpener: (level, opener) => {
          dispatch({ type: 'ON_OPEN_QC_CREATION_MODAL_FROM_OPENER', payload: { level } });
        },

        onOpenModalFromRow: recordId => {
          dispatch({ type: 'ON_OPEN_QC_CREATION_MODAL_FROM_ROW', payload: { recordId } });
        },

        onOpenToEdit: (rule, level) => {
          dispatch({
            type: 'ON_OPEN_TO_EDIT',
            payload: { ruleToEdit: { ...rule }, referenceId: rule.referenceId, level, updatedRuleId: rule.id }
          });
        },

        onResetOpener: () => {
          dispatch({ type: 'ON_OPENER_RESET' });
        },

        onAutomaticRuleIsUpdated: isUpdated => {
          dispatch({ type: 'ON_AUTOMATIC_RULE_IS_UPDATED', payload: isUpdated });
        },

        resetReOpenOpener: () => {
          dispatch({ type: 'RESET_REOPEN_OPENER' });
        },

        onFetchingData: (isFetchingData, updatedRuleId) => {
          dispatch({
            type: 'ON_FETCHING_DATA',
            payload: { isFetchingData, updatedRuleId }
          });
        }
      }}>
      {children}
    </ValidationContext.Provider>
  );
};
