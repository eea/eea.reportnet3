const initialState = {
  counter: 0,
  isOpen: false
};

const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'INCREMENT':
      return { ...state, counter: state.counter + 1 };

    case 'DECREMENT':
      return { ...state, counter: state.counter - 1 };
  }
};

const Counter = () => {
  const [counterState, counterDispatch] = useReducer(reducer, initialState);
  return (
    <>
      <h1>{counterState.counter}</h1>
      <button onClick={() => counterDispatch({ type: 'INCREMENT' })}>User Design Settings</button>
      <button onClick={() => counterDispatch({ type: 'DECREMENT' })}>User Configuration Settings</button>
    </>
  );
};

const initialState = {
  counter: 0,
  isOpen: false
};

const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'INCREMENT':
      const result = 100 ** state.counter;

      return { ...state, counter: state.counter + payload.num };

    case 'DECREMENT':
      return { ...state, counter: state.counter - payload.num };
    case 'SHOW':
      return { ...state, isOpen: (state.isOpen = payload.isOpen), counter: state.counter + payload.counter };
  }
};
const [counterState, counterDispatch] = useReducer(reducer, initialState);

console.log('counterState', counterState);
const Counter = () => {
  // const [counterState, counterDispatch] = useReducer(reducer, initialState);
  return (
    <>
      <h1>{counterState.counter}</h1>
      {counterState.isOpen && <div>HOLLLLLLLLAAA</div>}
      <button onClick={() => counterDispatch({ type: 'INCREMENT', payload: { num: 5 } })}>User Design Settings</button>
      <button onClick={() => counterDispatch({ type: 'DECREMENT', payload: { num: 5 } })}>
        User Configuration Settings
      </button>
      <button onClick={() => counterDispatch({ type: 'SHOW', payload: { isOpen: !counterState.isOpen, counter: 10 } })}>
        show div
      </button>
    </>
  );
};
