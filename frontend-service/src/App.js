import React, { useState } from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import styles from "./App.module.css";

import ReporterDataSet from "./components/Pages/ReporterDataSet/ReporterDataSet";
import ResourcesContext from "./components/Context/ResourcesContext";
import langResources from "./conf/messages.en.json";
import iconsResources from "./conf/prime.icons.json";
import ReportingDataFlow from "./components/Pages/ReportingDataFlow/ReportingDataFlow";
import DataFlowTasks from './components/Pages/DataFlowTasks/DataFlowTasks'

const App = () => {
	const [resources] = useState({ ...langResources, ...iconsResources });
	return (
		<div className={styles.App}>
			<ResourcesContext.Provider value={resources}>
				<Router>
					<Switch>
						<Route exact path="/" component={ReportingDataFlow} />
						<Route exact path="/dataFlow" component={DataFlowTasks} />
						<Route exact path="/ReporterDataSet" component={ReporterDataSet} />
						
					</Switch>
				</Router>
			</ResourcesContext.Provider>
		</div>
	);
};


export default App;
