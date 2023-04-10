package com.bridgelabz;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollDBIOService {

    private PreparedStatement employeePayrollDataStatement;
    private static EmployeePayrollDBIOService employeePayrollDBIOService;

    private EmployeePayrollDBIOService() {
    }

    public static EmployeePayrollDBIOService getInstance() {
        if (employeePayrollDBIOService == null)
            employeePayrollDBIOService = new EmployeePayrollDBIOService();
        return employeePayrollDBIOService;
    }

    private Connection getConnection() throws SQLException {
        String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service_DB?useSSL=false";
        String userName = "root";
        String password = "akshay@11";
        Connection connection;
        System.out.println("Connecting to database:" + jdbcURL);
        connection = DriverManager.getConnection(jdbcURL, userName, password);
        System.out.println("Connection is successful" + connection);
        return connection;
    }

    public List<EmployeePayrollData> readData() {
        String sql = "select * from employee_payroll";
        return this.getEmployeePayrollDataUsingDB(sql);
    }


    public List<EmployeePayrollData> getEmployeePayrollForDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = String.format("select * from employee_payroll where start between '%s' and '%s';",Date.valueOf(startDate), Date.valueOf(endDate));
        return this.getEmployeePayrollDataUsingDB(sql);
    }


    public Map<String, Double> getAverageSalaryByGender() {
        String sql = "select gender, avg(salary) as salary from employee_payroll group by gender;";
        return this.getSalaryGroupedByGender(sql);
    }


    public Map<String, Double> getSumSalaryByGender() {
        String sql = "select gender, sum(salary) as salary from employee_payroll group by gender;";
        return this.getSalaryGroupedByGender(sql);
    }

    private Map<String, Double> getSalaryGroupedByGender(String sql) {
        Map<String, Double> genderToSalaryMap = new HashMap<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double salary = resultSet.getDouble("salary");
                genderToSalaryMap.put(gender, salary);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return genderToSalaryMap;
    }

    private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql) {
        List<EmployeePayrollData> employeePayrollList;
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            employeePayrollList = this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return employeePayrollList;
    }

    public List<EmployeePayrollData> getEmployeePayrollData(String name) {
        List<EmployeePayrollData> employeePayrollDataList = null;
        if (this.employeePayrollDataStatement == null)
            this.prepareStatementForEmployeeData();
        try {
            employeePayrollDataStatement.setString(1, name);
            ResultSet resultSet = employeePayrollDataStatement.executeQuery();
            employeePayrollDataList = this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollDataList;
    }

    private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
        List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double salary = resultSet.getDouble("salary");
                LocalDate startDate = resultSet.getDate("start").toLocalDate();
                employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollList;
    }

    private void prepareStatementForEmployeeData() {
        try {
            Connection connection = this.getConnection();
            String sql = "select * from employee_payroll where name = ?";
            employeePayrollDataStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateEmployeeData(String name, double salary) {
        return this.updateEmployeeDataUsingStatement(name, salary);
    }

    private int updateEmployeeDataUsingStatement(String name, double salary) {
        String sql = String.format("update employee_payroll set salary = '%s' where name = '%s';", salary, name);
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
