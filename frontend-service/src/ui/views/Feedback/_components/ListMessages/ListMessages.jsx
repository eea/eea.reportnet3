import React from 'react';

import { Message } from './_components/Message';

export const ListMessages = ({ messages }) => {
  return (
    <div>
      {messages.map(message => (
        <Message message={message} />
      ))}
    </div>
  );
};
