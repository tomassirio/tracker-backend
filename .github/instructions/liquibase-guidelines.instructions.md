---
applyTo: "**/db/changelog/*.yaml"
---

## Liquibase Migration Requirements

When creating database migrations for this Spring Boot project, please follow these guidelines to ensure consistency and maintainability:

### File Naming and Structure
1. **Sequential numbering** - Use format `NNN-description.yaml` (e.g., `001-create-users-table.yaml`)
2. **Descriptive names** - Clearly describe what the migration does
3. **One changeset per file** - Keep migrations focused and atomic
4. **YAML format** - All changesets must be in YAML format (not XML or JSON)

### ChangeSet ID and Author
1. **Use file name as ID** - The changeSet ID should match the filename
2. **Set author** - Always include author: "tomassirio"
3. **Use preconditions** - Add preconditions to prevent re-running migrations

### Database Conventions
1. **Use UUID for IDs** - All primary keys should be `type: uuid`
2. **Use snake_case** - Column names use snake_case (e.g., `user_id`, `creation_timestamp`)
3. **Use JSONB for complex data** - Store nested objects as `type: jsonb`
4. **Use timestamp with time zone** - For all timestamp fields
5. **Add constraints** - Define nullable, primaryKey, and foreign key constraints

### Common Patterns

#### Creating a Table
```yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-users-table
      author: tomassirio
      preConditions:
        - onFail: MARK_RAN
        - not:
            - tableExists:
                tableName: users
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: username
                  type: varchar(255)
                  constraints:
                    nullable: false
                    unique: true
              - column:
                  name: creation_timestamp
                  type: timestamp with time zone
                  constraints:
                    nullable: false
```

#### Adding a Foreign Key
```yaml
        - addForeignKeyConstraint:
            baseTableName: trips
            baseColumnNames: user_id
            referencedTableName: users
            referencedColumnNames: id
            constraintName: fk_trips_user_id
```

#### Adding a Column
```yaml
databaseChangeLog:
  - changeSet:
      id: 009-add-waypoints-to-trips
      author: tomassirio
      preConditions:
        - onFail: MARK_RAN
        - not:
            - columnExists:
                tableName: trips
                columnName: waypoints
      changes:
        - addColumn:
            tableName: trips
            columns:
              - column:
                  name: waypoints
                  type: jsonb
```

#### Creating an Index
```yaml
        - createIndex:
            tableName: users
            indexName: idx_users_username
            columns:
              - column:
                  name: username
```

### JSONB Columns
1. **Use JSONB for complex types** - Store GeoLocation, Reactions, metadata as JSONB
2. **Document structure in comments** - Explain the expected JSON schema
3. **Examples of JSONB usage**:
   - `start_location` - GeoLocation with lat/lon
   - `reactions` - Reaction counts (heart, smiley, sad, laugh, anger)
   - `metadata` - Flexible additional data

### Master Changelog
1. **Update db.changelog-master.yaml** - Include new changesets in the master file
2. **Maintain order** - Keep changesets in chronological order
3. **Example master file**:
```yaml
databaseChangeLog:
  - include:
      file: db/changelog/001-create-users-table.yaml
  - include:
      file: db/changelog/002-create-trips-table.yaml
  - include:
      file: db/changelog/003-create-trip-updates-table.yaml
```

### Preconditions
1. **Always use preconditions** - Prevent errors from re-running migrations
2. **onFail: MARK_RAN** - Mark as run if precondition fails
3. **Check table existence** - Use `tableExists` precondition
4. **Check column existence** - Use `columnExists` precondition

### Best Practices
1. **Never modify existing changesets** - Always create new changesets for changes
2. **Test migrations** - Run `mvn clean install` to validate migrations
3. **Rollback support** - Consider adding rollback changes when appropriate
4. **Keep it simple** - One logical change per changeset
5. **Document complex changes** - Add comments explaining non-obvious migrations

### What to Avoid
- Don't use raw SQL unless absolutely necessary
- Don't modify existing changesets after they've been deployed
- Don't forget preconditions
- Don't use XML or JSON format (use YAML only)
- Don't hardcode values; use appropriate data types
- Don't create tables without primary keys
- Don't forget to add foreign key constraints for relationships
