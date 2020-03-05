import { capitalize } from 'lodash';

export const fieldDesignerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_PK':
      return { ...state, fieldPKValue: payload };
    case 'SET_REQUIRED':
      return { ...state, fieldRequiredValue: payload };
    case 'RESET_NEW_FIELD':
      return { ...state, fieldRequiredValue: false };
    case 'SET_INITIAL_FIELD_VALUE':
      return { ...state, initialFieldValue: payload };
    default:
      return state;
  }
};
