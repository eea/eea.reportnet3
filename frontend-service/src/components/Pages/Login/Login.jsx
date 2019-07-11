import React, { useContext } from "react";
import ResourcesContext from "../../Context/ResourcesContext";
import logo from "../../../assets/images/logo.png";

import styles from "./Login.module.css";

const Login = ({ history }) => {
	const resources = useContext(ResourcesContext);
	return (
		<div className="rp-container">
			<div className={`${styles.loginBoxContainer}`}>
				<div className={`${styles.loginBox}`}>
					<div className={styles.logo}>
						<img src={logo} alt="Reportnet" />
						<h1>{resources.messages.appName}</h1>
					</div>
					<form>
						<fieldset>
							<label htmlFor="userName">
								{resources.messages.loginUserName}
							</label>
							<input
								type="text"
								placeholder={resources.messages.loginUserName}
							/>
						</fieldset>
						<fieldset>
							<label htmlFor="password">
								{resources.messages.loginPassword}
							</label>
							<input
								type="password"
								placeholder={resources.messages.loginPassword}
							/>
						</fieldset>
						<fieldset className={`${styles.buttonHolder}`}>
							<button
								className="rp-btn primary"
								onClick={() => history.push("/data-flow-task/")}
							>
								{resources.messages.loginLogin}
							</button>
						</fieldset>
					</form>
				</div>
			</div>
		</div>
	);
};

export default Login;
