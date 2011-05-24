package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Account extends Model {

	public String provider_name;
	public String dllr_auth_token;
	public String active;
	public String last_error;
	public Date last_confirmed_at;
	public Date last_error_at;
	public Date created_at;
	public Date updated_at;

	public Account(String providerName, String dllrAuthToken, String active,
				   String lastError, Date lastConfirmedAt, Date lastErrorAt,
				   Date createdAt, Date updatedAt) {
		provider_name = providerName;
		dllr_auth_token = dllrAuthToken;
		this.active = active;
		last_error = lastError;
		last_confirmed_at = lastConfirmedAt;
		last_error_at = lastErrorAt;
		created_at = createdAt;
		updated_at = updatedAt;
	}
}