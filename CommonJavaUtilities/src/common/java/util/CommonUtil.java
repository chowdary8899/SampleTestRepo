package common.java.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.be128.globalcache.GlobalCache;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;
import com.ibm.broker.plugin.MbXMLNSC;

public class CommonUtil extends MbJavaComputeNode {

	// Logging FW Enhancements
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
	}

	public static void AuditLog(String TransactionLevel, String Payload,
			String AuditMessage, MbMessageAssembly outAssembly,
			MbElement mbAuditData, MbRoute labelAudit, String AppLogLevel,
			String BRKLogLevel, String ApplicationName, String BusProcId)
			throws MbException {
		if (BRKLogLevel.equalsIgnoreCase("DEBUG")
				|| (BRKLogLevel.equalsIgnoreCase("ERROR") && AppLogLevel
						.equalsIgnoreCase("ERROR"))
				|| (BRKLogLevel.equalsIgnoreCase("WARN") && (AppLogLevel
						.equalsIgnoreCase("WARN") || AppLogLevel
						.equalsIgnoreCase("ERROR")))
				|| (BRKLogLevel.equalsIgnoreCase("INFO") && (AppLogLevel
						.equalsIgnoreCase("INFO") || (AppLogLevel
						.equalsIgnoreCase("WARN") || AppLogLevel
						.equalsIgnoreCase("ERROR"))))) {
			if (mbAuditData.getFirstElementByPath("TransactionLevel") == null) {
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME,
						"loggerAppender", ApplicationName);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,
						"uniqueID", BusProcId);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,
						"Payload", Payload);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,
						"TransactionLevel", TransactionLevel);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,
						"AuditMessage", AuditMessage);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,
						"AppLogLevel", AppLogLevel);
			} else {

				mbAuditData.getFirstElementByPath("TransactionLevel").setValue(
						TransactionLevel);
				mbAuditData.getFirstElementByPath("Payload").setValue(Payload);
				mbAuditData.getFirstElementByPath("AuditMessage").setValue(
						AuditMessage);
				mbAuditData.getFirstElementByPath("AppLogLevel").setValue(AppLogLevel) ;
			}
			labelAudit.propagate(outAssembly);
		}
	}

	public static void readBkrConfigCacheXML(String BrkConfigKey, String varGCMapName,
			MbElement mVarAudit, String SchemaName) {
		for (int noOfRetry = 0; noOfRetry <= 3; noOfRetry++) {
			try {
				if (noOfRetry > 0) { 
					Thread.sleep(3000);
					new CommonUtil().loadConfigCache(mVarAudit, SchemaName, varGCMapName,
							BrkConfigKey);
				}
				String mapContent = GlobalCache.readCache(varGCMapName,
						BrkConfigKey);
				mVarAudit.createElementAsLastChildFromBitstream(
						mapContent.getBytes(), MbXMLNSC.PARSER_NAME, "", "",
						"", 0, 0, 0);
				break;
			} catch (Exception e) {
				// TODO Auto-generatedS catch block
				e.printStackTrace();

			}
		}
	}

	public static String readBrkConfig(String ApplicationName,
			String BrokerName, MbElement mVarAudit) {
		String Brk_LogLevel_LogSwitch = "";
		try {

			MbElement rBrkCaseConfig = mVarAudit.getFirstChild();
			while (rBrkCaseConfig != null) {

				if (rBrkCaseConfig.getFirstElementByPath("AppName")
						.getValueAsString().equalsIgnoreCase(ApplicationName)) {
					MbElement rConfig2 = rBrkCaseConfig
							.getFirstElementByPath("Config");

					while (rConfig2 != null) {
						if (rConfig2.getFirstElementByPath("BrokerName")
								.getValueAsString()
								.equalsIgnoreCase(BrokerName)
								|| rConfig2.getFirstElementByPath("BrokerName")
										.getValueAsString()
										.equalsIgnoreCase("ALL")) {
							Brk_LogLevel_LogSwitch = rConfig2
									.getFirstElementByPath("LogLevel")
									.getValueAsString()
									+ ","
									+ rConfig2.getFirstElementByPath(
											"LogSwitch").getValueAsString();
							break;
						}
						rConfig2 = rConfig2.getNextSibling();
					}
					break;
				}

				rBrkCaseConfig = rBrkCaseConfig.getNextSibling();
			}

		} catch (MbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Brk_LogLevel_LogSwitch;
	}

	public void loadConfigCache(MbElement envRef, String SchemaName,
			String UDP_varGCMapName, String UDP_GlobalCacheKEY)
			throws MbException, SQLException {
		String CH_AppName, dbQuery, currentAppName = "";
		ResultSet resultset;
		Boolean bGBcacheFlag;
		Connection conn = null;
		Statement stmt = null;
		String sDSN = (String) getUserDefinedAttribute("DSN");

		conn = getJDBCType4Connection(sDSN,
				JDBC_TransactionType.MB_TRANSACTION_AUTO);
		stmt = conn.createStatement();
		dbQuery = "SELECT * FROM " + SchemaName + ".BRK_CONFIG ORDER BY APP_NM";
		resultset = stmt.executeQuery(dbQuery);

		MbElement envCache = envRef
				.createElementAsLastChild(MbXMLNSC.PARSER_NAME);
		MbElement envCacheRef = envCache.createElementAsFirstChild(
				MbElement.TYPE_NAME_VALUE, "BRK_CONFIG", null);

		while (resultset.next()) {
			CH_AppName = resultset.getString("APP_NM");
			MbElement envCacheRefApp = envCacheRef.createElementAsLastChild(
					MbElement.TYPE_NAME, "Application", null);
			if (CH_AppName.equalsIgnoreCase(currentAppName)) {
				// do nothing
			} else {

				envCacheRefApp.createElementAsFirstChild(MbXMLNSC.ATTRIBUTE,
						"AppName", CH_AppName);
				currentAppName = CH_AppName;
			}
			MbElement envCacheRefConfig = envCacheRefApp
					.createElementAsLastChild(MbElement.TYPE_NAME, "Config",
							null);
			envCacheRefConfig.createElementAsLastChild(MbXMLNSC.ATTRIBUTE,
					"LogLevel", resultset.getString("LOG_LEVEL"));
			envCacheRefConfig.createElementAsLastChild(MbXMLNSC.ATTRIBUTE,
					"LogSwitch", resultset.getString("LOG_SWITCH"));
			envCacheRefConfig.createElementAsLastChild(MbXMLNSC.ATTRIBUTE,
					"BrokerName", resultset.getString("BRK_NM"));

		}

		String CH_GlobalCacheValue = new String(envRef.toBitstream(null, null,
				null, 273, 1208, 0));
		bGBcacheFlag = com.be128.globalcache.GlobalCache.insertCache(
				UDP_varGCMapName, UDP_GlobalCacheKEY, CH_GlobalCacheValue);
		
		envCacheRef = null ;
		envCache = null ;
		if(stmt != null){
			stmt.close();
			}
	}
			
}
