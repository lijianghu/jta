package davenkin.step1_failure;

import static junit.framework.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import davenkin.BankFixture;

/**
 * Created with IntelliJ IDEA.
 * User: davenkin
 * Date: 2/5/13
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class FailureBankServiceTest extends BankFixture {
  @Test
  public void transferSuccess() throws SQLException {
    FailureBankDao failureBankDao = new FailureBankDao(dataSource);
    FailureInsuranceDao failureInsuranceDao = new FailureInsuranceDao(dataSource);

    FailureBankService bankService = new FailureBankService(dataSource);
    bankService.setFailureBankDao(failureBankDao);
    bankService.setFailureInsuranceDao(failureInsuranceDao);

    bankService.transfer(1111, 2222, 200);
    int bankAmount = getBankAmount(1111);
    assertEquals(800, bankAmount);
    assertEquals(1200, getInsuranceAmount(2222));

  }

  @Test
  public void transferFailure() throws SQLException {
    FailureBankDao failureBankDao = new FailureBankDao(dataSource);
    FailureInsuranceDao failureInsuranceDao = new FailureInsuranceDao(dataSource);

    FailureBankService bankService = new FailureBankService(dataSource);
    bankService.setFailureBankDao(failureBankDao);
    bankService.setFailureInsuranceDao(failureInsuranceDao);

    int toNonExistId = 3333;
    bankService.transfer(1111, toNonExistId, 200);

    assertEquals(1000, getInsuranceAmount(2222));// 银行没有增加金额，用户的钱少了
    assertEquals(800, getBankAmount(1111));
  }
}
