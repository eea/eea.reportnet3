import uniqueId from 'lodash/uniqueId';

export const getEmptyLink = () => {
  const linkId = uniqueId();
  return {
    linkId,
    originField: '',
    referencedField: ''
  };
};
