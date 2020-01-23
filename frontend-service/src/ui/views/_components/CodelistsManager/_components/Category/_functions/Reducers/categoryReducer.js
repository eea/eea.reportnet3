import { isUndefined } from 'lodash';

export const categoryReducer = (state, { type, payload }) => {
  const getFilterKeys = () => {
    return Object.keys(state.filter).filter(key => key !== payload.filter && key !== 'status');
  };

  const checkFilters = (filteredKeys, codelist) => {
    for (let i = 0; i < filteredKeys.length; i++) {
      console.log(codelist[filteredKeys[i]].toLowerCase().includes(state.filter[filteredKeys[i]].toLowerCase()));
      console.log(state.filter[filteredKeys[i]]);
      if (
        codelist[filteredKeys[i]].toLowerCase().includes(state.filter[filteredKeys[i]].toLowerCase()) ||
        state.filter[filteredKeys[i]] === ''
      ) {
        return true;
      }
    }
    return false;
  };

  switch (type) {
    case 'SAVE_CATEGORY':
      return {
        ...state,
        categoryDescription: payload.description,
        categoryShortCode: payload.shortCode,
        isEditingDialogVisible: false
      };
    case 'SET_CATEGORY_INPUTS':
      return {
        ...state,
        categoryDescription: payload.description ? payload.description : state.categoryDescription,
        categoryId: payload.id ? payload.id : state.categoryId,
        categoryShortCode: payload.shortCode ? payload.shortCode : state.categoryShortCode
      };
    case 'SET_CODELISTS_IN_CATEGORY':
      return { ...state, codelists: payload.data };
    case 'SET_FILTER_VALUES':
      const filteredKeys = getFilterKeys();
      console.log({ filteredKeys });

      if (payload.filter !== 'status') {
        console.log(
          ...state.codelists.filter(
            codelist =>
              codelist[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
              [...state.filter.status.map(status => status.value.toLowerCase())].includes(
                codelist.status.toLowerCase()
              ) &&
              checkFilters(filteredKeys, codelist)
          )
        );
      }

      return {
        ...state,
        isKeyFiltered: true,
        filter: { ...state.filter, [payload.filter]: payload.value },
        filteredCodelists:
          payload.filter === 'status'
            ? [
                ...state.codelists.filter(
                  codelist =>
                    [...payload.value.map(status => status.value.toLowerCase())].includes(
                      codelist.status.toLowerCase()
                    ) &&
                    codelist['name'].toLowerCase().includes(state.filter.name.toLowerCase()) &&
                    codelist['version'].toLowerCase().includes(state.filter.version.toLowerCase()) &&
                    codelist['description'].toLowerCase().includes(state.filter.description.toLowerCase())
                )
              ]
            : [
                ...state.codelists.filter(
                  codelist =>
                    codelist[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
                    [...state.filter.status.map(status => status.value.toLowerCase())].includes(
                      codelist.status.toLowerCase()
                    ) &&
                    checkFilters(filteredKeys, codelist)
                )
              ]
      };
    case 'SET_ISLOADING':
      return { ...state, isLoading: payload.loading };
    case 'TOGGLE_EDIT_DIALOG_VISIBLE':
      return { ...state, isEditingDialogVisible: payload };
    case 'TOGGLE_ADD_CODELIST_DIALOG_VISIBLE':
      return { ...state, isAddCodelistDialogVisible: payload };
    case 'TOGGLE_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteConfirmDialogVisible: payload };
    case 'TOGGLE_FILTER_DEPRECATED_CODELISTS':
      return { ...state, isFiltered: !state.isFiltered };
    case 'TOGGLE_IS_EXPANDED':
      return { ...state, isExpanded: payload };
    case 'EDIT_NEW_CODELIST':
      return { ...state, [payload.property]: payload.value };
    case 'UPDATE_EDITING_CODELISTS':
      return { ...state, codelistsInEdition: state.codelistsInEdition + payload };
    default:
      return state;
  }
};
