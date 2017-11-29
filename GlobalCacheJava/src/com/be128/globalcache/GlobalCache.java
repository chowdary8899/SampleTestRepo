package com.be128.globalcache;

import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbGlobalMap;
import com.ibm.broker.plugin.MbGlobalMapSessionPolicy;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;
import com.ibm.broker.plugin.MbXMLNSC;

public class GlobalCache {
	
	
	/* This method is used to insert Map,Key and Value in cache,It also updates the existing key.*/
	 public static  Boolean insertCache(String cacheName, String key, String value) {
       try {
          // MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
		  // MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName, new MbGlobalMapSessionPolicy(86400) );
		   MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName, new MbGlobalMapSessionPolicy(259200) );  /* 3 days expiry */
           String keyname = (String) map.get(key);
           if (keyname == null) {
               map.put(key, value);
           } else {
        	   map.update(key, value);
           }
       } catch (MbException e) {
           return false;
       }
       return true;
   }
	 /* This method is used to get the Key and Value for the given map */ 
	 public static String readCache(String cacheName, String key) {
	        try {
	        	String test = null;
	            MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
	            test = (String) map.get(key) ;
	            if(test != null){
	            	return (String) map.get(key);
	            }else
	            	return null;
	        } catch (MbException e) {
	        	return null;
	        }
	    }
	 /* This method is used to delete the Key for the given map */  
	public static Boolean deleteCache(String cacheName, String key) {
        try {
            MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
            String keyname = (String) map.get(key);
            if (keyname == null) {
               return false;
            } else {
                map.remove(key);
                return true;
            }
        } catch (MbException e) {
            return false;
        }
      
        }
	
	public static synchronized Boolean updateCache(String cacheName, String key, String param) {
        try {
            MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
            String tempVal = (String) map.get(key);
            if (tempVal == null) {
            	return false;
            }
 			String[] paramList = param.split(";");
			for (int i = 0; i < paramList.length; i++) {
				String[] nodeNameVal = paramList[i].split("=");
				tempVal = tempVal.substring(0,tempVal.indexOf(nodeNameVal[0]))+nodeNameVal[0]+">"+nodeNameVal[1]+ tempVal.substring(tempVal.indexOf("</"+nodeNameVal[0]));
			}
			map.update(key, tempVal);
        }
        catch (MbException e) {
        	return false;
		}
        return true;
    }
	public static synchronized Boolean updateCacheGPS(String cacheName, String key, String param) {
        try {
            MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
            String tempVal = (String) map.get(key);
            if (tempVal == null) {
             return false;
            }
 tempVal = tempVal + ','+param;
	//tempVal = tempVal.substring(0,tempVal.indexOf(nodeNameVal[0]))+nodeNameVal[0]+">"+nodeNameVal[1]+ tempVal.substring(tempVal.indexOf("</"+nodeNameVal[0]));

	map.update(key, tempVal);
        }
        catch (MbException e) {
         return false;
	}
        return true;
    }
	
	public static synchronized Boolean updateActiveRequests(String cacheName, String key, String operation, String count){
		/*
		 * Parameter
		 * cacheName = Name of the cache
		 * key = Key to fetch values from cache
		 * operation =  Hardcoded values either Increment or Decrement
		 */
		MbGlobalMap map;
		Integer tempCacheVal;
		Integer tempCountVal=0 ;
		String tempStr = "";
		
		try {
			map = MbGlobalMap.getGlobalMap(cacheName);
			tempStr = (String) map.get(key);
	        if (tempStr == null) {
	        	map.put(key, count);
	        	return true ;
	        }else if (operation.equals("Increment")){
        	tempCacheVal = Integer.valueOf(tempStr);
			tempCountVal = Integer.valueOf(count) ;
        	tempCacheVal = tempCacheVal + tempCountVal;
        	 map.update(key, String.valueOf(tempCacheVal));
        	 return true ;
	        	}else if (operation.equals("Decrement")) {
	        			tempCacheVal = Integer.valueOf(tempStr);
	        			tempCountVal = Integer.valueOf(count) ;
	        			tempCacheVal = tempCacheVal - tempCountVal;
	        			if (tempCacheVal >= 0){
	        					map.update(key, String.valueOf(tempCacheVal));
	        					return true ;
	        			}else if(tempCacheVal < 0 ) {
	        				map.update(key, "0");
	        				return true ;
	        			}
	        			else
	        				return false;
	        			}
	    else{
			return false;
		}
		} catch (MbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	public static Boolean loadCustProfile(String cacheName, String key, String value){
		try {
	           MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
	           String keyname = (String) map.get(key);
	           if (keyname == null) {
	               map.put(key, value);
	           } else {
	               map.update(key, value);
	           }
	       } catch (MbException e) {
	           return false;
	       }
	       return true;
	}
	
	public static synchronized Boolean updateSourceCount(String cacheName, String key, String param){
		Integer countInt ;
		//Document doc ;
		try {
            MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
            String tempVal = (String) map.get(key);
            String[] nodeNameVal = param.split("=");
            
            /* Assumption is cache data will contain 
             * TransSetID = <SourceCount><SourceActualCnt>Value1</SourceActualCnt>
             * <SourceProcessedCnt>Value2</SourceProcessedCnt></SourceCount> 
            */
          
            if (tempVal == null) {
            	if(nodeNameVal[0].equals("SourceActualCnt")){
        			tempVal = "<SourceCount><SourceActualCnt>" +
        					  nodeNameVal[1]+
        					  "</SourceActualCnt><SourceProcessedCnt>0</SourceProcessedCnt></SourceCount>";	
        			map.put(key, tempVal);
            		return true;
            	} else {
            		return false ;
            	} 
            }
            else{
 
	    			if (nodeNameVal[0].equals("SourceProcessedCnt")){
	    			countInt = Integer.parseInt(tempVal.substring(tempVal.indexOf("SourceProcessedCnt")+19,tempVal.indexOf("</"+"SourceProcessedCnt")));
	    			countInt = countInt + Integer.valueOf(nodeNameVal[1]); 
	    			tempVal = tempVal.substring(0,tempVal.indexOf("SourceProcessedCnt"))+"SourceProcessedCnt"+">"+countInt+tempVal.substring(tempVal.indexOf("</"+"SourceProcessedCnt"));
	    			map.update(key, tempVal);
	    		}
            }	
            	
        } catch (Exception e) {
            return false;
        }
		return true;
	}
	
	public static Boolean checkSourceCount(String cacheName, String key){

		MbGlobalMap map;
		int actualCnt = 0;
		int processedCnt = 0;
		String tempVal ;
		try {
			map = MbGlobalMap.getGlobalMap(cacheName);
			tempVal = (String) map.get(key);
	        if (tempVal == null) {
	        	return false;
	        }
	        actualCnt = Integer.parseInt(tempVal.substring(tempVal.indexOf("SourceActualCnt")+16,tempVal.indexOf("</"+"SourceActualCnt")));
	        processedCnt = Integer.parseInt(tempVal.substring(tempVal.indexOf("SourceProcessedCnt")+19,tempVal.indexOf("</"+"SourceProcessedCnt")));
	        if (actualCnt == processedCnt){
	        	return true;
	        }else{
	        	return false;
	        }
		} 
        catch (MbException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean checkSourceCnt(String cacheName, String key, String transInqFlag,String fileType, String autoCancelInd){

		MbGlobalMap map;
		int actualCnt 				= 0;
		int processedCnt 			= 0;
		int processedCnt_transInq 	= 0;
		int processedCnt_debatch 	= 0;
		int processedCnt_dbLoad 	= 0;
		int processedCnt_autoCancel = 0;
		
		String transInq,debatch,dbLoad,autoCancel ;
		try {
			
			if(transInqFlag.equalsIgnoreCase("Y")){
				map = MbGlobalMap.getGlobalMap(cacheName); // TransactionalInquiry flow
				transInq = (String) map.get(key+"_1");
				processedCnt_transInq = Integer.parseInt(transInq.substring(transInq.indexOf("SourceProcessedCnt")+19,transInq.indexOf("</"+"SourceProcessedCnt")));
				
				dbLoad = (String) map.get(key+"_4");		// DBLoad flow
				processedCnt_dbLoad = Integer.parseInt(dbLoad.substring(dbLoad.indexOf("SourceProcessedCnt")+19,dbLoad.indexOf("</"+"SourceProcessedCnt")));
				
				processedCnt = processedCnt_transInq+processedCnt_dbLoad;
			}else if(fileType.equalsIgnoreCase("F") && autoCancelInd.equalsIgnoreCase("Y")){
				map = MbGlobalMap.getGlobalMap(cacheName); 
				autoCancel = (String) map.get(key+"_3");	// Autocancel flow
				processedCnt_autoCancel = Integer.parseInt(autoCancel.substring(autoCancel.indexOf("SourceProcessedCnt")+19,autoCancel.indexOf("</"+"SourceProcessedCnt")));
				
				dbLoad = (String) map.get(key+"_4");		// DBLoad flow
				processedCnt_dbLoad = Integer.parseInt(dbLoad.substring(dbLoad.indexOf("SourceProcessedCnt")+19,dbLoad.indexOf("</"+"SourceProcessedCnt")));
				
				processedCnt = processedCnt_autoCancel + processedCnt_dbLoad;
			}else{
				map = MbGlobalMap.getGlobalMap(cacheName); 
				debatch = (String) map.get(key+"_2");		// Debatcher flow
				processedCnt_debatch = Integer.parseInt(debatch.substring(debatch.indexOf("SourceProcessedCnt")+19,debatch.indexOf("</"+"SourceProcessedCnt")));
				
				dbLoad = (String) map.get(key+"_4");		// DBLoad flow
				processedCnt_dbLoad = Integer.parseInt(dbLoad.substring(dbLoad.indexOf("SourceProcessedCnt")+19,dbLoad.indexOf("</"+"SourceProcessedCnt")));
				
				processedCnt = processedCnt_debatch + processedCnt_dbLoad;
			}
			
	        actualCnt = Integer.parseInt(dbLoad.substring(dbLoad.indexOf("SourceActualCnt")+16,dbLoad.indexOf("</"+"SourceActualCnt")));
	        if (actualCnt == processedCnt){
	        	return true;
	        }else{
	        	return false;
	        }
		} 
        catch (MbException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean deletePollerCache(String cacheName, String key) {
        try{
            MbGlobalMap map = MbGlobalMap.getGlobalMap(cacheName);
            map.remove(key);
            return true;
        }catch(MbException e){
            return false;
        }
            
      }
	
	public static synchronized String incrementAndIsReadyCheckInb(String cacheName, String key, String batchCnt, String brkrList){
		
		/* Assumption is cache data will contain 
         * TransSetID_Broker1 = Actualcount,processedCount 
         * TransSetID_Broker2 = Actualcount,processedCount 
         * TransSetID_Broker..n = Actualcount,processedCount 
        */
		MbGlobalMap map;
		int actualCnt 			= 	0;
		int cnt					= 	0;
		int processedCnt 		= 	0;
		int totalprocessedCnt 	= 	0;
		String status 			=	"" ;
		String[] BrokerName 	= 	brkrList.split("_");
		String tempVal,keyVal;
		
		try{
			
			/*increment the batch member count */
			map = MbGlobalMap.getGlobalMap(cacheName);
			tempVal = (String) map.get(key);
			if (tempVal == null) {
				return "false";
			}
			actualCnt = Integer.parseInt(tempVal.substring(0,tempVal.indexOf(",")));
			processedCnt = Integer.parseInt(tempVal.substring(tempVal.indexOf(",") + 1))+Integer.parseInt(batchCnt);
	        map.update(key, actualCnt+","+processedCnt);	
			
			/*Checking all the brokers to find if is Ready trigger need to be sent */
	        while(cnt < BrokerName.length){
					 
					keyVal = key.substring(0,key.indexOf("_")+1)+BrokerName[cnt];
					tempVal = (String) map.get(keyVal);
					actualCnt = Integer.parseInt(tempVal.substring(0,tempVal.indexOf(",")));
					processedCnt = Integer.parseInt(tempVal.substring(tempVal.indexOf(",") + 1));
					status = status + BrokerName[cnt] +"::"+"ActualCnt:"+ actualCnt +","+ "ProcessedCnt:"+processedCnt+ " " ;
					cnt = cnt+1;
					totalprocessedCnt = totalprocessedCnt + processedCnt; 
				}

			if (actualCnt == totalprocessedCnt){
	        	return "Y";  /* Trigger isReady */
	        }else{
	        	return status;  /* Status of the file processing by each broker and application */
	        }
		}catch(MbException e) {
			e.printStackTrace();
			return "false";
			
		}
		
		
	}
	
	public static synchronized String incrementAndIsReadyCheckSrc(String cacheName, String key, String batchCnt, 
							String brkrList, String transInqFlag,String fileType, String autoCancelInd){
		
		/* Assumption is cache data will contain 
         * TransSetID_1_Broker1 = Actualcount,processedCount
         * TransSetID_2_Broker1 = Actualcount,processedCount
         * TransSetID_3_Broker1 = Actualcount,processedCount
         * TransSetID_4_Broker1 = Actualcount,processedCount
         *  
         * TransSetID_1_Broker2 = Actualcount,processedCount
         * TransSetID_2_Broker2 = Actualcount,processedCount
         * TransSetID_3_Broker2 = Actualcount,processedCount
         * TransSetID_4_Broker2 = Actualcount,processedCount
         *    
        */
		MbGlobalMap map;
		int actualCnt 				= 	0;
		int cnt						= 	0;
		int processedCnt 			= 	0;
		int totalprocessedCnt 		= 	0;
		int processedCnt_transInq 	= 	0;
		int processedCnt_debatch 	= 	0;
		int processedCnt_dbLoad 	= 	0;
		int processedCnt_autoCancel = 	0;
		String status 				=	"" ;
		String[] BrokerName 		= 	brkrList.split("_");
		String tempVal,keyVal,transInq,debatch,dbLoad,autoCancel ;
		
		try {
			
				/*increment the batch member count */
				map = MbGlobalMap.getGlobalMap(cacheName);
				tempVal = (String) map.get(key);
				if (tempVal == null) {
					return "false";
				}
				actualCnt = Integer.parseInt(tempVal.substring(0,tempVal.indexOf(",")));
				processedCnt = Integer.parseInt(tempVal.substring(tempVal.indexOf(",") + 1))+Integer.parseInt(batchCnt);
			    map.update(key, actualCnt+","+processedCnt);	
	
			  /*Checking all the brokers to find if is Ready trigger need to be sent */
		      while(cnt < BrokerName.length  ){
			    
		    	  keyVal = key.substring(0,key.indexOf("_"));
		    	  if(transInqFlag.equalsIgnoreCase("Y")){
		    		  map = MbGlobalMap.getGlobalMap(cacheName); // TransactionalInquiry flow
		    		  transInq = (String) map.get(keyVal+"_1_"+BrokerName[cnt]);
		    		  processedCnt_transInq = Integer.parseInt(transInq.substring(transInq.indexOf(",") + 1));
				
		    		  dbLoad = (String) map.get(keyVal+"_4_"+BrokerName[cnt]);		// DBLoad flow
		    		  processedCnt_dbLoad = Integer.parseInt(dbLoad.substring(dbLoad.indexOf(",") + 1));
				
		    		  processedCnt = processedCnt_transInq+processedCnt_dbLoad;
		    		  status = status + BrokerName[cnt] +"::"+"ActualCount:"+ actualCnt +","+" transInq:"+ processedCnt_transInq+" dbLoad:"+processedCnt_dbLoad+ "  " ;
		    	  
		    	  }else if(fileType.equalsIgnoreCase("F") && autoCancelInd.equalsIgnoreCase("Y")){
		    		  map = MbGlobalMap.getGlobalMap(cacheName); 
		    		  autoCancel = (String) map.get(keyVal+"_3_"+BrokerName[cnt]);	// Autocancel flow
		    		  processedCnt_autoCancel = Integer.parseInt(autoCancel.substring(autoCancel.indexOf(",") + 1));
						
		    		  dbLoad = (String) map.get(keyVal+"_4_"+BrokerName[cnt]);		// DBLoad flow
		    		  processedCnt_dbLoad = Integer.parseInt(dbLoad.substring(dbLoad.indexOf(",") + 1));
						
		    		  processedCnt = processedCnt_autoCancel + processedCnt_dbLoad;
		    		  status = status + BrokerName[cnt] +"::"+"ActualCount:"+ actualCnt +","+" autoCancel:"+ processedCnt_autoCancel+" dbLoad:"+processedCnt_dbLoad+ "  " ;
		    	  
		    	  }else{
		    		  map = MbGlobalMap.getGlobalMap(cacheName); 
		    		  debatch = (String) map.get(keyVal+"_2_"+BrokerName[cnt]);		// Debatcher flow
		    		  processedCnt_debatch = Integer.parseInt(debatch.substring(debatch.indexOf(",") + 1));
					
		    		  dbLoad = (String) map.get(keyVal+"_4_"+BrokerName[cnt]);		// DBLoad flow
		    		  processedCnt_dbLoad = Integer.parseInt(dbLoad.substring(dbLoad.indexOf(",") + 1));
					
		    		  processedCnt = processedCnt_debatch + processedCnt_dbLoad;
		    		  status = status + BrokerName[cnt] +"::"+"ActualCount:"+ actualCnt +","+" debatch:"+ processedCnt_debatch+" dbLoad:"+processedCnt_dbLoad+ "  " ;
		    	  }
		    	 
		    	  totalprocessedCnt = totalprocessedCnt + processedCnt;
				  cnt = cnt+1;
		      }
				  
	        if (actualCnt == totalprocessedCnt){
	        	return "Y";  /* Trigger isReady */
	        }else{
	        	return status;  /* Status of the file processing by each broker and application */
	        }
		   
		}catch (MbException e) {
			e.printStackTrace();
			return "false";
		}
		
	}	
	
	// Logging FW Enhancements-- commented here as these are added as CommonJavaUtilities 
	/*
	public static void AuditLog(String TransactionLevel,String Payload, String AuditMessage, MbMessageAssembly outAssembly,MbElement mbAuditData,MbRoute labelAudit,String AppLogLevel ,String BRKLogLevel, String ApplicationName, String BusProcId)throws MbException{
		if(BRKLogLevel.equalsIgnoreCase("DEBUG") || 
				(BRKLogLevel.equalsIgnoreCase("ERROR") && AppLogLevel.equalsIgnoreCase("ERROR")) || 
				(BRKLogLevel.equalsIgnoreCase("WARN") && AppLogLevel.equalsIgnoreCase("WARN")) || 
				(BRKLogLevel.equalsIgnoreCase("INFO") && AppLogLevel.equalsIgnoreCase("INFO")))
		{
			if(mbAuditData.getFirstElementByPath("TransactionLevel") == null){
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME, "loggerAppender", ApplicationName);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "uniqueID", BusProcId);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Payload", Payload);				
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "TransactionLevel", TransactionLevel);
				mbAuditData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "AuditMessage", AuditMessage);	
			}else{
			
				mbAuditData.getFirstElementByPath("TransactionLevel").setValue(TransactionLevel);
				mbAuditData.getFirstElementByPath("Payload").setValue(Payload);
				mbAuditData.getFirstElementByPath("AuditMessage").setValue(AuditMessage);	
			}
			labelAudit.propagate(outAssembly);
		}
}
		

public static void readBkrConfigCacheXML( String BrkConfigKey, String varGCMapName ,MbElement mVarAudit){
    try {
          String mapContent = GlobalCache.readCache(varGCMapName, BrkConfigKey);
          mVarAudit.createElementAsLastChildFromBitstream(mapContent.getBytes(), MbXMLNSC.PARSER_NAME, "", "", "", 0, 0, 0);
    } catch (MbException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
    }
    
}


public static String readBrkConfig( String ApplicationName, String BrokerName ,MbElement mVarAudit){
	String Brk_LogLevel_LogSwitch = "";
    try {
    	
    	MbElement rBrkCaseConfig = mVarAudit.getFirstChild();
          while(rBrkCaseConfig != null ){
                
                if( rBrkCaseConfig.getFirstElementByPath("AppName").getValueAsString().equalsIgnoreCase(ApplicationName)){
                      MbElement rConfig2 = rBrkCaseConfig.getFirstElementByPath("Config");
                      
                      while(rConfig2 != null){
                            if(rConfig2.getFirstElementByPath("BrokerName").getValueAsString().equalsIgnoreCase(BrokerName) || rConfig2.getFirstElementByPath("BrokerName").getValueAsString().equalsIgnoreCase("ALL")){
                            	Brk_LogLevel_LogSwitch = rConfig2.getFirstElementByPath("LogLevel").getValueAsString()+","+rConfig2.getFirstElementByPath("LogSwitch").getValueAsString();
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
	*/
}
