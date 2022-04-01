import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.util.StringUtils
import java.lang.RuntimeException
import java.lang.reflect.Type
import java.math.BigDecimal
import java.sql.*
import java.time.*
import java.util.*
import kotlin.reflect.KClass
const val AGG_DELIMITER = "|"

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

// TODO change field to ENUM rather than 3rd state for Boolean
fun ResultSet.retrieveBooleanNullable(name: String): Boolean? {
    val obj = this.getStringFromResult(name)
    if (obj != null) {
        return obj == "t"
    }
    return null
}

fun <T : PlainDomainObject<Long>> ResultSet.retrieveDomainObjectLong(name: String, clazz: KClass<T>): T? {
    return this.getDomainObjectLong(name, clazz.java)
}

fun <T : PlainDomainObject<Long>> ResultSet.retrieveDomainObjectLong(name: String, clazz: Class<T>): T? {
    return this.getDomainObjectLong(name, clazz)
}

fun <T : PlainDomainObject<UUID>> ResultSet.retrieveDomainObjectUUID(name: String, clazz: KClass<T>): T? {
    return this.getDomainObjectUUID(name, clazz.java)
}

fun <T : PlainDomainObject<UUID>> ResultSet.retrieveDomainObjectUUID(name: String, clazz: Class<T>): T? {
    return this.getDomainObjectUUID(name, clazz)
}

fun <E : Enum<E>> ResultSet.retrieveEnum(name: String?, enumType: KClass<E>): E? {
    return this.getEnum(name, enumType.java)
}

fun <E : Enum<*>?> ResultSet?.retrieveEnum(name: String?, enumType: Class<E>?): E? {
    return this.getEnum(name, enumType)
}

fun <T : Any> ResultSet.retrieveList(name: String?, clazz: KClass<T>): MutableList<T> {
    return this.getListFromResult(name, clazz.java) ?: mutableListOf()
}

fun <T> ResultSet.retrieveList(name: String?, clazz: Class<T>?): MutableList<T>? {
    return this.getListFromResult(name, clazz)
}

fun <T : Any> ResultSet.retrieveObjectFromJson(name: String?, clazz: KClass<T>) : T? {
    return this.getObjectFromResult(name, clazz.java)
}

fun <T> ResultSet.retrieveObjectFromJson(name: String?, type: Type?): T {
    return this.getObjectFromResult(name, type)
}

fun ResultSet?.retrieveDelimitedIds(name: String?): List<Long>? {
    return this.getDelimitedIds(name)
}

fun ResultSet.retrieveDelimitedStrings(name: String?): List<String>? {
    return this.getDelimitedStrings(name)
}

private fun <T> ResultSet.getObjectFromResult(name: String?, type: Type?): T {
    val gson = Gson()
    return gson.fromJson(this.getStringFromResult(name), type)
}

private fun <T> ResultSet.getObjectFromResult(name: String?, clazz: Class<T>?): T {
    val gson = Gson()
    return gson.fromJson(this.getStringFromResult(name), clazz)
}

private fun <T> ResultSet.getListFromResult(name: String?, clazz: Class<T>?): MutableList<T>? {
    val listType = TypeToken.getParameterized(MutableList::class.java, TypeToken.get(clazz).type).type
    val gson = Gson()
    return gson.fromJson(this.getStringFromResult(name), listType)
}

// TODO: do not swallow SQLException
private fun ResultSet?.getStringFromResult(name: String?): String? {
    return try {
        this?.getString(name)
    } catch (e: SQLException) {
        null
    }
}

