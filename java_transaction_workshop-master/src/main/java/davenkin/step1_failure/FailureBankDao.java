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
 * Time: 7:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class FailureBankDao {
  private DataSource dataSource;

  public FailureBankDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * 根据backId取出金额
   * 
   * @param bankId
   *          账户id
   * @param amount
   *          金钱
   * @throws SQLException
   */
  public void withdraw(int bankId, int amount) throws SQLException {
    Connection connection = dataSource.getConnection();
    PreparedStatement selectStatement = connection
        .prepareStatement("select bank_amount from bank_account where bank_id = ?");
    selectStatement.setInt(1, bankId);
    ResultSet resultSet = selectStatement.executeQuery();
    resultSet.next();
    int previousAmount = resultSet.getInt(1);
    resultSet.close();
    selectStatement.close();

    int newAmount = previousAmount - amount;
    PreparedStatement updateStatement = connection
        .prepareStatement("update bank_account set bank_amount = ? where bank_id = ?");
    updateStatement.setInt(1, newAmount);
    updateStatement.setInt(2, bankId);
    updateStatement.execute();

    updateStatement.close();
    connection.close();

  }
}
