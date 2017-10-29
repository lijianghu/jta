package davenkin;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import javax.jms.JMSException;
import javax.sql.DataSource;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created with IntelliJ IDEA.
 * User: davenkin
 * Date: 3/14/13
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml", "classpath:testContext.xml" })
public class DefaultOrderServiceTest {
  @Autowired
  private OrderService orderService;

  @Autowired
  private DataSource dataSource;

  @Autowired
  @Qualifier("testJmsTemplate")
  private JmsTemplate jmsTemplate;

  private static BrokerService broker;

  @BeforeClass
  public static void startEmbeddedActiveMq() throws Exception {
    broker = new BrokerService();
    broker.addConnector("tcp://localhost:61616");
    broker.start();
  }

  @AfterClass
  public static void stopEmbeddedActiveMq() throws Exception {
    broker.stop();
  }

  @Before
  public void clearQueue() throws JMSException {
    jmsTemplate.setReceiveTimeout(1000);
    while (jmsTemplate.receive() != null)
      ;
  }

  @Test
  public void makeOrder() {
    orderService.makeOrder(createOrder());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    assertEquals(1, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM USER_ORDER"));
    String dbItemName = jdbcTemplate.queryForObject("SELECT ITEM_NAME FROM USER_ORDER", String.class);
    String messageItemName = ((Order) jmsTemplate.receiveAndConvert()).getItemName();
    assertEquals(dbItemName, messageItemName);
  }

  @Test(expected = IllegalArgumentException.class)
  public void failToMakeOrder() {
    // 事务控制:其中消息已经发送,db操作错误.
    // 如果没有事务,消息是可以接受到一条的,如果有事务,则不能接收到.
    // 此例子验证了分布式事务,没有接收到消息,去掉事务注解之后可以接受到
    orderService.makeOrder(null);
    // 事务结束后,查询消息
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    assertEquals(0, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM USER_ORDER"));
    assertNull(jmsTemplate.receiveAndConvert());
  }

  private Order createOrder() {
    Order order = new Order();
    order.setBuyerName("davenkin");
    order.setItemName(randomName());
    order.setMailAddress("chengdu");
    order.setPrice(randomPrice());
    return order;
  }

  private String randomName() {
    return "book" + System.currentTimeMillis();
  }

  private double randomPrice() {
    String randomTimeString = String.valueOf(System.currentTimeMillis());
    return Double.parseDouble(randomTimeString.substring(randomTimeString.length() - 3));
  }
}
