import React, { useContext } from "react";
import UserCard from "../Layout/UI/UserCard/UserCard";
import logo from "../../assets/images/logo.png";
import styles from "./Navigation.module.css";
import ResourcesContext from "../Context/ResourcesContext";

const Navigation = () => {
	const resources = useContext(ResourcesContext);
	return (
		<div id="header" className={styles.header}>
			<a
				href="#home"
				className={styles.appLogo}
				title={resources.messages["titleHeader"]}
			>
				<img
					height="50px"
					src={logo}
					alt="Reportnet"
					className={styles.appLogo}
				/>
				<h1 className={styles.appTitle}>{resources.messages["titleHeader"]}</h1>
			</a>
			<UserCard />
		</div>
	);
};

export default Navigation;
