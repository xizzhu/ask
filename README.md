[![Build Status](https://img.shields.io/travis/xizzhu/ask.svg)](https://travis-ci.org/xizzhu/ask)
[![Coverage Status](https://img.shields.io/coveralls/github/xizzhu/ask.svg)](https://coveralls.io/github/xizzhu/ask)
[![API](https://img.shields.io/badge/API-21%2B-green.svg?style=flat)](https://developer.android.com/about/versions/android-5.0.html)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![JitPack](https://img.shields.io/jitpack/v/github/xizzhu/ask.svg)](https://jitpack.io/#xizzhu/ask)

Android SQLite KTX
==================

Kotlin extensions to simplify working with SQLite database.

Download
--------
* Gradle: Add the following to your `build.gradle`:
```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.xizzhu:ask:$latest_version'
}
```

Usage
-----
### Create and Drop Tables
* To create a table, use the `createTable()` function:
```kotlin
database.createTable("tableName") {
  it["textColumn"] = TEXT + PRIMARY_KEY
  it["integerColumn"] = INTEGER + UNIQUE(ConflictClause.REPLACE)
  it["anotherTextColumn"] = TEXT + FOREIGN_KEY("referenceTable", "referenceColumn")
}
```
Each column must have one of the four types: `BLOB`, `INTEGER`, `REAL`, and `TEXT`. It can also also have one or more modifiers: `PRIMARY_KEY`, `NOT_NULL`, `UNIQUE`, `DEFAULT`, and `FOREIGN_KEY`.

More info can be found [here](ask/src/main/kotlin/me/xizzhu/android/ask/db/ColumnModifier.kt).

* To delete a table, use the `dropTable()` function:
```kotlin
database.dropTable("tableName")
```

* To check if a table exists, use the `hasTable()` function:
```kotlin
database.hasTable("tableName")
```

### Create and Drop Indices
* To create an index, use the `createIndex()` function:
```kotlin
database.createIndex("indexName", "tableName", "column1", "column2")
```

* To delete an index, use the `dropIndex()` function:
```kotlin
database.dropIndex("indexName")
```

### Insert Values
To insert a row into a table, use the `insert()`, `insertOrThrow()`, or `insertWithOnConflict()` function:
```kotlin
database.insert("tableName") {
  it["textColumn"] = "random text"
  it["integerColumn"] = 8964L
}

database.insertOrThrow("tableName") {
  it["textColumn"] = "random text"
  it["integerColumn"] = 8964L
}

database.insertWithOnConflict("tableName", SQLiteDatabase.CONFLICT_REPLACE) {
  it["textColumn"] = "random text"
  it["integerColumn"] = 8964L
}
```

### Update Values
To update an existing row, use the `update()` function:
```kotlin
database.update("tableName", { it["textColumn"] = "random new value" }) {
  ("integerColumn" eq 1L) and ("anotherTextColumn" eq "value")
}
```
It supports simple conditions like `eq`, `less`, etc., and logical conditions like `and`, `or`, etc. The full list of supported conditions can be found [here](ask/src/main/kotlin/me/xizzhu/android/ask/db/Condition.kt).

### Delete Values
* To delete all values from a table, use the `deleteAll()` function:
```kotlin
database.deleteAll("tableName")
```

* To delete values matching certain conditions, use the `delete()` function:
```kotlin
database.delete("tableName") {
  "integerColumn" eq 1L
}
```
It supports same [conditions](ask/src/main/kotlin/me/xizzhu/android/ask/db/Condition.kt) as discussed in the Update Values section.

### Query Values
To query values from a table, use the `select()` function:
```kotlin
val query = database.select("tableName") {
  "integerColumn" eq 1L
}
```
It supports same [conditions](ask/src/main/kotlin/me/xizzhu/android/ask/db/Condition.kt) as discussed in the Update Values section.

The returned `Query` object can be further custmized by calling the `groupBy()`, `limit()` or other functions, e.g.:
```kotlin
query.groupBy("integerColumn")
  .having { max("integerColumn") greater 1L }
```

Note that the query is not executed, until `asCursor()` or one of the extension functions is called. More info can be found [here](ask/src/main/kotlin/me/xizzhu/android/ask/db/Query.kt).

### Run a Transaction
* To run a transaction, use the `transaction()` function:
```kotlin
database.transaction {
  // your transaction code
}
```
To abort the transaction, simply throw `TransactionAbortedException`.

* To run a transaction with a return value, use the `withTransaction()` function:
```kotlin
val value = database.withTransaction {
  // your transaction code that returns a value
}
```

License
-------
    Copyright (C) 2019 Xizhi Zhu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
