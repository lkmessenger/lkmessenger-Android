package org.linkmessenger.utils

import android.graphics.Path
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape

class CircleRandomRadius : QrVectorPixelShape {
    private val shapes = listOf(
        QrVectorPixelShape.Circle(.6f),
        QrVectorPixelShape.Circle(.75f),
        QrVectorPixelShape.Circle(.9f),
    )

    private var lastNeighbors = Neighbors.Empty
    private var lastShape = shapes.random()


    override fun createPath(size: Float, neighbors: Neighbors): Path {
        if (lastNeighbors != neighbors) {
            lastNeighbors = neighbors
            lastShape = shapes.random()
        }
        return lastShape.createPath(size, neighbors)
    }
}