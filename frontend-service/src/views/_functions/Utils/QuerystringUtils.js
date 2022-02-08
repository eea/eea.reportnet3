const getUrlParamValue = param => {
  let value = '';
  const queryString = window.location.search;
  const params = queryString.substring(1, queryString.length).split('&');
  params.forEach(parameter => {
    if (parameter.includes(`${param}=`)) {
      value = parameter.split('=')[1];
    }
  });
  return value;
};

export const QuerystringUtils = {
  getUrlParamValue
};
