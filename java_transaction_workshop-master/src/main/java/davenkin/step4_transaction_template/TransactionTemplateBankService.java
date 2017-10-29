package davenkin.step4_transaction_template;

import javax.sql.DataSource;

import davenkin.BankService;
import davenkin.step3_connection_holder.ConnectionHolderBankDao;
import davenkin.step3_connection_holder.ConnectionHolderInsuranceDao;

public class TransactionTemplateBankService implements BankService {
  private DataSource dataSource;
  private ConnectionHolderBankDao connectionHolderBankDao;
  private ConnectionHolderInsuranceDao connectionHolderInsuranceDao;

  public TransactionTemplateBankService(DataSource dataSource) {
    this.dataSource = dataSource;
    connectionHolderBankDao = new ConnectionHolderBankDao(dataSource);
    connectionHolderInsuranceDao = new ConnectionHolderInsuranceDao(dataSource);
  }

  public void transfer(final int fromId, final int toId, final int amount) {
    new TransactionTemplate(dataSource) {
      @Override
      protected void doJob() throws Exception {
        connectionHolderBankDao.withdraw(fromId, amount);
        connectionHolderInsuranceDao.deposit(toId, amount);
      }
    }.doJobInTransaction();
  }
}
