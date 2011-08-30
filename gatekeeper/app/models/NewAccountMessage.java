package models;

import java.io.Serializable;

public class NewAccountMessage implements Serializable
{
	private String	field1;
	private String	field2;

	public NewAccountMessage(String field1, String field2)
	{
		this.field1 = field1;
		this.field2 = field2;
	}

	public String getField1()
	{
		return field1;
	}

	public void setField1(String field1)
	{
		this.field1 = field1;
	}

	public String getField2()
	{
		return field2;
	}

	public void setField2(String field2)
	{
		this.field2 = field2;
	}

	public String toString() 
	{
		return "NewAccountMessage [field1=" + field1 + ", field2=" + field2 + "]";
	}
}