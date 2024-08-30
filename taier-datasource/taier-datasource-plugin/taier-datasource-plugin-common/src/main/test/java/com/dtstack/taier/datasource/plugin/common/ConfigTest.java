package com.dtstack.taier.datasource.plugin.common;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import org.junit.Test;

public class ConfigTest {


    @Test
    public void testConfig() {
        System.setProperty("TAIER_CONF_DIR", "/Users/jialiangcai/Personal/opensource/taier/conf");
        TaierConf conf = new TaierConf().loadFileDefaults();
        System.out.println(conf.get(TaierConf.FRONTEND_BIND_PORT));
        System.out.println(conf.get(TaierConf.ENGINE_JDBC_FETCH_SIZE));
        System.out.println(conf.get(TaierConf.OPERATION_IDLE_TIMEOUT));
        System.out.println(conf.get(TaierConf.SERVER_EXEC_POOL_SHUTDOWN_TIMEOUT));
        System.out.println(conf.get(TaierConf.SERVER_EXEC_KEEPALIVE_TIME));
        System.out.println(conf.get(TaierConf.SERVER_EXEC_POOL_SIZE));
        System.out.println(conf.get(TaierConf.SESSION_CHECK_INTERVAL));
        System.out.println(conf.get(TaierConf.SESSION_IDLE_TIMEOUT));
        System.out.println(conf.get(TaierConf.SERVER_EXEC_WAIT_QUEUE_SIZE));
    }

}
//operation 执行后不会自动关闭，只有超时后才会自动关闭
//operation 只有超时后才会释放，因为不确定什么时候会fetch result
//operation 可反复获取同一个operation结果，但是只有第一次会有结果。
//operation 在执行的时候，被expire 清理了咋办？ 不会只有op 超时才会被清理
//session 在执行的时候，被expire 清理了咋办？不会，只有idle 超时才会被清理

//session 测试case
//1.close session 不论发生任何异常，必须保证 connection 被close
//2.并发关闭同一个session 的时候 ，只有一个可以成功
//3.并发关闭不同的session 的时候，可以同时关闭
//4.并发关闭一个不存在的session 的时候，都失败
//5.并发关闭一个已经关闭的session 的时候，都失败
//6.session 超时后，自动关闭
//7.session 超时后，再次关闭，失败
//8.session 超时后，再次执行操作，失败
//9.session 超时后，保证session 自动关闭，并且close connection,cancel operation
//10.关闭一个session 的时候，保证所有的operation 都被关闭
//11.关闭一个session 的时候，保证所有的operation 都被cancel
//12.使用一个 关闭的session 执行 execute statement,失败
//13.使用一个 关闭的session 执行 fetch result,失败
//14.使用一个 关闭的session 执行 close operation,失败
//15.使用一个 关闭的session 执行 cancel operation,失败
//16.使用一个 关闭的session 执行 applyOpAction,失败
//17.session close 或者超时后，保证hashmap 中的session 被删除
//18.session close 或者超时后，保证hashmap 中的session 中的operation 被删除

//operation 测试case
//operation 不存在的时候，cancel,close,fetch result,applyOpAction 都失败
//operation 已经关闭的时候，cancel,close,fetch result,applyOpAction 都失败
//operation   超时后，自动关闭
//operation   并发关闭同一个operation 的时候 ，只有一个可以成功
//operation   并发关闭不同的operation 的时候，可以同时关闭
//operation   超时后，再次关闭，失败
//operation 关闭或者超时或者close 或者cancel 后，保证connection 被close
//operation 关闭或者超时或者close 或者cancel 后，保证hashmap 中的operation 被删除
//operation 关闭或者超时或者close 或者cancel 后，保证hashmap 中的session 中的operation 被删除
//operation 的 OperationState 必须对应    INITIALIZED, PENDING, RUNNING, COMPILED, FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR, UNKNOWN;
//operation 的 OperationState 的状态转换必须符合规则,刚创建时状态必须是INITIALIZED,最终状态必须是FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR