import React, { Fragment } from "react";
import Navigation from "../Navigation/Navigation";
import Footer from "./Footer/Footer";
import styles from "./MainLayout.module.css";

const MainLayout = ({ children }) => (
	<Fragment>
		<Navigation />
		<div className={styles.mainContent}>{children}</div>
		<Footer />
	</Fragment>
);
export default MainLayout;
