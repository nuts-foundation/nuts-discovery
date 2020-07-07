/*
 *     Nuts discovery service
 *     Copyright (C) 2020 Nuts community
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package nl.nuts.discovery.api

import nl.nuts.discovery.DiscoveryException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletResponse

/**
 * Custom mappings for Exception => http status codes
 */
@ControllerAdvice
class ExceptionMappings {

    val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Map IllegalArgument to http 500
     */
    @ExceptionHandler(value = [IllegalArgumentException::class])
    fun onIllegalArgument(ex: IllegalArgumentException, response: HttpServletResponse) {
        logger.warn(ex.message)
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value())
    }

    /**
     * Map DiscoveryException to http 400
     */
    @ExceptionHandler(value = [DiscoveryException::class])
    fun onIllegalArgument(ex: DiscoveryException, response: HttpServletResponse) {
        logger.error(ex.message, ex)
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.message)
    }
}