// TODO: do not swallow SQLException
private fun ResultSet?.getDoubleFromResult(name: String?): Double? {
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

// TODO: do not swallow SQLException
private fun ResultSet?.getBigDecimalFromResult(name: String?): BigDecimal? {
    return try {
        this?.getBigDecimal(name)
    } catch (e: SQLException) {
        null
    }
}

// TODO: do not swallow SQLException
private fun ResultSet?.getInteger(name: String?): Int? {
    return try {
        val obj = this?.getString(name)
        obj?.toInt()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getShortFromResult(name: String?): Short? {
    return try {
        val obj = this?.getString(name)
        obj?.toShort()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getLongFromResult(name: String?): Long? {
    return try {
        val obj = this?.getStringFromResult(name)
        obj?.toLong()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getUUIDFromResult(name: String?): UUID? {
    return try {
        val obj = this?.getStringFromResult(name)
        if(obj == null) {
            null
        } else {
            UUID.fromString(obj)
        }
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getBooleanFromResult(name: String?): Boolean? {
    return try {
        this?.getBoolean(name)
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getLocalDate(name: String?): LocalDate? {
    return try {
        val date = this?.getDate(name) ?: return null
        Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getLocalDateTime(name: String?): LocalDateTime? {
    return try {
        val timestamp = this?.getTimestamp(name) ?: return null
        Instant.ofEpochMilli(timestamp.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getOffsetDateTime(name: String?): OffsetDateTime? {
    return try {
        val timestamp = this?.getTimestamp(name) ?: return null
        Instant.ofEpochMilli(timestamp.time).atZone(ZoneId.systemDefault()).toOffsetDateTime()
    } catch (e: SQLException) {
        null
    }
}

private fun <E : Enum<*>?> ResultSet?.getEnum(name: String?, enumType: Class<E>?): E? {
    return try {
        val str = this?.getStringFromResult(name) ?: return null
        java.lang.Enum.valueOf(enumType, str) as E
    } catch (e: SQLException) {
        null
    }
}

// TODO: this requires setId on domain objects, might make sense?
//	public <T extends PlainDomainObject> T getPlainDomainObjectFromResult(ResultSet rs, String name, Class<T> clazz) {
//		try {
//			Integer id = rs.getInt(name);
//			if (id == null) {
//				return null;
//			}
//			PlainDomainObject domainObject = clazz.newInstance();
//			domainObject.
//		} catch (SQLException e) {
//			return null;
//		}
//	}

fun ResultSet?.getDelimitedPostgresArray(name: String?, delimiter: String? = DelimitedValuesConverter.POSTGRES_DELIMITER): List<String>? {
    return try {
        val str = this?.getString(name) ?: return null
        val strRemoveCurlyBrackets = str.replace("{", "").replace("}", "")
        val ids: MutableList<String> = mutableListOf()
        val attributes = StringUtils.delimitedListToStringArray(strRemoveCurlyBrackets, delimiter)
        for (attribute in attributes) {
            ids.add(attribute)
        }
        ids
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getDelimitedIds(name: String?): List<Long>? {
    return try {
        val str = this?.getString(name) ?: return null
        val ids: MutableList<Long> = mutableListOf()
        val attributes = StringUtils.delimitedListToStringArray(str, DelimitedValuesConverter.DELIMITER)
        for (attribute in attributes) {
            ids.add(attribute.toLong())
        }
        ids
    } catch (e: SQLException) {
        null
    }
}

private fun ResultSet?.getDelimitedStrings(name: String?): List<String>? {
    return try {
        val str = this?.getString(name) ?: return null
        ArrayList(
            Arrays.asList(
                *StringUtils.delimitedListToStringArray(
                    str,
                    DelimitedValuesConverter.DELIMITER
                )
            )
        )
    } catch (e: SQLException) {
        null
    }
}

//        TODO: determine whether this should be deleted
//        fun <E : Enum<*>?> getDelimitedEnumsFromResult(rs: ResultSet?, name: String?, enumType: Class<E>): List<*>? {
//            val delimitedValuesConverter: DelimitedValuesConverter<*> = DelimitedValuesConverter<Any?>()
//            return try {
//                val str = rs?.getString(name) ?: return null
//                delimitedValuesConverter.convertToEntityAttribute(str, enumType)
//            } catch (e: SQLException) {
//                null
//            }
//        }

private fun <T : PlainDomainObject<Long>?> ResultSet?.getDomainObjectLong(name: String?, clazz: Class<T>): T? {
    return try {
        val idRaw = this?.getStringFromResult(name)
        if (StringUtils.isEmpty(idRaw)) {
            return null
        }
        val `object` = clazz.newInstance()
        `object`?.id = idRaw!!.toLong()
        `object`
    } catch (e: InstantiationException) {
        throw RuntimeException("Unable to create object with id")
    } catch (e: IllegalAccessException) {
        throw RuntimeException("Unable to create object with id")
    }
}

private fun <T : PlainDomainObject<UUID>?> ResultSet?.getDomainObjectUUID(name: String?, clazz: Class<T>): T? {
    return try {
        val idRaw = this?.getStringFromResult(name)
        if (StringUtils.isEmpty(idRaw)) {
            return null
        }
        val `object` = clazz.newInstance()
        `object`?.id = UUID.fromString(idRaw!!)
        `object`
    } catch (e: InstantiationException) {
        throw RuntimeException("Unable to create object with id")
    } catch (e: IllegalAccessException) {
        throw RuntimeException("Unable to create object with id")
    }
}
