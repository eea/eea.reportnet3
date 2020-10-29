// import { config } from 'conf';

import { apiFeedback } from 'core/infrastructure/api/domain/model/Feedback';

import { Feedback } from 'core/domain/model/Feedback/Feedback';

const create = async (dataflowId, message, providerId) => {
  const created = await apiFeedback.create(dataflowId, message, providerId);
  return created;
};

const loadMessages = async (dataProviderId, page) => {
  const response = await apiFeedback.loadMessages(dataProviderId, page);
  console.log(response);
  const messagesDTO = response.map(
    message =>
      new Feedback({ content: message.content, datetime: message.datetime, id: message.id, read: message.read })
  );

  return messagesDTO;
  // const data = [
  //   {
  //     id: 1000,
  //     message: 'This is the first message',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: false
  //   },
  //   {
  //     id: 1001,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: false
  //   },
  //   {
  //     id: 1002,
  //     message:
  //       'This is a message. Please read it bla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blablabla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: false
  //   },
  //   {
  //     id: 1003,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: false
  //   },
  //   {
  //     id: 1004,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: true
  //   },
  //   {
  //     id: 1005,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: true
  //   },
  //   {
  //     id: 1006,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: false
  //   },
  //   {
  //     id: 1007,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: false
  //   },
  //   {
  //     id: 1008,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: false
  //   },
  //   {
  //     id: 1009,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: true
  //   },
  //   {
  //     id: 1010,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: true
  //   },
  //   {
  //     id: 1011,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: false
  //   },
  //   {
  //     id: 1012,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: true
  //   },
  //   {
  //     id: 1013,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: true
  //   },
  //   {
  //     id: 1014,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: true
  //   },
  //   {
  //     id: 1015,
  //     message: 'This is another message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-14',
  //     sender: false
  //   },
  //   {
  //     id: 1016,
  //     message: 'This is a message. Please read it bla bla blabla',
  //     read: true,
  //     datetime: '2015-09-13',
  //     sender: false
  //   }
  // ];
  // let messages = [];
  // const slicedData = data.slice(first, data.length);
  // for (let i = 0; i < slicedData.length; i++) {
  //   messages.push(new Feedback({ ...slicedData[i] }));
  // }
  // return messages;
};

const loadMessagesByFlag = async () => {
  return await apiFeedback.loadMessagesByFlag();
};

const markAsRead = async (dataflowId, messageIds, read) => {
  const messages = messageIds.map(messageId => {
    return { id: messageId, read };
  });
  const updated = await apiFeedback.markAsRead(dataflowId, messages);
  return updated;
};

export const ApiFeedbackRepository = {
  create,
  loadMessages,
  loadMessagesByFlag,
  markAsRead
};
