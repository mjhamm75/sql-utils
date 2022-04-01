import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class PlainDomainObject<ID: Serializable> : DomainObject<ID> {
    override fun equals(other: Any?): Boolean {
        if (other !is PlainDomainObject<*>) return false
        val id1: Any? = id
        val id2: Any? = other.id
        return if (id1 == null || id2 == null) {
            this === other
        } else {
            id1 == id2
        }
    }
}