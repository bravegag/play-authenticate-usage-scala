# schema

# --- !Ups

CREATE TABLE "user" (
	id BIGSERIAL,
	last_name VARCHAR(50),
	middle_name VARCHAR(50),
	first_name VARCHAR(50),
	dob DATE,
	telephone VARCHAR(100),
	location_id BIGINT,
	username VARCHAR(100),
	email VARCHAR(100),
	password VARCHAR(100) DEFAULT NULL,
	salt VARCHAR(100) DEFAULT NULL,
	last_login timestamp,
	active boolean,
	email_validated boolean,	
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE linked_account (
	user_id BIGINT,
	provider_username varchar(255),
	provider_key varchar(255),
	modified TIMESTAMP DEFAULT now(),
	FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE security_role (
	id BIGSERIAL,
	role_name VARCHAR(255),
	PRIMARY KEY (id)
);

CREATE TABLE user_security_role (
	user_id BIGINT,
	security_role_id BIGINT,
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (user_id, security_role_id),
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	FOREIGN KEY (security_role_id) REFERENCES security_role(id)
);

CREATE TABLE token_action (
	user_id BIGINT,
	token VARCHAR(255) UNIQUE,
	type VARCHAR(2),
	created TIMESTAMP,
	expires TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	CHECK (type IN ('PR', 'EV'))
);

CREATE TABLE security_permission (
	id BIGSERIAL,
  	value VARCHAR(255),
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE user_security_permission (
	user_id BIGINT,
	security_permission_id BIGINT,
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (user_id, security_permission_id),
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	FOREIGN KEY (security_permission_id) REFERENCES security_permission(id)	
);

CREATE OR REPLACE FUNCTION update_modified()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = now();;
    RETURN NEW;;
END;;
$$ language 'plpgsql';

CREATE TRIGGER update_modified_user BEFORE UPDATE ON "user" FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_linked_account BEFORE UPDATE ON linked_account FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_user_security_role BEFORE UPDATE ON user_security_role FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_user_security_permission BEFORE UPDATE ON user_security_permission FOR EACH ROW EXECUTE PROCEDURE update_modified();

# --- !Downs

DROP TABLE security_permission CASCADE;

DROP TABLE user_security_permission CASCADE;

DROP TABLE token_action CASCADE;

DROP TABLE user_security_role CASCADE;

DROP TABLE security_role CASCADE;

DROP TABLE linked_account CASCADE;

DROP TABLE "user" CASCADE;

DROP FUNCTION update_modified_column;