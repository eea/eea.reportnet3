import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';

export const deleteLink = (linkId, links) => {
  const [deleteCandidate] = links.filter(expression => expression.linkId == linkId);
  if (links.length > 1) {
    const remainRules = pullAllWith(links, [deleteCandidate], isEqual);
    return remainRules;
  } else {
    const linksKey = Object.keys(deleteCandidate);
    linksKey.forEach(linkKey => {
      if (linkKey != 'linkId') {
        deleteCandidate[linkKey] = '';
      }
    });
    return [deleteCandidate];
  }
};
