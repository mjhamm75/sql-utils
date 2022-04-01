import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

class SqlBuilder(sql: String? = "", includeWhere: Boolean = true) {
    val stringBuilder: StringBuilder = StringBuilder("$sql ")
    val sql: String
        get() {
            var sql = stringBuilder.toString()
            val sqlParts = sql.split("(?<!\\()(?i)ORDER BY".toRegex()).toTypedArray()
            sql = sqlParts[0]
            var orderBy = ""
            if (sqlParts.size > 1) {
                orderBy = "ORDER BY " + sqlParts[1]
            }
            var first = 0
            for (condition in conditions) {
                sql += (if (first == 0 && includeWhere) "WHERE " else "AND ") + condition + " "
                first++
            }
            var result = sql + orderBy
            if (limit != null) {
                result = result + " LIMIT " + limit
            }
            if (offset != null) {
                result = result + " OFFSET " + offset
            }
            return result
        }
    var params: MapSqlParameterSource = MapSqlParameterSource()
        private set
    private var conditions: MutableList<String> = mutableListOf()
    private var limit: Int? = null
    private var offset: Int? = null
    // this should be used limitedly
    private var includeWhere: Boolean = includeWhere
        set

    private fun getConditions(): List<String> {
        return conditions
    }

    fun addConditions(sqlBuilder: SqlBuilder): SqlBuilder {
        val conditions = sqlBuilder.getConditions()
        val params = sqlBuilder.params
        this.conditions.addAll(conditions)
        this.params.addValues(params.values)
        return this
    }

    fun addCondition(condition: String, active: Boolean): SqlBuilder {
        if (java.lang.Boolean.TRUE == active) {
            conditions.add(condition)
        }
        return this
    }

    fun addCondition(condition: String): SqlBuilder {
        conditions.add(condition)
        return this
    }

    fun append(sql: Any): SqlBuilder {
        this.stringBuilder.append(sql.toString())
        return this
    }

    fun append(sql: String): SqlBuilder {
        this.stringBuilder.append(sql)
        return this
    }

    private fun getParamNamesFromCondition(condition: String): List<String> {
        val paramNames = condition.split("(?<!:):(?!:)".toRegex()).toTypedArray()
        val paramNameList: MutableList<String> = ArrayList()
        for (i in 1 until paramNames.size) {
            val paramName = paramNames[i]
            paramNameList.add(paramName.split("[ \\),]".toRegex()).toTypedArray()[0])
        }
        return paramNameList
    }

    fun addCondition(condition: String, vararg values: Any?): SqlBuilder {
        val paramNames = getParamNamesFromCondition(condition)
        var hasValue = false
        // This handles case when there is no bind variable
        if (paramNames.size == 0) {
            if (values.size == 1 && java.lang.Boolean.TRUE == values[0]) {
                conditions.add(condition)
                return this
            }
        } else {
            for (i in 0 until values.size) {
                var value = values[i]
                val paramName = paramNames[i]
                if (
                    value == null ||
                    value is String &&
                    value.toString().replace("%", "").isBlank() ||
                    value is List<*> &&
                    value.isEmpty()
                ) {
                    continue
                }
                hasValue = true
                // convert list items as needed
                if (value is List<*>) {
                    val newValues: MutableList<Any> = ArrayList()
                    for (v in value) {
                        newValues.add(getValueFromObject(v!!))
                    }
                    params.addValue(paramName, newValues)
                } else {
                    value = getValueFromObject(value)
                    params.addValue(paramName, value)
                }
            }
        }
        if (hasValue) {
            conditions.add(condition)
        }
        return this
    }

    private fun getValueFromObject(value: Any): Any {
        var value = value
        if (value is LocalDate) {
            value = Date.valueOf(value)
        }
        if (value is LocalDateTime) {
            value = Timestamp.valueOf(value)
        }
        if (value is Enum<*>) {
            value = value.name
        }
/*        if (value is UUID) {
            value = value.toString()
        }*/
        return value
    }

    fun addParam(paramName: String?, value: Any): SqlBuilder {
        var value = value
        value = getValueFromObject(value)
        if (paramName != null) {
            params.addValue(paramName, value)
        }
        return this
    }

    fun addLimit(limit: Int?): SqlBuilder {
        this.limit = limit
        return this
    }

    fun addOffset(offset: Int?): SqlBuilder {
        this.offset = offset
        return this
    }

    val countSql: String
        get() {
            val sql = sql
            val (sqlPartsRemoveOrderBy) = sql.split("(?<!\\()(?i)ORDER BY".toRegex())
            val (sqlPartsRemoveLimit) = sqlPartsRemoveOrderBy.split("(?<!\\()(?i)LIMIT".toRegex())
            val (sqlPartsRemoveOffset) = sqlPartsRemoveLimit.split("(?<!\\()(?i)OFFSET".toRegex())
//            needs alias for count to work properly
            return """SELECT COUNT(*) AS count FROM ($sqlPartsRemoveOffset) original_sql"""
        }

    fun addCondition(condition: String, active: Boolean? = null): SqlBuilder {
        if (java.lang.Boolean.TRUE == active || active == null) {
            conditions.add(condition)
        }
        return this
    }
}