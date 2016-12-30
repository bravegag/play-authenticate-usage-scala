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
	modified TIMESTAMP,
	PRIMARY KEY (id)
);

CREATE TABLE linked_account (
	user_id BIGINT NOT NULL,
	provider_key VARCHAR(255) NOT NULL,
	provider_password VARCHAR(255) NOT NULL,
	modified TIMESTAMP,
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
	modified TIMESTAMP,
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
	modified TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES "user"(id),
	CHECK ("type" IN ('EV', 'PR'))
);

CREATE TABLE security_permission (
	id BIGSERIAL,
  	value VARCHAR(255) NOT NULL,
	modified TIMESTAMP,
	PRIMARY KEY (id)
);

CREATE TABLE user_security_permission (
	user_id BIGINT NOT NULL,
	security_permission_id BIGINT,
	modified TIMESTAMP,
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

CREATE TRIGGER update_modified_user BEFORE UPDATE OR INSERT ON "user" FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_linked_account BEFORE UPDATE OR INSERT ON linked_account FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_user_security_role BEFORE UPDATE OR INSERT ON user_security_role FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_token_action BEFORE UPDATE OR INSERT ON token_action FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_security_permission BEFORE UPDATE OR INSERT ON security_permission FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_user_security_permission BEFORE UPDATE OR INSERT ON user_security_permission FOR EACH ROW EXECUTE PROCEDURE update_modified();

INSERT INTO security_role (name) values ('user');
INSERT INTO security_role (name) values ('administrator');

# --- !Downs

DROP TABLE security_permission CASCADE;

DROP TABLE user_security_permission CASCADE;

DROP TABLE token_action CASCADE;

DROP TABLE user_security_role CASCADE;

DROP TABLE security_role CASCADE;

DROP TABLE linked_account CASCADE;

DROP TABLE "user" CASCADE;

DROP FUNCTION update_modified_column;