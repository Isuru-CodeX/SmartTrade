package lk.jiat.smarttrade.util;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Hibernate 5 keeps property names as they are (Address, createdAt ...) while the SmartTrade
 * database uses lower_snake_case (address, created_at ...). This strategy maps every table and
 * column to lower_snake_case so the schema is identical on Windows, Linux and macOS MySQL servers
 * (Linux MySQL table names are case sensitive).
 */
public class SnakeCaseNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return convert(name);
    }

    private Identifier convert(Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        String text = identifier.getText();
        StringBuilder builder = new StringBuilder(text.length() + 4);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && text.charAt(i - 1) != '_' && !Character.isUpperCase(text.charAt(i - 1))) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return Identifier.toIdentifier(builder.toString(), identifier.isQuoted());
    }
}
