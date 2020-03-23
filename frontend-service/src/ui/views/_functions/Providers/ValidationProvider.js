import React, { useReducer } from 'react';

import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';
import isNil from 'lodash/isNil';
const validationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_OPEN_QC_CREATION_MODAL':
      return {
        ...state,
        isVisible: true,
        opener: payload.opener
      };
    case 'ON_OPEN_QC_CREATION_MODAL_FROM_FIELD':
      return {
        ...state,
        isVisible: true,
        fieldId: payload.fieldId
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
        onOpenModal: (opener = null) => {
          dispatch({
            type: 'ON_OPEN_QC_CREATION_MODAL',
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
        }
      }}>
      {children}
    </ValidationContext.Provider>
  );
};
