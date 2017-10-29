package davenkin.step1_failure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Created with IntelliJ IDEA.
 * User: davenkin
 * Date: 2/7/13
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FailureInsuranceDao {
  private DataSource dataSource;

  public FailureInsuranceDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * 向银行存钱
   * 
   * @param insuranceId
   *          账户id
   * @param amount
   *          金额
   * @throws SQLException
   */
  public void deposit(int insuranceId, int amount) throws SQLException {
    Connection connection = dataSource.getConnection();
    PreparedStatement selectStatement = connection
        .prepareStatement("select insurance_amount from insurance_account where insurance_id = ?");
    selectStatement.setInt(1, insuranceId);
    ResultSet resultSet = selectStatement.executeQuery();
    resultSet.next();
    int previousAmount = resultSet.getInt(1);
    resultSet.close();
    selectStatement.close();

    int newAmount = previousAmount + amount;
    PreparedStatement updateStatement = connection
        .prepareStatement("update insurance_account set insurance_amount = ? where insurance_id = ?");
    updateStatement.setInt(1, newAmount);
    updateStatement.setInt(2, insuranceId);
    updateStatement.execute();

    updateStatement.close();
    connection.close();
  }
}
