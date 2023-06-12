package org.eea.dataset.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/dremio")
@RestController
public class TestDremioControllerImpl {

    @Qualifier("dremioJdbcTemplate")
    @Autowired
    JdbcTemplate dremioJdbcTemplate;

    @GetMapping("run")
    public void run() {
        SqlRowSet rs = dremioJdbcTemplate.queryForRowSet("SELECT * FROM rn3-dataset.rn3-dataset.\"tab.csv\"");
        while (rs.next()) {
            System.out.println(rs.getString("A") + "," + rs.getString("B") + "," + rs.getString("C") + "," + rs.getString("D"));
        }
    }
}
