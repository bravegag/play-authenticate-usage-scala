# schema

# --- !Ups

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

COPY security_role (name) FROM '/home/bravegag/code/play-authenticate-usage-scala/conf/evolutions/default/security_role.csv' DELIMITER ',' CSV;

# --- !Downs

DROP FUNCTION update_modified_column;