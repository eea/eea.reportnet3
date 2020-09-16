const getUrlParamValue = param => {
  let value = '';
  let queryString = window.location.search;
  const params = queryString.substring(1, queryString.length).split('&');
  params.forEach(parameter => {
    if (parameter.includes(param)) {
      value = parameter.split('=')[1];
    }
  });
  return param === 'tab' ? Number(value) : value === 'true';
};

const getWebformTabs = allTables => {
  const initialValues = {};

  const tables = allTables.map(table => table.index).filter(index => index > -1);

  const value = getUrlParamValue('tab');

  tables.forEach(table => {
    initialValues[table] = false;
    initialValues[value] = true;
  });

  return initialValues;
};

export const WebformUtils = { getUrlParamValue, getWebformTabs };
