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

CREATE TABLE cookie_token_series (
    user_id BIGINT NOT NULL,
    series VARCHAR(50) NOT NULL,
    token VARCHAR(50) NOT NULL,
    created TIMESTAMP,
    modified TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE linked_account (
	user_id BIGINT NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
	provider_key VARCHAR(50) NOT NULL,
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
	token VARCHAR(50) UNIQUE NOT NULL,
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

CREATE TABLE user_device (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  "type" VARCHAR(50) NOT NULL,
  fingerprint VARCHAR(500) NOT NULL,
  created TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES "user"(id),
  PRIMARY KEY (id)
);

CREATE TABLE gauth_recovery_token (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  token VARCHAR(60) NOT NULL,
  created TIMESTAMP NOT NULL,
  used TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES "user"(id),
  PRIMARY KEY (id)
);

CREATE OR REPLACE FUNCTION update_created()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created = now();;
    RETURN NEW;;
END;;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION update_modified()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = now();;
    RETURN NEW;;
END;;
$$ language 'plpgsql';

CREATE TRIGGER update_modified_user BEFORE UPDATE OR INSERT ON "user" FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_modified_linked_account BEFORE UPDATE OR INSERT ON linked_account FOR EACH ROW EXECUTE PROCEDURE update_modified();
CREATE TRIGGER update_created_cookie_token_series BEFORE INSERT ON cookie_token_series FOR EACH ROW EXECUTE PROCEDURE update_created();
CREATE TRIGGER update_created_gauth_recovery_token BEFORE INSERT ON gauth_recovery_token FOR EACH ROW EXECUTE PROCEDURE update_created();
CREATE TRIGGER update_created_user_device BEFORE INSERT ON user_device FOR EACH ROW EXECUTE PROCEDURE update_created();
CREATE TRIGGER update_modified_cookie_token_series BEFORE UPDATE OR INSERT ON cookie_token_series FOR EACH ROW EXECUTE PROCEDURE update_modified();
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

DROP TABLE cookie_token_series CASCADE;

DROP TABLE gauth_recovery_token CASCADE;

DROP TABLE user_device CASCADE;

DROP TABLE linked_account CASCADE;

DROP TABLE "user" CASCADE;

DROP FUNCTION update_created;

DROP FUNCTION update_modified;