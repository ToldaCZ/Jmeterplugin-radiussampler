package com.ipass.jmeterplugin.radiussampler;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.soap.providers.com.Log;
import org.htmlparser.Remark;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;


public class AddAttributes extends RadiusAttribute{


	public AccessRequest addAuthRadiusAttribute(AccessRequest accessRequest,CollectionProperty collectionProperty){

		int size = collectionProperty.size();
		//collectionProperty=removeAttributes(collectionProperty);
		for (int row = 0; row < size; row++)
		{

			JMeterProperty rowProperty = collectionProperty.get(row);
			RadiusAttributes arAttribute = (RadiusAttributes) rowProperty.getObjectValue();

			String name = arAttribute.getName();
			String value = arAttribute.getValue();
			if(name!=null && value!=null && !checkPaddedAttribute(name))
				accessRequest.addAttribute(name, value);

		}

		return accessRequest;
	}

	public static void main(String[] args) {
		CollectionProperty collectionProperty = new CollectionProperty();
		//collectionProperty.addItem("User-Name\tpavan");
		//collectionProperty.addItem("User-Password\tpavan");

		System.out.println(collectionProperty);
		System.out.println(new AddAttributes().removeAttributes(collectionProperty));
	}

	public CollectionProperty removeAttributes(CollectionProperty collectionProperty){
		try{
			int size=collectionProperty.size();
			for(int i=0;i<size;i++) {
				if (collectionProperty.get(i).toString().toLowerCase().startsWith("user-name")) {
					collectionProperty.remove(i);
					break;
				}
			}
			size=collectionProperty.size();
			for(int i=0;i<size;i++) {
				if (collectionProperty.get(i).toString().toLowerCase().startsWith("user-password")) {
					collectionProperty.remove(i);
					break;
				}
			}
			size=collectionProperty.size();
			for(int i=0;i<size;i++) {
				if (collectionProperty.get(i).toString().toLowerCase().startsWith("DTITacct-status-type")) {
					collectionProperty.remove(i);
					break;
				}
			}
		}catch	(Exception e){
			System.out.println("removeAttributes exception:"+e.toString());
		}

		return collectionProperty;
	}
	
	public boolean checkPaddedAttribute(String name){
		try{
			//System.out.println("Debug name: "+name);
			if(name.toLowerCase().startsWith("user-name")||name.toLowerCase().startsWith("user-password"))
				return true;
			
			
		}catch
		(Exception e){
			System.out.println("Debug name - "+name+" Exeption -"+e);
		}

		return false;
	}

	public AccountingRequest addAcctRadiusAttribute(AccountingRequest acctRequest,CollectionProperty collectionProperty){

		collectionProperty=removeAttributes(collectionProperty);
		int size = collectionProperty.size();
		for (int row = 0; row < size; row++)
		{
			JMeterProperty rowProperty = collectionProperty.get(row);
			RadiusAttributes arAttribute = (RadiusAttributes) rowProperty.getObjectValue();

			String name = arAttribute.getName();
			String value = arAttribute.getValue();
			if(name!=null && name.length() >0 && value!=null && value.length()>0 && !checkPaddedAttribute(name)){
				acctRequest.addAttribute(name, value);
			}else if (name==null || name.length() <1 || value==null || value.length()<1){
				System.out.println("Attribute name or val is empty. Attribute - "+name+" value - "+value);
				throw new NullPointerException("Attribute name or val is empty. Attribute - "+name+" value - "+value);
			} else {
				if (!checkPaddedAttribute(name)) {
					System.out.println("Unknown error. Attribute - " + name + " value - " + value);
					throw new NullPointerException("Unknown error. Attribute - " + name + " value - " + value);
				}
			}
		}

		return acctRequest;
	}


	public String getRequiredAttribute(CollectionProperty collectionProperty,String attributeName){

		int size = collectionProperty.size();

		for (int row = 0; row < size; row++)
		{
			JMeterProperty rowProperty = collectionProperty.get(row);
			RadiusAttributes arAttribute = (RadiusAttributes) rowProperty.getObjectValue();

			String name = arAttribute.getName();
			String value = arAttribute.getValue();

			if(name!=null && name.equalsIgnoreCase(attributeName)){
				return value;
			}

		}

		return null;

	}

}
