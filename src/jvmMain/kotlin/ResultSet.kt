import com.google.gson.Gson
import java.lang.reflect.Type
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException
import java.time.*
import java.util.*

fun ResultSet.retrieveString(name: String): String? {
    return this.getStringFromResult(name)
}

fun ResultSet.retrieveBigDecimal(name: String): BigDecimal? {
    return this.getBigDecimalFromResult(name)
}

fun ResultSet.retrieveDouble(name: String): Double? {
    return this.getDoubleFromResult(name)
}

fun ResultSet.retrieveLong(name: String): Long? {
    return this.getLongFromResult(name)
}

fun ResultSet.retrieveUUID(name: String): UUID? {
    return this.getUUIDFromResult(name)
}

fun ResultSet.retrieveInteger(name: String): Int? {
    return this.getInteger(name)
}

fun ResultSet.retrieveShort(name: String): Short? {
    return this.getShortFromResult(name)
}

fun ResultSet.retrieveLocalDate(name: String): LocalDate? {
    return this.getLocalDate(name)
}

fun ResultSet.retrieveLocalDateTime(name: String): LocalDateTime? {
    return this.getLocalDateTime(name)
}

fun ResultSet.retrieveOffsetDateTime(name: String): OffsetDateTime? {
    return this.getOffsetDateTime(name)
}

fun ResultSet.retrieveBoolean(name: String): Boolean? {
    return this.getBooleanFromResult(name)
}

fun ResultSet.retrieveBooleanNullable(name: String): Boolean? {
    val obj = this.getStringFromResult(name)
    if (obj != null) {
        return obj == "t"
    }
    return null
}

fun <T> ResultSet.retrieveObjectFromJson(name: String, type: Type): T {
    return this.getObjectFromResult(name, type)
}

private fun ResultSet?.getStringFromResult(name: String): String? {
    return try {
        this?.getString(name)
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getDoubleFromResult(name: String): Double? {
    try {
        val obj = this?.getBigDecimal(name)
        if (obj != null) {
            return obj.toDouble()
        }
    } catch (e: SQLException) {
        return null
    }
    return null
}

private fun ResultSet.getBigDecimalFromResult(name: String): BigDecimal? {
    return try {
        val stringValue = this.getString(name)
        return BigDecimal(stringValue)
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getInteger(name: String): Int? {
    return try {
        val obj = this.getString(name)
        obj?.toInt()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getShortFromResult(name: String): Short? {
    return try {
        val obj = this.getString(name)
        obj?.toShort()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getLongFromResult(name: String): Long? {
    return try {
        val obj = this.getStringFromResult(name)
        obj?.toLong()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getUUIDFromResult(name: String): UUID? {
    return try {
        val obj = this.getStringFromResult(name)
        if (obj == null) {
            null
        } else {
            UUID.fromString(obj)
        }
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getBooleanFromResult(name: String): Boolean? {
    return try {
        this.getBoolean(name)
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getLocalDate(name: String): LocalDate? {
    return try {
        val date = this.getDate(name) ?: return null
        Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getLocalDateTime(name: String): LocalDateTime? {
    return try {
        val timestamp = this.getTimestamp(name) ?: return null
        Instant.ofEpochMilli(timestamp.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet.getOffsetDateTime(name: String): OffsetDateTime? {
    return try {
        val timestamp = this.getTimestamp(name) ?: return null
        Instant.ofEpochMilli(timestamp.time).atZone(ZoneId.systemDefault()).toOffsetDateTime()
    } catch (e: SQLException) {
        null
    }
}

private fun <T> ResultSet.getObjectFromResult(name: String, type: Type): T {
    val gson = Gson()
    return gson.fromJson(this.getStringFromResult(name), type)
}