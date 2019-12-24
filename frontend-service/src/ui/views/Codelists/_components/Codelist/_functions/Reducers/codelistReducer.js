import { RecordUtils } from 'ui/views/_functions/Utils';
import { isUndefined } from 'lodash';

export const codelistReducer = (state, { type, payload }) => {
  switch (type) {
    case 'EDIT_CODELIST_PROPERTIES':
      return { ...state, [payload.property]: payload.value };
    case 'RESET_INITIAL_VALUES':
      console.log(payload.items[0]);
      return { ...payload };
    default:
      return state;
  }
};
