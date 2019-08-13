import React from 'react';

import { storiesOf } from '../../../../../.storybook/storiesOf';

import { config } from 'assets/conf';
import { IconComponent } from './IconComponent';

storiesOf('IconComponent', module)
  .add('Archive  Icon', () => <IconComponent icon={config.icons.archive} />)
  .add('Banned Icon', () => <IconComponent icon={config.icons.banned} />)
  .add('Cancel Icon', () => <IconComponent icon={config.icons.cancel} />)
  .add('Camera Icon', () => <IconComponent icon={config.icons.camera} />)
  .add('Check Icon', () => <IconComponent icon={config.icons.check} />)
  .add('Clock Icon', () => <IconComponent icon={config.icons.clock} />)
  .add('Clone Icon', () => <IconComponent icon={config.icons.clone} />)
  .add('Comment Icon', () => <IconComponent icon={config.icons.comment} />)
  .add('Dashboard Icon', () => <IconComponent icon={config.icons.dashboard} />)
  .add('Export Icon', () => <IconComponent icon={config.icons.export} />)
  .add('Eye Icon', () => <IconComponent icon={config.icons.eye} />)
  .add('Filter Icon', () => <IconComponent icon={config.icons.filter} />)
  /*  .add('group-by Icon', () => <IconComponent icon={config.icons.group-by} />) */
  .add('Home Icon', () => <IconComponent icon={config.icons.home} />)
  .add('Import Icon', () => <IconComponent icon={config.icons.import} />)
  .add('Info Icon', () => <IconComponent icon={config.icons.info} />)
  .add('Logout Icon', () => <IconComponent icon={config.icons.logout} />)
  .add('Plus Icon', () => <IconComponent icon={config.icons.plus} />)
  .add('Refresh Icon', () => <IconComponent icon={config.icons.refresh} />)
  .add('Replay Icon', () => <IconComponent icon={config.icons.replay} />)
  .add('Share Icon', () => <IconComponent icon={config.icons.share} />)
  .add('Shopping Cart Icon', () => <IconComponent icon={config.icons.shoppingCart} />)
  .add('Sort Icon', () => <IconComponent icon={config.icons.sort} />)
  .add('Trash Icon', () => <IconComponent icon={config.icons.trash} />)
  .add('User Icon', () => <IconComponent icon={config.icons.user} />)
  .add('Validate Icon', () => <IconComponent icon={config.icons.validate} />)
  .add('Warning Icon', () => <IconComponent icon={config.icons.warning} />);
