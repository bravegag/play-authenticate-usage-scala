# schema

# --- !Ups

CREATE TABLE "user" (
	id BIGSERIAL,
	first_name VARCHAR(50),
	middle_name VARCHAR(50),
	last_name VARCHAR(50),
	date_of_birth DATE,
	username VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL,
	last_login TIMESTAMP DEFAULT NULL,
	active BOOLEAN NOT NULL DEFAULT FALSE,
	email_validated BOOLEAN NOT NULL DEFAULT FALSE,
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE linked_account (
	user_id BIGINT NOT NULL,
	provider_key VARCHAR(255) NOT NULL,
	provider_password VARCHAR(255) NOT NULL,
	modified TIMESTAMP DEFAULT now(),
	FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE security_role (
	id BIGSERIAL,
	name VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE user_security_role (
	user_id BIGINT NOT NULL,
	security_role_id BIGINT NOT NULL,
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (user_id, security_role_id),
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	FOREIGN KEY (security_role_id) REFERENCES security_role(id)
);

CREATE TABLE token_action (
	user_id BIGINT NOT NULL,
	token VARCHAR(255) UNIQUE NOT NULL,
	"type" CHAR(2) NOT NULL,
	created TIMESTAMP NOT NULL,
	expires TIMESTAMP NOT NULL,
	modified TIMESTAMP DEFAULT now(),
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	CHECK ("type" IN ('EV', 'PR'))
);

CREATE TABLE security_permission (
	id BIGSERIAL,
  	value VARCHAR(255) NOT NULL,
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE user_security_permission (
	user_id BIGINT NOT NULL,
	security_permission_id BIGINT,
	modified TIMESTAMP DEFAULT now(),
	PRIMARY KEY (user_id, security_permission_id),
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	FOREIGN KEY (security_permission_id) REFERENCES security_permission(id)	
);

# --- !Downs

DROP TABLE security_permission CASCADE;

DROP TABLE user_security_permission CASCADE;

DROP TABLE token_action CASCADE;

DROP TABLE user_security_role CASCADE;

DROP TABLE security_role CASCADE;

DROP TABLE linked_account CASCADE;

DROP TABLE "user" CASCADE;
