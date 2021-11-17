package spark.streaming.playground.spend.util

import com.mchange.v2.c3p0.ComboPooledDataSource

import java.sql.Connection

class MysqlPool extends Serializable {
  private val cpds: ComboPooledDataSource = new ComboPooledDataSource(true)
  try {
    cpds.setJdbcUrl("jdbc:mysql://mysql:3306/sql-demo?useUnicode=true&characterEncoding=UTF-8")
    cpds.setDriverClass("com.mysql.cj.jdbc.Driver")
    cpds.setUser("sql-demo")
    cpds.setPassword("demo-sql")
    cpds.setMaxPoolSize(200)
    cpds.setMinPoolSize(20)
    cpds.setAcquireIncrement(5)
    cpds.setMaxStatements(180)
  } catch {
    case e: Exception => e.printStackTrace()
  }

  def getConnection: Connection = {
    try {
      cpds.getConnection()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        null
    }
  }
}

object MysqlManager {
  var mysqlManager: MysqlPool = _

  def getMysqlManager: MysqlPool = {
    if (mysqlManager == null) {
      synchronized {
        if (mysqlManager == null) {
          mysqlManager = new MysqlPool
        }
      }
    }
    mysqlManager
  }
}
