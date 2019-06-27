import React, { useState, useEffect, useContext } from "react";
import { withRouter } from "react-router-dom";
import { BreadCrumb } from "primereact/breadcrumb";
import { Button } from "primereact/button";
import { SplitButton } from "primereact/splitbutton";
import { SplitButtonNE } from "../../Layout/UI/SplitButtonNE/SplitButtonNE";
import jsonDataSchema from "../../../assets/jsons/datosDataSchema3.json";
//import jsonDataSchemaErrors from '../../../assets/jsons/errorsDataSchema.json';
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from "./ReportingDataFlow.module.css";
import ResourcesContext from "../../Context/ResourcesContext";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import IconComponent from "../../Layout/UI/icon-component";
import config from "../../../conf";

const ReportingDataFlow = props => {
	const resources = useContext(ResourcesContext);
	/* const [breadCrumbItems, setBreadCrumbItems] = useState([]); */
	const [redirect, setRedirect] = useState(false);
	const [redirectTo, setRedirectTo] = useState("");

	console.log("ReportingDataFlow Render...");

	const home = { icon: resources.icons["home"], url: "/" };

	/* useEffect(() => {
		console.log("ReportingDataFlow useEffect");

		setBreadCrumbItems([
			{ label: resources.messages["AcceptedDF"], url: "#" },
			{ label: resources.messages["DFReporting"], url: "#" }
		]);
	}, [resources.messages]); */

	/* let items = [
		{ label: "New", icon: "pi pi-fw pi-plus" },
		{ label: "Delete", icon: "pi pi-fw pi-trash" }
	]; */

	const { nameDataSetSchema } = jsonDataSchema;

	const handleRedirect = target => {
		props.history.push(target);
	};

	return (
		<div>
			<MainLayout>
				<div className="titleDiv">
					<BreadCrumb
						model={[{ label: "Reporting data flow", url: "" }]}
						home={home}
					/>
				</div>
				<div className="rep-container">
					<div className="rep-row">
						<DataFlowColumn
							navTitle="data flows"
							dataFlowTitle={nameDataSetSchema}
							search={true}
						/>
						<div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
							<h2 className={styles.title}>
								<IconComponent icon={config.icons.shoppingCart} />
								{nameDataSetSchema}
							</h2>

							<div className={`${styles.buttonsWrapper}`}>
								<div>
									<Button
										label="DO"
										className="p-button-warning"
										onClick={e => {
											handleRedirect("/documentation-data-set");
										}}
									/>
									<p className="caption">Documents</p>
								</div>
								<div className={styles.buttonwrapper}>
									<SplitButtonNE
										label="NE"
										className="p-button-primary"
										handleRedirect={handleRedirect}
									/>
									<p className="caption">New dataset</p>
								</div>
							</div>
						</div>
					</div>
				</div>
			</MainLayout>
		</div>
	);
};

export default withRouter(ReportingDataFlow);
