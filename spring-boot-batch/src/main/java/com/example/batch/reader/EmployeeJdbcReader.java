package com.example.batch.reader;

import com.example.batch.model.EmployeeSyncItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EmployeeJdbcReader {

    private static final Logger log = LoggerFactory.getLogger(EmployeeJdbcReader.class);
    private static final String SELECT_QUERY = 
        "SELECT id, first_name, last_name, email, department, position, salary, hire_date, is_active FROM employees";

    @Bean
    public JdbcPagingItemReader<EmployeeSyncItem> employeeItemReader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<EmployeeSyncItem> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(100);
        reader.setRowMapper(new EmployeeRowMapper());
        reader.setQueryProvider(pagingQueryProvider(dataSource));
        reader.setSaveState(true);
        return reader;
    }

    @Bean
    public PagingQueryProvider pagingQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause(SELECT_QUERY);
        factoryBean.setFromClause("FROM employees");
        factoryBean.setWhereClause("WHERE is_active = true");
        factoryBean.setSortKey("id");
        
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        factoryBean.setSortKeys(sortKeys);
        
        return factoryBean.getObject();
    }

    private static class EmployeeRowMapper implements org.springframework.jdbc.core.RowMapper<EmployeeSyncItem> {
        @Override
        public EmployeeSyncItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            EmployeeSyncItem item = new EmployeeSyncItem();
            item.setId(rs.getLong("id"));
            item.setFirstName(rs.getString("first_name"));
            item.setLastName(rs.getString("last_name"));
            item.setEmail(rs.getString("email"));
            item.setDepartment(rs.getString("department"));
            item.setPosition(rs.getString("position"));
            item.setSalary(rs.getBigDecimal("salary"));
            item.setHireDate(rs.getDate("hire_date").toLocalDate());
            item.setIsActive(rs.getBoolean("is_active"));
            item.setOperation("SYNC");
            return item;
        }
    }
}
