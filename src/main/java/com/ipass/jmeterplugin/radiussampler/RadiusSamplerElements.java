package com.ipass.jmeterplugin.radiussampler;

public interface RadiusSamplerElements {

	
	public String AUTH_PORT="RadiusSampler.authPort";
	public String ACCT_PORT="RadiusSampler.acctPort";
	public String SERVER_IP="RadiusSampler.serverIP";
	public String SHARED_SECRET="RadiusSampler.sharedSecret";
	public String PACKET_IDENTIFIER="RadiusSampler.packetIdentifier";
	public String SOCKET_TIMEOUT="RadiusSampler.socketTimeout";
	public String RADIUS_RETRY="RadiusSampler.retry";
	public String REQUEST_TYPE="RadiusSampler.requestType";
	public String RADIUS_ATTRIBUTES="RadiusSampler.radiusAttributes";
	
	public String USER_NAME = "RadiusSampler.userName";
	public String PASSWORD = "RadiusSampler.password";
	
	public String AUTH_REQ="RadiusSampler.authReq";
	public String ACCT_REQ="RadiusSampler.acctReq";
	public String AUTH_ACCT_REQ="RadiusSampler.authAcctReq";

	public String STATUS_TYPE="RadiusSampler.statusType";
	public String LOCAL_PORT="RadiusSampler.localPort";
	public String LOCAL_ADDRESS="RadiusSampler.localAddress";
}
