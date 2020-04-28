import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const getSearchKeys = data => {
  if (!isNil(data)) return Object.keys(data).filter(item => item !== 'id' && item !== 'key');
};

const onApplySearch = (data, searchBy = [], value) => [
  ...data.filter(data => {
    const searchedParams = !isEmpty(searchBy) ? searchBy : getSearchKeys(data);
    const filteredData = [];
    for (let index = 0; index < searchedParams.length; index++) {
      filteredData.push(data[searchedParams[index]].toLowerCase().includes(value.toLowerCase()));
    }
    return filteredData.includes(true);
  })
];

export const SearchUtils = { onApplySearch };
