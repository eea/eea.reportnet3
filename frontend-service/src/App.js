import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';

import styles from './App.module.css';

import { DataFlowTasks } from 'ui/views/DataFlowTasks/DataFlowTasks';
import { DocumentationDataSet } from 'ui/views/DocumentationDataSet/DocumentationDataSet';
import { Login } from 'ui/views/Login';
import { ReporterDataSet } from 'ui/views/ReporterDataSet/ReporterDataSet';
import { ReportingDataFlow } from 'ui/views/ReportingDataFlow/ReportingDataFlow';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import iconsResources from 'assets/conf/prime.icons.json';
import langResources from 'assets/conf/messages.en.json';

const App = () => {
  const [resources] = useState({ ...langResources, ...iconsResources });
  return (
    <div className={styles.app}>
      <ResourcesContext.Provider value={resources}>
        <Router>
          <Switch>
            <Route exact path="/" component={Login} />
            <Route path="/data-flow-task/" component={DataFlowTasks} />
            <Route exact path="/reporting-data-flow/:dataFlowId" component={ReportingDataFlow} />
            <Route path="/reporting-data-flow/:dataFlowId/reporter-data-set/:dataSetId" component={ReporterDataSet} />
            <Route path="/reporting-data-flow/:dataFlowId/documentation-data-set/" component={DocumentationDataSet} />
          </Switch>
        </Router>
      </ResourcesContext.Provider>
    </div>
  );
};

export default App;
