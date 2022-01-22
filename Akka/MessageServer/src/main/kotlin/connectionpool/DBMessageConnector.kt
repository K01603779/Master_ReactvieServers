package connectionpool

import org.apache.commons.dbcp.BasicDataSource
import java.sql.Connection

class DBMessageConnector {

    private var ds = BasicDataSource()

    private fun init() {
        //Class.forName("com.mysql.jdbc.Driver");
        ds.url = "jdbc:mysql://127.0.0.1:55555/warehouse"
        ds.username = "root"
        ds.password = ""
        ds.minIdle = 5
        ds.maxIdle = 10
        ds.maxOpenPreparedStatements = 10
        ds.driverClassName = "com.mysql.jdbc.Driver"
    }

    init {
        init()
    }

    fun getConnection(): Connection? {
        return ds.connection

    }

    fun closeAllAndRestart() {
        ds.close()
        ds = BasicDataSource()
        init()
    }
}