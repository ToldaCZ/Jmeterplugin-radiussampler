package com.ipass.jmeterplugin.radiussampler;

import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.dictionary.DefaultDictionary;
import org.tinyradius.dictionary.Dictionary;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusUtil;

//import com.ipass.radius.aaasampler.Attribute;
//import com.ipass.radius.aaasampler.IpassDictionary;

public class RadiusSampler extends AbstractSampler
{

	private static Random random = new Random();

	public static void main(String[] args) {
		String str=frameSessionId("pavan@ipass.com");
		System.out.println(str);
	}

	private static String genSessionId(int min,int max){
		String val="";
		for(int i=0;i<8;i++){
			int s=random.nextInt((max - min) + 1) + min;
			char c = (char)s;
			val+=c;

		}

		return val;
	}

	private static String frameSessionId(String username){
		String str=genSessionId(97,120);
		return "0U"+str+"/"+username;
	}

	public SampleResult sample(Entry arg0)
	{

		String userName = getUserName();
		String password = getPassword();
		String serverIp = getServerIp();
		String sharedSecret = getSharedSecret();
		String localAddress = getLocalAddress();
		String packetIdentifier = getPacketIdentifier();
		int acctStatusType = getAcctStatusType(); //342
		int localPort = getLocalPort();
		int authPort = getAuthPort();
		int acctPort = getAcctPort();
		int retryCount = getRetryCount();
		int timeout = getSocketTimeout();

		SampleResult res = new SampleResult();
		res.setSampleLabel(getName());
		if(authPort !=0 && acctPort !=0 ){
			res.setSamplerData("Host: " + getServerIp() + " Auth Port: " + getAuthPort() + " Acct Port: "+getAcctPort());
		}



		CollectionProperty collectionProperty=getAttributesManager().getAttributes();

		AddAttributes add = new AddAttributes();


		if((userName==null || userName.length()<=0 ) && collectionProperty!=null){
			userName = add.getRequiredAttribute(collectionProperty,"User-Name");
		}

		if((password==null || password.length()<=0) && collectionProperty!=null){
			password = add.getRequiredAttribute(collectionProperty,"User-Password");
		}

		if((add.getRequiredAttribute(collectionProperty,"Acct-Status-Type")!=null && add.getRequiredAttribute(collectionProperty,"Acct-Status-Type").length()>0) && collectionProperty!=null) {
			String tmp;
			tmp = add.getRequiredAttribute(collectionProperty, "Acct-Status-Type");
			if (tmp.equals("1")) {
				acctStatusType = AccountingRequest.ACCT_STATUS_TYPE_START;
			} else if (tmp.equals("2")) {
				acctStatusType = AccountingRequest.ACCT_STATUS_TYPE_STOP;
			} else if (tmp.equals("3")) {
				acctStatusType = AccountingRequest.ACCT_STATUS_TYPE_INTERIM_UPDATE;
			} else if (tmp.equals("7")) {
				acctStatusType = AccountingRequest.ACCT_STATUS_TYPE_ACCOUNTING_ON;
			} else if (tmp.equals("8")) {
				acctStatusType = AccountingRequest.ACCT_STATUS_TYPE_ACCOUNTING_OFF;
			}  else {
				throw new IllegalArgumentException("Radius Acct-Status-Type value is unsupported: "+tmp);
				//acctStatusType = AccountingRequest.ACCT_STATUS_TYPE_START;
			}
		}


		res.sampleStart();

		//collectionProperty = add.removeAttributes(collectionProperty);

		if ( (userName!=null && userName.length()>0 && password!=null && password.length()>0 ) && (serverIp != null) && (serverIp.length() > 0) && authPort!=0 && acctPort !=0 && localPort >-1 && (sharedSecret!=null && sharedSecret.length()>0))
		{

			if(System.getenv("GEN_SES_ID")!=null && System.getenv("GEN_SES_ID").toLowerCase().equals("true"))
				userName = frameSessionId(userName);

			try {
				RadiusClient rcClient = null;
				AccessRequest accessReq = null;
				AccountingRequest acctReq = null;
				RadiusPacket authRadiusPacket = null;
				RadiusPacket acctRadiusPacket = null;
				//RadiusPacket acctStopRadiusPacket = null;
				boolean reqAuthNAcct = false;
				boolean authRAcct = true;


				AddAttributes addAttributes = new AddAttributes();

				String reqType = getRequestType();

				if (reqType.equalsIgnoreCase("both")) {
					reqAuthNAcct = true;
					//Auth Records
					rcClient = new RadiusClient(serverIp, sharedSecret);
					accessReq = new AccessRequest(userName, password);
					rcClient.setAuthPort(authPort);
					if (timeout > 0)
						rcClient.setSocketTimeout(timeout);

					if (retryCount > 0)
						rcClient.setRetryCount(retryCount);


					if (collectionProperty != null)
						accessReq = addAttributes.addAuthRadiusAttribute(accessReq, collectionProperty);

					authRadiusPacket = rcClient.authenticate(accessReq);


					//Start Records
					rcClient = new RadiusClient(serverIp, sharedSecret);
					acctReq = new AccountingRequest(userName, AccountingRequest.ACCT_STATUS_TYPE_START);
					rcClient.setAcctPort(acctPort);
					if (timeout > 0)
						rcClient.setSocketTimeout(timeout);

					if (retryCount > 0)


						if (collectionProperty != null)
							acctReq = addAttributes.addAcctRadiusAttribute(acctReq, collectionProperty);
					acctRadiusPacket = rcClient.account(acctReq);

					//Stop records
					rcClient = new RadiusClient(serverIp, sharedSecret);
					acctReq = new AccountingRequest(userName, AccountingRequest.ACCT_STATUS_TYPE_STOP);
					rcClient.setAcctPort(acctPort);
					if (timeout > 0)
						rcClient.setSocketTimeout(timeout);

					if (retryCount > 0)
						rcClient.setRetryCount(retryCount);
					/*if(acctRadAttr!=null)
						accessReq.addAttribute(getAttributes());*/
					if (collectionProperty != null)
						acctReq = addAttributes.addAcctRadiusAttribute(acctReq, collectionProperty);
					acctRadiusPacket = rcClient.account(acctReq);


				} else if (reqType.equalsIgnoreCase("auth")) {
					//Auth Records
					rcClient = new RadiusClient(serverIp, sharedSecret);
					accessReq = new AccessRequest(userName, password);
					rcClient.setAuthPort(authPort);
					if (collectionProperty != null)
						accessReq = addAttributes.addAuthRadiusAttribute(accessReq, collectionProperty);
					/*RadiusAttribute radAttr = getAttributes();
					if(radAttr!=null)
						accessReq.addAttribute(getAttributes());*/
					if (timeout > 0)
						rcClient.setSocketTimeout(timeout);

					if (retryCount > 0)
						rcClient.setRetryCount(retryCount);
					else
						rcClient.setRetryCount(1);
					authRadiusPacket = rcClient.authenticate(accessReq);
				} else if (reqType.equalsIgnoreCase("acct")) {
					authRAcct = false;

					rcClient = new RadiusClient(serverIp, sharedSecret);
					if (packetIdentifier != null && packetIdentifier.length() > 0)
						acctReq = new AccountingRequest(userName, acctStatusType, Integer.parseInt(packetIdentifier));
					else
						acctReq = new AccountingRequest(userName, acctStatusType);
					rcClient.setAcctPort(acctPort);
					rcClient.setLocalAddress(localAddress);
					rcClient.setLocalPort(localPort);
					if (collectionProperty != null)
						acctReq = addAttributes.addAcctRadiusAttribute(acctReq, collectionProperty);
					/*RadiusAttribute acctRadAttr = getAttributes();
					if(acctRadAttr!=null)
						accessReq.addAttribute(getAttributes());*/
					if (timeout > 0)
						rcClient.setSocketTimeout(timeout);

					if (retryCount > 0)
						rcClient.setRetryCount(retryCount);
					else
						rcClient.setRetryCount(1);
					acctRadiusPacket = rcClient.account(acctReq);
				} else {
					throw new IllegalArgumentException("Radius packet type is only auth,acct or both. Invalid request Type" + reqType);
				}


				if (reqAuthNAcct) {

					if (authRadiusPacket != null && acctRadiusPacket != null) {
						res.setSuccessful(true);
						res.setResponseData(authRadiusPacket.getPacketTypeName().getBytes());
						res.setDataType("text");
						res.setResponseCodeOK();
						res.setResponseMessage(authRadiusPacket.getPacketTypeName());
					} else {
						res.setSuccessful(false);
						if (authRadiusPacket == null)
							res.setResponseMessage("Server Dropped the Auth request ");
						if (acctRadiusPacket == null)
							res.setResponseMessage("Server Dropped the Acct request ");
						res.setResponseCode("500");
					}


				} else {

					if (authRAcct) {

						if (authRadiusPacket != null) {
							ObjectMapper response = new ObjectMapper();
							response.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
							String json = null;
							try {
								json = response.writeValueAsString(authRadiusPacket);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							if (authRadiusPacket.getPacketTypeName() == "Access-Accept") {
								res.setSuccessful(true);
								//res.setResponseData(authRadiusPacket.getPacketTypeName().getBytes());
								res.setDataType("text");
								res.setResponseData(json,"UTF-8");
								res.setResponseCode("2");
								//res.setResponseMessage(authRadiusPacket.getPacketTypeName());
								res.setResponseMessage(json);
							} else if (authRadiusPacket.getPacketTypeName() == "Access-Reject") {
								res.setSuccessful(true);
								//res.setResponseMessage("Access-Reject");
								res.setResponseMessage(json);
								res.setDataType("text");
								res.setResponseData(json,"UTF-8");
								res.setResponseCode("3");
							} else {
								res.setSuccessful(false);
								//res.setResponseMessage("Unexpected response");
								res.setResponseMessage(json);
								res.setDataType("text");
								res.setResponseData(json,"UTF-8");
								res.setResponseCode(authRadiusPacket.getPacketTypeName());
							}
						} else {
							res.setSuccessful(false);
							if (authRadiusPacket == null)
								res.setResponseMessage("Server Dropped the Auth request ");
							res.setResponseCode("500");
						}

					} else {
						if (acctRadiusPacket != null) {
							res.setSuccessful(true);
							//res.setResponseData(acctRadiusPacket.getPacketTypeName().getBytes());
							ObjectMapper response = new ObjectMapper();
							response.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
							String json = null;
							try {
								json = response.writeValueAsString(acctRadiusPacket);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							res.setResponseData(json);
							res.setDataType("text");
							res.setResponseCodeOK();
							res.setResponseMessage(json);
							//res.setResponseMessage(String.join(",",(acctRadiusPacket.getAttributes())));
						} else {
							res.setSuccessful(false);
							res.setResponseMessage("Server Dropped the Acct request ");
							res.setResponseCode("500");
						}
					}
				}
			}catch (Throwable excep) {
				excep.printStackTrace();
				res.setSuccessful(false);
				res.setResponseMessage(excep.getMessage());
				res.setResponseCode("500");
				//throw new NullPointerException(excep.toString());
			}
			finally {
				res.sampleEnd();

			}
		}else{
			if (userName == null || userName.length() == 0)
				throw new NullPointerException("User-Name cannot be empty");
			if (password == null || password.length() == 0)
				throw new NullPointerException("User-Password cannot be empty");
			if (serverIp == null || serverIp.length() == 0)
				throw new NullPointerException("Server IP cannot be empty");
			if (sharedSecret == null || sharedSecret.length() == 0)
				throw new NullPointerException("Shared Secret cannot be empty");
			if (authPort < 1)
				throw new NumberFormatException("Auth port must be greater than 0");
			if (acctPort < 1)
				throw new NumberFormatException("Acct port must be greater than 0");
			if (localPort < 0 || localPort > 65535)
				throw new NumberFormatException("Source port must be greater than 0");
		}
		return res;
	}



	public void setAttributesManager(RadiusAttributesManager value)
	{
		setProperty(new TestElementProperty(RadiusSamplerElements.RADIUS_ATTRIBUTES, value));
	}

	public RadiusAttributesManager getAttributesManager()
	{
		return (RadiusAttributesManager)getProperty(RadiusSamplerElements.RADIUS_ATTRIBUTES).getObjectValue();
	}

	public void setServerIp(String serverIp)
	{
		setProperty(RadiusSamplerElements.SERVER_IP, serverIp);
	}

	public String getServerIp()
	{
		return getPropertyAsString(RadiusSamplerElements.SERVER_IP);
	}


	public void setSharedSecret(String sharedSecret)
	{
		setProperty(RadiusSamplerElements.SHARED_SECRET, sharedSecret);
	}

	public String getSharedSecret()	{ return getPropertyAsString(RadiusSamplerElements.SHARED_SECRET); }

	public void setPacketIdentifier(String packetIdentifier) { setProperty(RadiusSamplerElements.PACKET_IDENTIFIER, packetIdentifier); }

	public String getPacketIdentifier() { return getPropertyAsString(RadiusSamplerElements.PACKET_IDENTIFIER); }

	public int getRetryCount(){
		return getPropertyAsInt(RadiusSamplerElements.RADIUS_RETRY);
	}

	public int getAcctStatusType(){
		return getPropertyAsInt(RadiusSamplerElements.STATUS_TYPE);
	}

	public void setRetryCount(int retryCount){
		setProperty(RadiusSamplerElements.RADIUS_RETRY,retryCount);
	}

	public int getSocketTimeout(){
		return getPropertyAsInt(RadiusSamplerElements.SOCKET_TIMEOUT);
	}

	public void setSocketTimeout(int socketTimeout){
		setProperty(RadiusSamplerElements.SOCKET_TIMEOUT,socketTimeout);
	}

	public void setAuthPort(int authPort){
		setProperty(RadiusSamplerElements.AUTH_PORT,authPort);
	}

	public void setAcctPort(int acctPort){
		setProperty(RadiusSamplerElements.ACCT_PORT,acctPort);
	}

	public int getAuthPort(){
		return getPropertyAsInt(RadiusSamplerElements.AUTH_PORT);
	}

	public void setLocalAddress(String localAddress){setProperty(RadiusSamplerElements.LOCAL_ADDRESS,localAddress); }

	public String getLocalAddress(){ return getPropertyAsString(RadiusSamplerElements.LOCAL_ADDRESS); }

	public void setLocalPort(int localPort){
	setProperty(RadiusSamplerElements.LOCAL_PORT,localPort);
}

	public int getLocalPort(){
		return getPropertyAsInt(RadiusSamplerElements.LOCAL_PORT);
	}


	public int getAcctPort()
	{
		return getPropertyAsInt(RadiusSamplerElements.ACCT_PORT);
	}

	public void setRequestType(String requestType)
	{
		setProperty(RadiusSamplerElements.REQUEST_TYPE, requestType);
	}

	public String getRequestType()
	{
		return getPropertyAsString(RadiusSamplerElements.REQUEST_TYPE);
	}

	public void setUserName(String userName)
	{
		setProperty(RadiusSamplerElements.USER_NAME, userName);
	}

	public String getUserName()
	{
		return getPropertyAsString(RadiusSamplerElements.USER_NAME);
	}
	public void setPassword(String password)
	{
		setProperty(RadiusSamplerElements.PASSWORD, password);
	}

	public String getPassword()
	{
		return getPropertyAsString(RadiusSamplerElements.PASSWORD);
	}

}