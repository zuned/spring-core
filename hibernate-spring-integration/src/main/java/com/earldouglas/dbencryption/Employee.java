package com.earldouglas.dbencryption;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "employee")
public class Employee {

	@Id
	@Column(name = "identifier")
	private String identifier;

	@Column(name = "name")
	private String name;

	/**
	 * The employee's social security number, which is to be encrypted within
	 * the database.
	 */
	@Column(name = "ssn")
	@Type(type = "encryptedString")
	private String ssn;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

}
