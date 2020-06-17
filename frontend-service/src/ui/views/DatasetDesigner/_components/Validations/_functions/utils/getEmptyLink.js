import uuid from 'uuid';

export const getEmptyLink = () => {
  const linkId = uuid.v4();
  return {
    linkId,
    originField: '',
    referencedField: ''
  };
};
