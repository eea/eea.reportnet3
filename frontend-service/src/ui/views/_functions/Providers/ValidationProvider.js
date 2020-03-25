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
        opener: payload.opener
      };
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_FIELD':
      return {
        ...state,
        isVisible: true,
        fieldId: payload,
        opener: null
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
        fieldId: null,
        reOpenOpener: !isNil(state.opener) ? true : false
      };
    case 'ON_OPEN_TO_EDIT':
      return {
        ...state,
        isVisible: true,
        fieldId: payload.fieldId,
        opener: payload.opener
      };
    case 'RESET_REOPENOPENER':
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
  fieldId: null
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
        onOpenModalFronOpener: opener => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL_FROM_OPENER',
            payload: {
              opener
            }
          });
        },
        onOpenModalFromField: fieldId => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL_FROM_FIELD',
            payload: fieldId
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
        onOpenToEdit: (fieldId, opener) => {
          dispatch({
            type: 'ON_OPEN_TO_EDIT',
            payload: {
              fieldId,
              opener
            }
          });
        },
        resetReOpenOpener: () => {
          dispatch({
            type: 'RESET_REOPENOPENER'
          });
        }
      }}>
      {children}
    </ValidationContext.Provider>
  );
};
