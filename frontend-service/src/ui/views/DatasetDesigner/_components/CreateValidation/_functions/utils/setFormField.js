export const setFormField = (field, creationFormDispatch) => {
  creationFormDispatch({
    type: 'SET_FORM_FIELD',
    payload: field
  });
};
