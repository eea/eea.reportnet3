import isNil from 'lodash/isNil';

const getSearchKeys = data => {
  if (!isNil(data)) return Object.keys(data).filter(item => item !== 'id');
};

const onApplySearch = (data, value) => [
  ...data.filter(data => {
    return (
      data['title'].toLowerCase().includes(value.toLowerCase()) ||
      data['legalInstrument'].toLowerCase().includes(value.toLowerCase()) ||
      data['dueDate'].toLowerCase().includes(value.toLowerCase())
    );
  })
];

export const SearchUtils = { onApplySearch };
