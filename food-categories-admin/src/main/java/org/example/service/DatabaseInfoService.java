package org.example.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseInfoService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInfoService.class);

    private final DataSource primaryDataSource;
    private final DataSource foodCategoriesDataSource;

    @Value("${spring.datasource.primary.url}")
    private String primaryDbUrl;

    @Value("${spring.datasource.url}")
    private String foodCategoriesDbUrl;

    public DatabaseInfoService(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                              @Qualifier("foodCategoriesDataSource") DataSource foodCategoriesDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.foodCategoriesDataSource = foodCategoriesDataSource;
    }

    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> result = new HashMap<>();
        
        // Check Primary Database (DB1)
        Map<String, Object> db1Info = new HashMap<>();
        db1Info.put("name", "Primary Database (Calorie Tracker)");
        db1Info.put("url", sanitizeUrl(primaryDbUrl));
        
        try {
            List<String> tables = getTableList(primaryDataSource);
            db1Info.put("status", "SUCCESS");
            db1Info.put("tables", tables);
            db1Info.put("tableCount", tables.size());
            logger.info("Successfully retrieved {} tables from primary database", tables.size());
        } catch (Exception e) {
            db1Info.put("status", "ERROR");
            db1Info.put("error", e.getMessage());
            db1Info.put("tables", new ArrayList<String>());
            db1Info.put("tableCount", 0);
            logger.error("Failed to retrieve tables from primary database: {}", e.getMessage());
        }
        
        // Check Food Categories Database (DB2)
        Map<String, Object> db2Info = new HashMap<>();
        db2Info.put("name", "Food Categories Database");
        db2Info.put("url", sanitizeUrl(foodCategoriesDbUrl));
        
        try {
            List<String> tables = getTableList(foodCategoriesDataSource);
            db2Info.put("status", "SUCCESS");
            db2Info.put("tables", tables);
            db2Info.put("tableCount", tables.size());
            logger.info("Successfully retrieved {} tables from food categories database", tables.size());
        } catch (Exception e) {
            db2Info.put("status", "ERROR");
            db2Info.put("error", e.getMessage());
            db2Info.put("tables", new ArrayList<String>());
            db2Info.put("tableCount", 0);
            logger.error("Failed to retrieve tables from food categories database: {}", e.getMessage());
        }
        
        result.put("db1", db1Info);
        result.put("db2", db2Info);
        
        return result;
    }

    private List<String> getTableList(DataSource dataSource) throws SQLException {
        List<String> tables = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Get all tables and views
            try (ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    String tableType = resultSet.getString("TABLE_TYPE");
                    
                    // Skip system tables
                    if (!tableName.toLowerCase().startsWith("pg_") && 
                        !tableName.toLowerCase().startsWith("information_schema") &&
                        !tableName.toLowerCase().startsWith("sql_")) {
                        tables.add(tableName + " (" + tableType + ")");
                    }
                }
            }
        }
        
        return tables;
    }

    private String sanitizeUrl(String url) {
        // Remove password from URL for display purposes
        if (url != null && url.contains("@")) {
            String[] parts = url.split("@");
            if (parts.length > 1) {
                String hostPart = parts[1];
                String schemePart = parts[0];
                if (schemePart.contains("://")) {
                    String scheme = schemePart.substring(0, schemePart.indexOf("://") + 3);
                    String userPart = schemePart.substring(schemePart.indexOf("://") + 3);
                    if (userPart.contains(":")) {
                        String user = userPart.substring(0, userPart.indexOf(":"));
                        return scheme + user + ":***@" + hostPart;
                    }
                }
            }
        }
        return url;
    }
} 