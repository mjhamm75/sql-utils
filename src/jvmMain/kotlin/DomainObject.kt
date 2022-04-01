import java.io.Serializable

interface DomainObject<ID: Serializable> : Serializable {
    var id: ID?
}