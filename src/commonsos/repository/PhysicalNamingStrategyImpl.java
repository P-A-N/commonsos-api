package commonsos.repository;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PhysicalNamingStrategyImpl extends org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl {
  private static final long serialVersionUID = 1L;

  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
    return context.getIdentifierHelper().toIdentifier(
      name.getText().replaceAll("((?!^)[^_])([A-Z])", "$1_$2").toLowerCase(),
      name.isQuoted()
    );
  }

  public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
    return context.getIdentifierHelper().toIdentifier(
      name.getText().replaceAll("((?!^)[^_])([A-Z])", "$1_$2").toLowerCase(),
      name.isQuoted()
    );
  }
}