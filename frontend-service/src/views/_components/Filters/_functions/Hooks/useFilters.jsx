import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import { filterByAllKeys } from '../Stores/filterKeysStore';
import { filterByStore } from '../Stores/filterStore';

export const useFilters = ({ keyStore, onFilterData, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));
  const setFilterByKeyByType = useSetRecoilState(keyStore(recoilId));

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  useEffect(() => {
    setFilterByKeyByType(prevState => ({ keys: uniq([...prevState.keys, option.key]), nestedKey: option?.nestedKey }));
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  const onFilter = async value => {
    setFilterBy({ [option.key]: value });
    await onFilterData({ key: option.key, value, type: option.type });
  };

  return { filterBy, onFilter };
};
