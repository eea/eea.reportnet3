export const setValidationRelation = (linkId, field, links) => {
  const [targetRelation] = links.filter(link => linkId === link.linkId);

  const { value } = field;
  if (value === null) {
    targetRelation[field.key] = '';
  } else {
    targetRelation[field.key] = value;
  }

  return links;
};
