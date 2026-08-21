package com.deecode.myapp.data.util

import com.deecode.myapp.domain.model.LocationPoint

object PolylineDecoder {

    /**
     * Decodes an encoded polyline string into a list of LocationPoints.
     * Standard Google Encoded Polyline algorithm (lossless precision 1e5).
     */
    fun decode(encodedPath: String): List<LocationPoint> {
        val poly = ArrayList<LocationPoint>()
        var index = 0
        val len = encodedPath.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                if (index >= len) break
                b = encodedPath[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                if (index >= len) break
                b = encodedPath[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latitude = lat.toDouble() / 1E5
            val longitude = lng.toDouble() / 1E5
            poly.add(LocationPoint(latitude = latitude, longitude = longitude))
        }

        return poly
    }
}
