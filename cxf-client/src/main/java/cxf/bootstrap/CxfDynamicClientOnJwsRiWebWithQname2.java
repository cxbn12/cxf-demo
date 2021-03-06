package cxf.bootstrap;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cxf.service.User;
import util.JsonUtil;

/**
 * CXF 动态代理模式，不用生成本地WS代理类，
 * 通过反射调用 WS 的对应的方法，传入相应的参数
 * 访问cxf-server-web项目下的webservice;
 * 测试jaxws-ri发布WebService web方式。
 * 此测试实例，用于测试SEI和SIB的targetNamespace未指定的webService接口：
 * http://localhost:8080/cxf-server-web/cxf_services?wsdl；
 * 针对SEI和SIB未指定targetNamespace的测试。
 * @author donald
 * 2017年7月8日
 * 下午7:24:12
 */
public class CxfDynamicClientOnJwsRiWebWithQname2 {
	private static final Logger log = LoggerFactory.getLogger(CxfClient.class);
	private static final String JWS_RT_WSDL_URI = "http://localhost:8080/cxf-server-web/cxf_services?wsdl";
	public static void main(String[] args) throws Exception {
		log.info("======CXF-WS Dynamic Client start！======");
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		Client client =  dcf.createClient(JWS_RT_WSDL_URI);
		HTTPConduit conduit = (HTTPConduit)client.getConduit(); 
		HTTPClientPolicy policy = new HTTPClientPolicy(); 
		policy.setConnectionTimeout(10000); 
		policy.setAllowChunking(false); 
		policy.setReceiveTimeout(10000); 
		conduit.setClient(policy);	
		QName operateQName = getOperateQName(client,"sum");
		Object[] invokeResult = client.invoke(operateQName, 17,8);	
		log.info("=======sumResult:" + invokeResult[0]);
		operateQName = getOperateQName(client,"multiply");
		invokeResult = client.invoke(operateQName, 17,8);
		log.info("=======multiplyResult:" + invokeResult[0]);
//		operateQName = getOperateQName(client,"login");
		operateQName = new QName("http://service.cxf/","login");
		invokeResult = client.invoke(operateQName, "donald","123456");
//		import cxf.service.User;;注意User必须为cxf.service.User;
		User userInfo = (User)invokeResult[0];
		log.info("=======loginUserInfo:" + JsonUtil.toJson(userInfo));
		
	}
	/**
	 * 针对SEI和SIB不在统一个包内的情况，先查找操作对应的Qname，
	 * client通过Qname调用对应操作
	 * @param client
	 * @param operation
	 * @return
	 */
	private static QName getOperateQName(Client client,String operation){
		Endpoint endpoint = client.getEndpoint();  
		QName opName = new QName(endpoint.getService().getName().getNamespaceURI(), operation);  
		BindingInfo bindingInfo = endpoint.getEndpointInfo().getBinding();  
		if (bindingInfo.getOperation(opName) == null) {  
		    for (BindingOperationInfo operationInfo : bindingInfo.getOperations()) {  
		        if (operation.equals(operationInfo.getName().getLocalPart())) {  
		            opName = operationInfo.getName();  
		            break;  
		        }  
		    }  
		}
		log.info("Operation:"+operation+",namespaceURI:" + opName.getNamespaceURI());
		return opName;
	}
}
