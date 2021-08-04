const parseParentPath = (parentPath, location) => {
  const splittedParentPath = parentPath.split('/');

  const splittedPath = location.pathname?.split('/');
  let newPath = '';
  splittedParentPath.forEach((token, i) => {
    if (token.includes(':')) {
      newPath = `${newPath}/${splittedPath[i]}`;
    } else if (token !== '') {
      newPath = `${newPath}/${token}`;
    }
  });
  return newPath;
};

export const RouteUtils = {
  parseParentPath
};
