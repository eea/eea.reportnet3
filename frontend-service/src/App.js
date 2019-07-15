import React, { useState } from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import styles from "./App.module.css";

import ReporterDataSet from "./components/Pages/ReporterDataSet/ReporterDataSet";
import ResourcesContext from "./components/Context/ResourcesContext";
import langResources from "./conf/messages.en.json";
import iconsResources from "./conf/prime.icons.json";
import ReportingDataFlow from "./components/Pages/ReportingDataFlow/ReportingDataFlow";
import DocumentationDataSet from "./components/Pages/DocumentationDataSet/DocumentationDataSet";
import DataFlowTasks from "./components/Pages/DataFlowTasks/DataFlowTasks";
import Login from "./components/Pages/Login/Login";

const App = () => {
	const [resources] = useState({ ...langResources, ...iconsResources });
	return (
		<div className={styles.app}>
			<ResourcesContext.Provider value={resources}>
				<Router>
					<Switch>
						<Route exact path="/" component={Login} />
						<Route path="/data-flow-task/" component={DataFlowTasks} />
						<Route
              exact
							path="/reporting-data-flow/:dataFlowId"
							component={ReportingDataFlow}
						/>
            <Route 
              path="/reporting-data-flow/:dataFlowId/reporter-data-set/:dataSetId" 
              component={ReporterDataSet} 
            />
						<Route
							path="/reporting-data-flow/:dataFlowId/documentation-data-set/"
							component={DocumentationDataSet}
						/>
					</Switch>
				</Router>
			</ResourcesContext.Provider>
		</div>
	);
};

export default App;
