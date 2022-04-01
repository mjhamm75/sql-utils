import org.springframework.util.StringUtils
import java.lang.Exception
import java.lang.RuntimeException
import java.util.ArrayList
import javax.persistence.AttributeConverter

open class DelimitedValuesConverter<X> : AttributeConverter<List<X?>?, String> {
    override fun convertToDatabaseColumn(relations: List<X?>?): String {
        return StringUtils.collectionToDelimitedString(relations, DELIMITER)
    }

    override fun convertToEntityAttribute(data: String): List<X?>? {
        return null // this should be implemented in subclasses
    }

    // requires that the enum implement get(String value), would use an interface, but interface does not support static
    fun convertToEntityAttribute(data: String?, enumClass: Class<X>): List<X> {
        val dataElements = StringUtils.delimitedListToStringArray(data, DELIMITER)
        val enums: MutableList<X> = ArrayList()
        for (dataElement in dataElements) {
            try {
                enums.add(enumClass.getDeclaredMethod("get", String::class.java).invoke(null, dataElement) as X)
            } catch (e: Exception) {
                throw RuntimeException(e.message)
            }
        }
        return enums
    }

    companion object {
        const val DELIMITER = "|"
        const val POSTGRES_DELIMITER = ","
    }
}