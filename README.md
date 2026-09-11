# FUSE — Fluent Unified Stream Expressions

A lightweight, type-safe query language for querying and filtering Java object graphs. Navigate nested objects, arrays, and collections transparently, with powerful filtering, aggregation, and sorting capabilities.

## Features

- **Container-transparent navigation**: Seamlessly traverse arrays, collections (`List`, `Set`, etc.), and `Optional` with the same syntax
- **Flexible filtering**: Filter collections with conditions using comparison operators, string matching, membership tests, and collection predicates
- **Logical expressions**: Combine conditions with `&&` (and), `||` (or), and `!` (negation); parentheses for precedence control
- **Collection operations**: Test predicates with `any()`, `all()`, and `none()` on nested collections
- **Parameter bindings**: Use `:paramName` placeholders for reusable, pre-compiled expressions (JPA-style API)
- **String operations**: Built-in `contains()`, `startsWith()`, `endsWith()`, and regex `matches()`
- **Comparison operators**: `==`, `!=`, `<`, `<=`, `>`, `>=` for numeric and string comparisons
- **List membership**: Test inclusion with `in` / `not in`

## Quick Start

```java
// Simple navigation
Object result = FUSE.eval("person.address.city", person);

// Filtering
List<Person> adults = FUSE.compile("persons[age >= 18]")
    .getResultList(data, Person.class);

// Pre-compiled expressions with parameter bindings
Expression expr = FUSE.compile("persons[age >= :minAge && status in :statuses]");
List<Person> filtered = expr
    .setParameter("minAge", 21)
    .setParameter("statuses", List.of("active", "vip"))
    .getResultList(data, Person.class);

// Collection operations
boolean hasExpensiveItems = FUSE.eval("orders[items.any(price > 100)]", store);

// Complex conditions
List<Item> results = FUSE.compile("items[name contains :search && (category == :cat || featured == true)]")
    .setParameter("search", "laptop")
    .setParameter("cat", "electronics")
    .getResultList(store, Item.class);
```

## Expression Syntax

### Navigation
```
person.address.city
persons[0].name
items.each.price
```

### Filtering
```
persons[age >= 18]
items[name == "Widget"]
orders[status != "cancelled"]
products[price > 99.99]
```

### String Operations
```
items[name contains "book"]
products[sku startsWith "ABC"]
categories[label endsWith "-premium"]
entries[value matches "^[0-9]{3}$"]
```

### List Membership
```
persons[name in ("Alice", "Bob")]
items[status not in ("deleted", "archived")]
orders[id in :orderIds]  // parameter binding
```

### Collection Predicates
```
store[items.any(price > 100)]
persons[addresses.all(country == "DE")]
items[orders.none(status == "cancelled")]
```

### Logical Combinations
```
persons[age >= 18 && status == "active"]
items[price < 10 || featured == true]
orders[!cancelled && items.any(price > 50)]
persons[age > 21 && (city == "NYC" || city == "LA")]
```

## Parameter Bindings

Use named parameters (`:paramName`) to create reusable, pre-compiled expressions:

```java
Expression filter = FUSE.compile("persons[age >= :minAge && name != :excluded]");

// Use with different values
filter.setParameter("minAge", 18).setParameter("excluded", "Admin");
List<Person> group1 = filter.getResultList(data, Person.class);

filter.setParameter("minAge", 21).setParameter("excluded", "Guest");
List<Person> group2 = filter.getResultList(data, Person.class);
```

Parameters work in:
- Scalar comparisons: `age >= :minAge`, `:maxAge > price`
- List membership: `name in :allowedNames`, `id not in :excludedIds`
- Nested collection predicates: `items.any(price > :threshold)`

Missing a parameter at evaluation time raises `MissingParameterException`.

## API

### Core Entry Point: `FUSE`

```java
// One-shot evaluation
Object result = FUSE.eval(String expression, Object object);

// Pre-compile for reuse
Expression expr = FUSE.compile(String expression);
```

### Expression

```java
// Evaluate
Object eval(Object object);

// Get single result (throws NonUniqueResultException if multiple)
Object getSingleResult(Object object, Class<?> resultType);

// Get list of results
List<?> getResultList(Object object, Class<?> resultType);

// Parameter binding (fluent, returns this)
Expression setParameter(String name, Object value);
```

## Type Safety

Result types are cast automatically when using `getSingleResult()` and `getResultList()`:

```java
List<Person> adults = FUSE.compile("persons[age >= 18]")
    .getResultList(company, Person.class);
// → returns List<Person>, safe cast

Person ceo = FUSE.compile("company.employees[title == 'CEO']")
    .getSingleResult(company, Person.class);
```

## Reflection & Property Access

Properties are resolved in order:
1. Exact method name match (`getFoo()` for property `foo`)
2. JavaBean getter (`getFoo()` or `isFoo()` for property `foo`)
3. Public fields (if no getter exists)

This ensures encapsulation while supporting flexible object graphs.

## Exception Hierarchy

- `LexerException` — Invalid token or syntax error
- `ParserException` — Grammar violation
- `NonUniqueResultException` — `getSingleResult()` matched multiple results
- `MissingParameterException` — Parameter binding not provided at evaluation time
- `EvaluatorException` — Runtime evaluation error (null navigation, type mismatch, etc.)

All inherit from `RuntimeException`.

## Maven

```xml
<dependency>
    <groupId>com.robinloom</groupId>
    <artifactId>fuse</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

## License

Apache License 2.0
