# sqlFormater-plugin-for-IDE
A simple Intellij IDE plugin, to fotmat sql in java files and surround with StringBuilder "append" function.

Note:

  - default dialect is msql.

## Examples

```sql
select * from t_user where a = b and c > d
```

This will output:

```java
StringBuilder sql = new StringBuilder();
sql.append(" SELECT ");
sql.append("     * ");
sql.append(" FROM ");
sql.append("     t_user ");
sql.append(" WHERE ");
sql.append("     a = b ");
sql.append("     AND c > d ");
```

The sql formatter dependent on <https://github.com/vertical-blank/sql-formatter>.

