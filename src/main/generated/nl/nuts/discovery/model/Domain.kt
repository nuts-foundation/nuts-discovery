package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonValue
import javax.validation.Valid
import javax.validation.constraints.*

/**
* 
* Values: medical,pgo,insurance,social
*/
enum class Domain(val value: String) {

    medical("medical"),

    pgo("pgo"),

    insurance("insurance"),

    social("social");

}

