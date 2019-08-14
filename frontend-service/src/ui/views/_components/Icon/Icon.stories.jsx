import React from 'react';

import { storiesOf } from '../../../../../.storybook/storiesOf';

import { Icon } from './Icon';

storiesOf('Icon', module)
  .add('Archive  Icon', () => <Icon icon="archive" />)
  .add('Banned Icon', () => <Icon icon="banned" />)
  .add('Cancel Icon', () => <Icon icon="cancel" />)
  .add('Camera Icon', () => <Icon icon="camera" />)
  .add('Check Icon', () => <Icon icon="check" />)
  .add('Clock Icon', () => <Icon icon="clock" />)
  .add('Clone Icon', () => <Icon icon="clone" />)
  .add('Comment Icon', () => <Icon icon="comment" />)
  .add('Dashboard Icon', () => <Icon icon="dashboard" />)
  .add('Export Icon', () => <Icon icon="export" />)
  .add('Eye Icon', () => <Icon icon="eye" />)
  .add('Filter Icon', () => <Icon icon="filter" />)
  .add('groupBy Icon', () => <Icon icon="groupBy" />)
  .add('Home Icon', () => <Icon icon="home" />)
  .add('Import Icon', () => <Icon icon="import" />)
  .add('Info Icon', () => <Icon icon="info" />)
  .add('Logout Icon', () => <Icon icon="logout" />)
  .add('Plus Icon', () => <Icon icon="plus" />)
  .add('Refresh Icon', () => <Icon icon="refresh" />)
  .add('Replay Icon', () => <Icon icon="replay" />)
  .add('Share Icon', () => <Icon icon="share" />)
  .add('Shopping Cart Icon', () => <Icon icon="shoppingCart" />)
  .add('Sort Icon', () => <Icon icon="sort" />)
  .add('Trash Icon', () => <Icon icon="trash" />)
  .add('User Icon', () => <Icon icon="user" />)
  .add('Validate Icon', () => <Icon icon="validate" />)
  .add('Warning Icon', () => <Icon icon="warning" />);
