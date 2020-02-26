export const receiptReducer = (receiptState, { type, payload }) => {
  switch (type) {
    case 'INIT_DATA':
      return { ...receiptState, ...payload };

    case 'ON_DOWNLOAD':
      return { ...receiptState, isLoading: payload.isLoading, receiptData: payload.receiptData };

    case 'ON_CLEAN_UP':
      return { ...receiptState, ...payload };

    case 'ON_RELEASE_NEW_DATA':
      return { ...receiptState, isOutdated: payload.isOutdated };

    default:
      return receiptState;
  }
};
