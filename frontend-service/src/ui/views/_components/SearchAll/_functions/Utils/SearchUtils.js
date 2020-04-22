import isNil from 'lodash/isNil';

const getSearchKeys = data => {
  if (!isNil(data)) return Object.keys(data).filter(item => item !== 'id');
};

const onApplySearch = (data, value, typeData) => [
  ...data.filter(data => {
    if (typeData == 'qc') {
      return (
        data['name'].toLowerCase().includes(value.toLowerCase()) ||
        data['description'].toLowerCase().includes(value.toLowerCase())
      );
    } else {
      return (
        data['title'].toLowerCase().includes(value.toLowerCase()) ||
        data['legalInstrument'].toLowerCase().includes(value.toLowerCase()) ||
        data['dueDate'].toLowerCase().includes(value.toLowerCase())
      );
    }
  })
];

export const SearchUtils = { onApplySearch };
