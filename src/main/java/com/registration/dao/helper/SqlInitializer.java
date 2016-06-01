package com.registration.dao.helper;

import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.File;
import java.sql.Connection;

public class SqlInitializer {
    final static String CREATE_USER_TABLE = "src/main/resources/scripts/createTable.sql";
    public static void initializeDatabase(Connection connection) {
        ScriptUtils.executeSqlScript(connection, new FileSystemResource(new File(CREATE_USER_TABLE).getAbsolutePath()));
    }

}
