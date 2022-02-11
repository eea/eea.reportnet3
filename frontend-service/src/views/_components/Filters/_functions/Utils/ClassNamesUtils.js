const getPanelClassName = recoilId => {
  if (
    recoilId !== 'reporting' &&
    recoilId !== 'business' &&
    recoilId !== 'citizenScience' &&
    recoilId !== 'reference'
  ) {
    return undefined;
  }

  return recoilId;
};

export const ClassNamesUtils = { getPanelClassName };
