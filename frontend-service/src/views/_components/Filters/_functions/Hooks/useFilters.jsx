import { useEffect } from 'react';
import { useRecoilState, useResetRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import { filterByAllKeys } from '../Stores/filterKeysStore';
import { filterByStore, filteredDataStore } from '../Stores/filterStore';

export const useFilters = ({ keyStore, onFilterData, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));
  const setFilterByKeyByType = useSetRecoilState(keyStore(recoilId));

  const resetFilterBy = useResetRecoilState(filterByStore(`${option.key}_${recoilId}`));
  const resetFilteredData = useResetRecoilState(filteredDataStore(recoilId));

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  useEffect(() => {
    setFilterByKeyByType(prevState => ({ keys: uniq([...prevState.keys, option.key]), nestedKey: option?.nestedKey }));
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, []);

  const onFilter = async value => {
    setFilterBy({ [option.key]: value });
    await onFilterData({ key: option.key, value, type: option.type });
  };

  const resetState = () => {
    resetFilterBy();
    // resetFilteredData();
  };

  return { filterBy, onFilter, resetState };
};
