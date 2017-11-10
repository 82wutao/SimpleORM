package net.aqsj.buz.test;

import net.aqsj.common.orm.Executer;
import net.aqsj.common.orm.transcation.ServiceProxyFactory;
import net.aqsj.common.orm.transcation.meta.TransactionIsolation;
import net.aqsj.common.orm.transcation.meta.TransactionMeta;
import org.apache.commons.dbcp.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class TestService {

protected  Executer executer =null;

    public void setExecuter(Executer executer) {
        this.executer = executer;
    }

    @TransactionMeta(isolation = TransactionIsolation.ISOLATION_READ_COMMITTED)
    public void save() throws SQLException {
        executer.executeUpdate("insert into tt(i,n) value(9,'name9');");
        int ret = 9/0;
        System.out.println(ret);
    }




    public static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        FileInputStream is =null;
        try{
            is = new FileInputStream("dbcp.properties");
            properties.load(is);
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            is.close();
        }

        try{
            DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
            return dataSource;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) throws IOException, SQLException {
        DataSource dataSource = createDataSource();

        TestService service = ServiceProxyFactory.proxyServer(TestService.class,dataSource);
        service.setExecuter(new Executer(dataSource));
        service.save();
    }
}
